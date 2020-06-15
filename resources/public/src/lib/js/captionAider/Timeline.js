/* eslint-disable eqeqeq */
/* eslint-disable no-prototype-builtins */
import { TimedText } from 'yvideo-timedtext'
import { CommandStack } from 'yvideo-editorwidgets'
import WaveForm from './WaveForm'
import Reader from './aurora/aurora.js'

let lastTime = 0,
	requestFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame,
	cancelFrame = window.cancelAnimationFrame || window.mozCancelAnimationFrame

if(!TimedText) throw new Error(`TimedText Uninitialized`)

if(typeof CommandStack !== `function`) throw new Error(`Missing CommandStack Constructor`)

if(!requestFrame) {
	requestFrame = function(callback) {
		const currTime = +new Date(),
			timeToCall = Math.max(0, 16 - (currTime - lastTime)),
			id = window.setTimeout(() => {
				callback(currTime + timeToCall)
			}, timeToCall)
		lastTime = currTime + timeToCall
		return id
	}
	cancelFrame = clearTimeout
}

function Timeline(location, params) {
	if(!(location instanceof HTMLElement)) throw new Error(`Invalid DOM Insertion Point`)
	if(!params) params = {}
	const getFor = params.getFor,
		canvas = document.createElement(`canvas`),
		overlay = document.createElement(`canvas`),
		cache = document.createElement(`canvas`),
		node = document.createElement(`div`),
		cnode = document.createElement(`div`),
		media = params.syncWith && typeof params.syncWith.addEventListener === `function` ? params.syncWith : null,
		lconf = typeof params.confirm === `function`?params.confirm:window.confirm,
		that = this

	let saveLocation = params.saveLocation === void 0?void 0:`${params.saveLocation}`,
		fonts = params.fonts || new Timeline.Fonts({}),
		colors = params.colors || new Timeline.Colors({}),
		images = params.images || new Timeline.Images({}),
		cursors = params.cursors || new Timeline.Cursors({}),
		width = params.width || location.offsetWidth,
		length = params.length || 1800,
		autoCueRepeat = !!params.autoCueRepeat,
		trackSeeker = params.hasOwnProperty(`trackSeeker`)?!!params.trackSeeker:true,
		currentTool = typeof params.tool === `number`?params.tool:Timeline.SELECT,
		trackMode = /showing|hidden|disabled|inherit/.test(params.trackMode)?params.trackMode:`inherit`,
		automove = !!params.automove,
		autocueonbuffer = typeof params.autoCueOnBuffer === `undefined`||!!params.autoCueOnBuffer,
		autofillonbuffer = typeof params.autoFillOnBuffer === `undefined`||!!params.autoFillOnBuffer,
		abRepeatEnabled = false

	Object.defineProperties(this,{
		canGetFor: {
			value: typeof params.canGetFor === `function`
				? params.canGetFor
				: function() {
					return false
				},
		},
		getFor: {
			value: typeof getFor === `function`
				? async function(whatfor,data,defs) {
					const datap = Promise.resolve(getFor(whatfor,data))
					const values = await datap
					const defvalues = values.map((p, i) => {
						let notPromise = true
						const key = data[i]
						try {
							notPromise = typeof p.then !== `function`
						} finally {
							if (notPromise || !defs.hasOwnProperty(key))
								// eslint-disable-next-line no-unsafe-finally
								return p
						}
						return p.then(void 0, () => {
							return defs[key]
						})
					})
					return Promise.all(defvalues)
				}
				: function(_,data,defs) {
					let i, missing
					// eslint-disable-next-line no-cond-assign
					for(i = 0; missing = data[i]; i++)
						if(!defs.hasOwnProperty(missing)) break

					return missing?
						Promise.reject(new Error(`Cannot retrieve ${missing} data.`)):
						Promise.resolve(data.map((key) => {
							return defs[key]
						}))
				},
		},
		fonts: {
			get() {
				return fonts
			},
			set(obj) {
				fonts = obj; this.render()
			},
			enumerable:true,
		},colors: {
			get() {
				return colors
			},
			set(obj) {
				colors = obj; this.render()
			},
			enumerable:true,
		},images: {
			get() {
				return images
			},
			set(obj) {
				images = obj; this.render()
			},
			enumerable:true,
		},cursors: {
			get() {
				return cursors
			},
			set(obj) {
				cursors = obj; this.render()
			},
			enumerable:true,
		},saveLocation: {
			get() {
				return saveLocation
			},
			set(loc) {
				saveLocation = loc === void 0?loc:`${loc}`
				this.emit(new Timeline.Event(`locationchange`))
				this.render()
				return saveLocation
			},enumerable: true,
		},length: { // In seconds
			get() {
				return length
			},
			set(val) {
				let vlen, vend
				if(val !== length) {
					length = val
					vend = this.view.endTime
					vlen = vend - this.view.startTime
					if(length < vend) {
						this.view.endTime = length
						this.view.startTime = Math.max(0,length-vlen)
					}
					this.render()
				}
				return length
			},enumerable:true,
		},width: { // In pixels
			get() {
				return width
			},
			set(val) {
				let id
				if(val !== width) {
					width = +val
					canvas.width = width
					overlay.width = width
					cache.width = width
					for(id in this.audio)
						this.audio[id].width = width

					// Re-render the timeline
					this.render()
				}
				return width
			},enumerable: true,
		},timeMarkerPos: {
			value: 0, writable: true,
		},commandStack: {
			value: params.stack instanceof CommandStack ? params.stack : new CommandStack(),
		},
		abRepeatSet: {
			get() {
				return !(this.repeatA === null || this.repeatB === null)
			},
		},
		abRepeatEnabled: {
			get() {
				return abRepeatEnabled
			},
			set(on) {
				on = !!on
				if(abRepeatEnabled === on) return on
				abRepeatEnabled = on
				this.render()
				this.emit(new Timeline.Event(on?`abrepeatenabled`:`abrepeatdisabled`))
				return on
			},
		},
		abRepeatOn: {
			get() {
				return abRepeatEnabled && this.abRepeatSet
			},
		},
		trackSeeker: {
			get() {
				return trackSeeker
			},
			set(val) {
				let time
				val = !!val
				if(val === trackSeeker) return val
				trackSeeker = val
				if(trackSeeker) { // move the view
					time = this.timeMarkerPos
					if(time < this.view.startTime)
						this.view.center(time - this.view.length/2)
					else if(time > this.view.endTime)
						this.view.center(time + this.view.length/2)

					this.render()
					this.emit(new Timeline.Event(`trackseekeron`))
				}else
					this.emit(new Timeline.Event(`trackseekeroff`))

				return val
			},
		},
		trackMode: {
			get() {
				return trackMode
			},
			set(val) {
				trackMode = /showing|hidden|disabled|inherit/.test(val)?val:`inherit`
				return trackMode
			},
		},
		currentTool: {
			get() {
				return currentTool
			},
			set(tool) {
				const ot = currentTool
				if(typeof tool === `number` && tool !== currentTool) {
					currentTool = tool
					this.emit(new Timeline.Event(`toolchange`,{oldtool: ot, newtool: tool}))
				}
				return currentTool
			},
		},
		automove: {
			get() {
				return automove
			},
			set(val) {
				val = !!val
				if(val === automove) return val
				automove = val
				this.emit(new Timeline.Event(val?`automoveon`:`automoveoff`))
				return val
			},
		},
		autoCueOnBuffer: {
			get() {
				return autocueonbuffer
			},
			set(val) {
				autocueonbuffer = !!val
				this.emit(new Timeline.Event(`autocueonbufferchange`))
				return autocueonbuffer
			},
		},
		autoFillOnBuffer: {
			get() {
				return autocueonbuffer
			},
			set(val) {
				autofillonbuffer = !!val
				this.emit(new Timeline.Event(`autofillonbufferchange`))
				return autofillonbuffer
			},
		},
		autoCueRepeat: {
			get() {
				return autoCueRepeat
			},
			set(val) {
				val = !!val
				if(val === autoCueRepeat) return val
				autoCueRepeat = val
				this.emit(new Timeline.Event(val?`cuerepeaton`:`cuerepeatoff`))
				return val
			},
		},
	})

	this.confirm = function(txt) {
		return Promise.resolve(lconf(txt))
	}
	this.dropLocation = params.dropLocation === void 0?void 0:`${params.dropLocation}`
	this.autoSelect = !!params.autoSelect
	this.autoCueStatus = Timeline.AutoCueResolved
	this.autoCueStart = 0

	this.selectedSegments = []
	this.toCopy = []
	this.events = {}
	this.tracks = []
	this.audio = {}
	this.trackIndices = {}
	this.trackCache = new Map()

	this.activeElement = null
	this.activeIndex = -1
	this.sliderActive = false
	this.scrubActive = false

	this.slider = new Timeline.Slider(this)
	this.view = new Timeline.View(this, params.start || 0, params.end || 60)

	this.repeatA = null
	this.repeatB = null
	this.abRepeatSetting = false

	// Sizing
	this.height = this.keyHeight + this.trackPadding + this.sliderHeight

	// rendering control
	this.requestedTrack = null
	this.requestedFrame = 0

	// mouse control
	this.mouseDown = false
	this.mouseDownPos = {x: 0, y: 0}
	this.mousePos = {x: 0, y: 0}
	this.scrollInterval = 0
	this.currentCursor = `pointer`

	// context menu
	this.activeMenu = null
	this.menuClick = false
	this.menuOptions = Timeline.Menu?[].slice.call(Timeline.Menu):[] // just in case .Menu is overwritten

	// Canvas
	this.canvas = canvas
	this.context = canvas.getContext(`2d`)
	canvas.width = width
	canvas.height = this.height
	canvas.addEventListener(`mousemove`, mouseMove.bind(this), false)
	canvas.addEventListener(`mouseup`, mouseUp.bind(this), false)
	canvas.addEventListener(`mouseout`, mouseOut.bind(this), false)
	canvas.addEventListener(`mousedown`, mouseDown.bind(this), false)
	canvas.addEventListener(`mousewheel`, mouseWheel.bind(this), false)
	canvas.addEventListener(`DOMMouseScroll`, mouseWheel.bind(this), false) // Firefox
	canvas.addEventListener(`contextmenu`, contextMenu.bind(this), false)
	document.addEventListener(`click`, () => {
		if(that.activeMenu && !that.menuClick) {
			that.activeMenu.parentNode.removeChild(that.activeMenu)
			that.activeMenu = null
		}
	},false)
	window.addEventListener(`keyup`, (e) => {
		if(e.keyCode == 27) {
			that.activeMenu.parentNode.removeChild(that.activeMenu)
			that.activeMenu = null
		}
	}, false)

	this.overlay = overlay
	this.octx = overlay.getContext(`2d`)
	overlay.width = width
	overlay.height = this.height
	overlay.style.position = `absolute`
	overlay.style.top = 0
	overlay.style.left = 0
	overlay.style.pointerEvents = `none`

	// This canvas is never seen, but it must be attached to the DOM for LTR text to render properly.
	// No one knows why, it simply is so
	this.cache = cache
	this.ctx = cache.getContext(`2d`)
	cache.width = width
	cache.height = this.height
	cache.style.display = `none`

	node.style.position = `relative`
	cnode.style.position = `relative`
	cnode.appendChild(canvas)
	cnode.appendChild(overlay)
	cnode.appendChild(cache)
	if(!params.hideControls && Timeline.ControlBar)
		node.appendChild(Timeline.ControlBar(this, params.controls))

	node.appendChild(cnode)

	node.addEventListener(`drop`, dragDrop.bind(this), false)
	node.addEventListener(`dragover`, dragOver.bind(this), false)

	location.appendChild(node)

	if(media) {
		media.addEventListener(`loadedmetadata`,setlen,false)
		media.addEventListener(`durationchange`,setlen,false)
		media.addEventListener(`timeupdate`,() => {
			that.currentTime = media.currentTime
		},false)
		// this is simpler than updating every site that emits jump events
		this.on(`jump`,(e) => {
			media.currentTime = e.time
		})
	}

	Timeline.bindKeys(this)
	this.render()

	function setlen() {
		that.length = media.duration
	}
}

Timeline.ORDER = 0
Timeline.SELECT = 1
Timeline.MOVE = 2
Timeline.CREATE = 3
Timeline.DELETE = 4
Timeline.REPEAT = 5
Timeline.SPLIT = 6
Timeline.SHIFT = 7

Timeline.AutoCueResolved = 0
Timeline.AutoCueCueing = 1
Timeline.AutoCueRepeating = 2

Timeline.Event = function(name,data) {
	const that = this
	let prevented = false
	if(typeof data === `object`) {
		Object.keys(data).forEach((key) => {
			that[key] = data[key]
		})
	}

	this.timeStamp = +new Date()
	this.target = null
	this.type = name
	this.preventDefault = function() {
		prevented = true
	}
	Object.defineProperty(this,`defaultPrevented`,{get() {
		return prevented
	}})
}

const Proto = Timeline.prototype

Object.defineProperties(Proto,{
	trackNames: {get() {
		return Object.keys(this.trackIndices)
	}, enumerable: true },
})

// Keybinding stub
Timeline.bindKeys = function() {}

// Sizing
Proto.trackHeight = 50
Proto.trackPadding = 10
Proto.sliderHeight = 25
Proto.sliderHandleWidth = 10
Proto.segmentTextPadding = 5
Proto.keyTop = 0
Proto.keyHeight = 25

/** Event Triggers **/

Proto.emit = function(evt) {
	const that = this, fns = this.events[evt.type]
	evt.target = this
	if(fns instanceof Array) {
		fns.forEach((cb) => {
			try{
				cb.call(that,evt)
			} catch(ignore) {
				console.error(ignore)
			}
		})
	}
	return !evt.defaultPrevented
}

Proto.on = function(name, cb) {
	name = name.toLowerCase()
	if(this.events.hasOwnProperty(name)) this.events[name].push(cb)
	else this.events[name] = [cb]
}

Proto.off = function(name, cb) {
	name = name.toLowerCase()
	if(!this.events.hasOwnProperty(name)) return
	const i = this.events[name].indexOf(cb)
	if(~i) this.events[name].splice(i,1)
}

/** Context menu functions*/

function clickMenu(action,pos,vars) {
	const tl = this.timeline
	if(tl.activeMenu) {
		tl.activeMenu.parentNode.removeChild(tl.activeMenu)
		tl.activeMenu = null
	}
	action.call(this,pos,vars)
}

function checkMenuSize() {
	this.classList.remove(`tl-menu-up`)
	if(this.getBoundingClientRect().bottom > window.innerHeight)
		this.classList.add(`tl-menu-up`)

}

function buildOption(menu,pos,ovars,opt) {
	let vars = ovars

	if(typeof opt.vars === `object`) {
		vars = Object.create(vars)
		Object.keys(opt.vars).forEach(function(key) {
			vars[key] = opt.vars[key].call(this,pos,ovars)
		},this)
	}

	if(typeof opt.condition === `function` && !opt.condition.call(this,pos,vars)) return

	const li = document.createElement(`li`)

	li.innerHTML = `
	<a>
	${String(
		typeof opt.label === `undefined`
			? opt.name
			: typeof opt.label === `function`
				? opt.label.call(this, pos, vars)
				: opt.label,
	)}
	</a>
	`

	if(typeof opt.action === `function`)
		li.addEventListener(`click`,clickMenu.bind(this,opt.action,pos,vars),false)

	const ul = buildLevel(pos, opt, vars, this)

	if(ul !== null) {
		li.appendChild(ul)
		li.addEventListener(`mouseover`, checkMenuSize.bind(ul), false)
	}

	menu.appendChild(li)
}

function buildLevel(pos, level, ovars, that) {
	let menu = null
	if(level.submenu instanceof Array) {
		menu = document.createElement(`ul`)
		level.submenu.forEach(buildOption.bind(that,menu,pos,ovars))
		if(typeof level.calc === `function`)
			level.calc.call(that,buildOption.bind(that,menu,pos,ovars))

	}else if(typeof level.calc === `function`) {
		menu = document.createElement(`ul`)
		level.calc.call(that,buildOption.bind(that,menu,pos,ovars))
	}
	return menu
}

Proto.showMenu = function(pos) {
	const cvs = this.canvas,
		top = pos.y + cvs.offsetTop,
		left = pos.x + cvs.offsetLeft,
		track = this.trackFromPos(pos),
		menu = buildLevel(pos,{submenu:this.menuOptions},{},{
			timeline: this,
			track,
			segment: track && track.segFromPos(pos),
		})

	if(left < cvs.offsetWidth/2) {
		menu.className = `tl-context-menu`
		menu.style.left = `${left + 2}px`
	}else{
		menu.className = `tl-context-menu tl-menu-right`
		menu.style.right = `${cvs.parentNode.offsetWidth-left}px`
	}

	menu.style.top = `${top}px`
	cvs.parentNode.appendChild(menu)
	if(menu.getBoundingClientRect().bottom > window.innerHeight) {
		menu.style.top = `auto`
		menu.style.bottom = `${cvs.parentNode.offsetHeight-top}px`
	}
	this.activeMenu = menu
}

Proto.addMenuItem = function(path,config) { // label, action, condition, calc, index
	let optname, idx, opt,
		submenu = this.menuOptions

	const sequence = path instanceof Array
		? path.slice()
		: typeof path === `string`
			? path.split(`.`)
			: []

	if(typeof config !== `object`) config = {}
	if(!sequence.length) throw new Error(`No Path`)

	opt = {submenu}
	do{
		if(!opt.hasOwnProperty(`submenu`)) opt.submenu = []
		submenu = opt.submenu
		optname = sequence.shift()
		for(idx = 0; (opt = submenu[idx]) && opt.name !== optname; idx++) {
			// console.log(optname,opt);
		}
		if(idx === submenu.length) {
			opt = {name:optname}
			if(typeof config.index === `number`)
				submenu.splice(Math.floor(config.index),0,opt)
			else
				submenu.push(opt)

		}
	}while(sequence.length)

	opt.label = config.label
	if(typeof config.vars === `object`)
		opt.vars = config.vars

	if(typeof config.action === `function`)
		opt.action = config.action

	if(typeof config.calc === `function`)
		opt.calc = config.calc

	if(typeof config.condition === `function`)
		opt.condition = config.condition

}

Proto.getMenuItems = function(path) {
	let optname, idx, opt,
		submenu = this.menuOptions

	const sequence = path.split(`.`)

	opt = {submenu}
	do{
		if(!opt.hasOwnProperty(`submenu`)) return []
		submenu = opt.submenu
		optname = sequence.shift()
		for(idx = 0; (opt = submenu[idx]) && opt.name !== optname; idx++) {
			// console.log(optname,opt);
		}
		if(idx === submenu.length) return []
	}while(sequence.length)
	return opt.submenu.slice()
}

/**
 * Helper Functions
 *
 * These functions deal with manipulating the data
 **/

Proto.timeInView = function(time) {
	return time < this.view.endTime && time > this.view.startTime
}

Proto.spanInView = function(start, end) {
	return start < this.view.endTime && end > this.view.startTime
}

Proto.getTrackTop = function(track) {
	return this.keyHeight + this.trackPadding + this.trackIndices[track.id] * (this.trackHeight + this.trackPadding)
}

Proto.getTrack = function(id) {
	return this.trackIndices.hasOwnProperty(id)?this.tracks[this.trackIndices[id]]:null
}

Proto.trackFromPos = function(pos) {
	return this.tracks[this.indexFromPos(pos)]||null
}

Proto.segFromPos = function(pos) {
	const track = this.tracks[this.indexFromPos(pos)]
	if(!track) return null
	return track.segFromPos(pos)
}

Proto.indexFromPos = function(pos) {
	let i, bottom,
		top = this.keyHeight + this.trackPadding

	const padding = this.trackPadding,
		height = this.trackHeight

	for(i = 0; i < this.tracks.length; i++, top = bottom + padding) {
		bottom = top + height
		if(pos.y >= top && pos.y <= bottom) return i
	}

	return -1
}

function resolveTrack(timeline, tid) {
	const indices = timeline.trackIndices,
		track = tid instanceof Timeline.TextTrack?tid:
			indices.hasOwnProperty(tid)?timeline.tracks[indices[tid]]:
				null
	if(!track) throw new Error(`Track ${tid} Does Not Exist`)
	return track
}

function swaptracks(n,o) {
	const oinfo = this.trackCache.get(o.textTrack)
	this.tracks[this.trackIndices[n.id]] = n
	n.render()
	if(!this.commandStack.isFileSaved(o.id, oinfo.location))
		oinfo.location = void 0

	this.commandStack.removeEvents(o.id)
	this.emit(new Timeline.Event(`removetrack`,{track:o}))
	this.emit(new Timeline.Event(`addtrack`,{track:n}))
}

Proto.cacheTextTrack = function(track, mime, location) {
	this.trackCache.set(track, {mime, location})
}

Proto.hasCachedTextTrack = function(track) {
	return this.trackCache.has(track)
}

Proto.getCachedTextTracks = function() {
	const arr = []
	this.trackCache.forEach((_, key) => {
		arr.push(key)
	})
	return arr
}

Proto.hasTextTrack = function(name) {
	return this.trackIndices.hasOwnProperty(name)
}

function addTlTextTrack(track, from) {
	if(this.trackIndices.hasOwnProperty(track.id))
		swaptracks.call(this,track,this.tracks[this.trackIndices[track.id]])
	else{
		this.trackIndices[track.id] = this.tracks.length
		this.tracks.push(track)
		// Adjust the height
		this.height += this.trackHeight + this.trackPadding
		this.canvas.height = this.height
		this.overlay.height = this.height
		this.cache.height = this.height
		this.render()
		this.emit(new Timeline.Event(`addtrack`,{track}))
	}
	this.commandStack.setFileSaved(track.id, from)
}

Proto.addTextTrack = function(textTrack,mime,from,overwrite) {
	if(!overwrite && this.trackIndices.hasOwnProperty(textTrack.label))
		throw new Error(`Track name already in use.`)

	this.trackCache.set(textTrack, {mime, location: from})
	addTlTextTrack.call(this, new Timeline.TextTrack(this, textTrack, mime), from)
	if(this.trackMode !== `inherit`)
		textTrack.mode = this.trackMode

}

Proto.addCachedTextTrack = function(textTrack,overwrite) {
	if(!overwrite && this.trackIndices.hasOwnProperty(textTrack.label))
		throw new Error(`Track name already in use.`)

	if(!this.trackCache.has(textTrack))
		throw new Error(`Track missing from cache.`)

	const tinfo = this.trackCache.get(textTrack)
	this.addTextTrack(textTrack, tinfo.mime, tinfo.location, overwrite)
}

Proto.removeTextTrack = function(id) {
	let i,t,track,tinfo,aid,loc
	if(this.trackIndices.hasOwnProperty(id)) {
		loc = this.trackIndices[id]
		aid = this.tracks[loc].audioId
		if(this.audio.hasOwnProperty(aid)) this.audio[aid].references--
		track = this.tracks.splice(loc, 1)[0]
		tinfo = this.trackCache.get(track.textTrack)
		if(!this.commandStack.isFileSaved(track.id, tinfo.location))
			tinfo.location = void 0

		delete this.trackIndices[id]
		// eslint-disable-next-line no-cond-assign
		for(i = loc; t = this.tracks[i]; i++)
			this.trackIndices[t.id] = i

		// Adjust the height
		this.height -= this.trackHeight + this.trackPadding
		this.canvas.height = this.height
		this.overlay.height = this.height
		this.cache.height = this.height
		this.render()
		this.commandStack.removeEvents(track.id)
		this.emit(new Timeline.Event(`removetrack`,{track}))
	}
}

Proto.cloneTrack = function(tid, args, filter, overwrite) {
	if(!overwrite && this.trackIndices.hasOwnProperty(args.name))
		throw new Error(`Track name already in use.`)

	addTlTextTrack.call(this, resolveTrack(this, tid)
		.cloneTrack(args, filter))
}

function undoAlterTextTrack(tid, kind, lang, name, overwrite) {
	const track = resolveTrack(this, tid)
	if(typeof name !== `undefined` && name !== track.id) {
		if(this.trackIndices.hasOwnProperty(name)) {
			if(!overwrite) throw new Error(`Track name already in use.`)
			this.removeTextTrack(name)
		}
		this.trackIndices[name] = this.trackIndices[track.id]
		delete this.trackIndices[track.id]
		this.commandStack.renameEvents(track.id,name)
		track.textTrack.label = name
	}

	// avoid side-effects of setting track properties directly
	if(typeof kind !== `undefined`) track.textTrack.kind = kind
	if(typeof lang !== `undefined`) track.textTrack.language = lang

	this.render()
	this.emit(new Timeline.Event(`altertrack`,{track}))
}

Proto.alterTextTrack = function(tid, kind, lang, name, overwrite) {
	const track = resolveTrack(this, tid),
		oname = track.textTrack.label,
		okind = track.textTrack.kind,
		olang = track.textTrack.language

	// Set up the undo/redo functions
	const undo = undoAlterTextTrack.bind(
		this, name,
		okind, olang,
		oname, overwrite,
	)
	const redo = undoAlterTextTrack.bind(
		this, oname,
		kind, lang,
		name, overwrite,
	)
	this.commandStack.push({
		file: oname,
		redo,
		undo,
	})

	// perform the edit
	redo()
}

Proto.setAutoCue = function(onoff, tid) {
	if(typeof tid === `undefined`) {
		this.tracks.forEach((track) => {
			track.autoCue = onoff
		})
		return
	}
	if(typeof tid.map === `function`) {
		tid.map(resolveTrack.bind(this)).forEach((t) => {
			t.autoCue = onoff
		})
	} else
		resolveTrack(this, tid).autoCue = onoff
}

/** Audio Functions **/

Proto.addAudioTrack = function(wave, id) {
	let track
	if(this.audio.hasOwnProperty(id)) throw new Error(`Track with that id already loaded.`)
	if(wave instanceof Timeline.AudioTrack) {
		track = wave
		id = wave.id
	}else
		track = new Timeline.AudioTrack(this, wave, id)

	this.audio[id] = track
	this.render()
}

Proto.loadAudioTrack = function(source, id) {
	const rate = 1001,
		reader = Reader[source instanceof File?`fromFile`:`fromURL`](source),
		wave = new WaveForm(
			this.width,
			this.trackHeight,
			1/* channels*/,rate,
		)

	let bufsize = 10000,
		chansize, buffer, channels, resampler

	this.addAudioTrack(wave, id)
	console.log(`Initializing Audio Decoder`)
	console.time(`audio ${id}`)

	reader.on(`format`, (data) => {
		console.log(`Decoding Audio...`)
		channels = data.channelsPerFrame
		bufsize -= bufsize%channels
		buffer = new Float32Array(bufsize)
		chansize = bufsize/channels
		resampler = new Resampler({
			from: data.sampleRate,
			to: rate,
			bitrate: 32,
			channels: 1,
			// smaller buffers mean smoother drawing
			// large buffers mean slight speed increases
			bufferSize: 256,
		})
		resampler.on(`data`, (channels) => {
			wave.addFrame(channels[0]) // addFrame emits redraw
		})
	})
	reader.on(`ready`, getData)
	reader.start()

	function getData() {
		let i, j, chan
		if(reader.get(buffer) !== `filled`) {
			resampler.flush()
			console.log(`Finished Decoding`)
			console.timeEnd(`audio ${id}`)
		}else{
			// deinterlace; select only the first channel
			chan = new Float32Array(chansize)
			for(i=0,j=0;j<bufsize;i++,j+=channels)
				chan[i] = buffer[j]

			resampler.append([chan])
			setTimeout(getData,0)
		}
	}
}

Proto.removeAudioTrack = function(id) {
	let i, top, ctx, track
	if(!this.audio.hasOwnProperty(id)) return
	if(this.audio[id].references) {
		top = this.keyHeight+this.trackPadding
		ctx = this.octx
		for(i=0;track=this.tracks[i];i++) {
			if(track.active && track.audioId === id)
				ctx.clearRect(0, top, this.width, this.trackHeight)

			top += this.trackHeight + this.trackPadding
		}
	}
	delete this.audio[id]
}

Proto.setAudioTrack = function(tid, aid) {
	const track = resolveTrack(this, tid)
	if(this.audio.hasOwnProperty(track.audioId)) this.audio[track.audioId].references--
	track.audioId = aid
	if(this.audio.hasOwnProperty(aid)) {
		this.audio[aid].references++
		this.audio[aid].render()
	}
}

Proto.unsetAudioTrack = function(tid) {
	const track = resolveTrack(this, tid),
		audio = this.audio[track.audioId]
	if(audio) {
		track.audioId = null
		audio.references--
		this.octx.clearRect(0, this.getTrackTop(track), this.width, this.trackHeight)
	}
}

function getLineBuffer(source) {
	return new Promise((resolve,reject) => {
		let reader
		if(source instanceof File) {
			reader = new FileReader()
			reader.onerror = reject
			reader.onload = function(evt) {
				resolve([evt.target.result,source.type || TimedText.inferType(source.name)])
			}
			reader.readAsText(source)
		}else if(typeof source === `string` || source instanceof String) {
			reader = new XMLHttpRequest()
			reader.open(`GET`, source, true)
			reader.onerror = reject
			reader.onload = function() {
				resolve([this.responseText,this.getResponseHeader(`content-type`)])
			}
			reader.send(null)
		}else if(source instanceof Object)
			resolve([source.content,source.mime || TimedText.inferType(source.name)])

	}).then((arr) => {
		const text = arr[0], mime = arr[1]
		try{
			return TimedText.parse(mime,text).cueList.map((cue) => {
				return cue.text
			})
		} catch(_) {
			return text.split(/[\r\n]+/g).map((s) => {
				return s.trim()
			})
		}
	})
}

Proto.loadLineBuffer = function(tid,src) {
	const that = this,
		track = resolveTrack(this, tid)
	getLineBuffer(src).then((lines) => {
		lines.reverse();
		[].push.apply(track.linebuffer,lines)
		if(that.autoCueOnBuffer) track.autoCue = true
		if(that.autoFillOnBuffer) track.autoFill = true
	})
}

Proto.clearLineBuffer = function(tid) {
	resolveTrack(this, tid).linebuffer = []
}

Proto.addSegment = function(tid, cue, select) {
	resolveTrack(this, tid).add(cue, select)
}

Proto.shiftTrack = function(tids,delta,all) {
	if(!(tids instanceof Array)) tids = [tids]
	tids.forEach(function(tid) {
		resolveTrack(this, tid).shift(delta,all)
	},this)
}

/** Drawing functions **/

function renderBackground(tl) {
	const ctx = tl.ctx,
		grd = ctx.createLinearGradient(0,0,0,tl.height)

	// Draw the backround color
	grd.addColorStop(0,tl.colors.bgTop)
	grd.addColorStop(0.5,tl.colors.bgMid)
	grd.addColorStop(1,tl.colors.bgBottom)
	ctx.save()
	ctx.fillStyle = grd
	ctx.globalCompositeOperation = `source-over`
	ctx.fillRect(0, 0, tl.width, tl.height)
	ctx.restore()
}

function renderKey(tl) {
	let ctx = tl.ctx,
		view = tl.view,
		zoom = view.zoom,
		font = tl.fonts.key,
		power, d=0,
		hours, mins, secs, pixels,
		start, end, position, offset, increment

	ctx.save()
	ctx.font = font.font
	ctx.fillStyle = font.color
	ctx.strokeStyle = font.color
	ctx.textBaseline = `top`

	// Find the smallest increment in powers of 2 that gives enough room for 1-second precision
	power = Math.ceil(Math.log(ctx.measureText(` 0:00:00`).width*zoom)/0.6931471805599453)
	increment = Math.pow(2,power)
	pixels = increment/zoom

	// if we're below 1-second precision, adjust the increment to provide extra room
	if(power < 0) {
		d = power<-2?3:-power
		if(pixels < ctx.measureText(` 0:00:0${(0).toFixed(d)}`).width) {
			increment*=2
			pixels*=2
			d--
		}
	}

	start = view.startTime
	start -= start%increment
	end = view.endTime
	offset = tl.canvas.dir === `rtl` ? -2 : 2

	for (position = view.timeToPixel(start); start < end; start += increment, position += pixels) {
		// Draw the tick
		ctx.beginPath()
		ctx.moveTo(position, tl.keyTop)
		ctx.lineTo(position, tl.keyTop + tl.keyHeight)
		ctx.stroke()

		// Now put the number on
		secs = start % 60
		mins = Math.floor(start / 60)
		hours = Math.floor(mins / 60)
		mins %= 60

		ctx.fillText(
			hours + (mins<10?`:0`:`:`) + mins + (secs<10?`:0`:`:`) + secs.toFixed(d), position + offset,
			tl.keyTop + 2,
		)
	}
	ctx.restore()
}

function renderABRepeat(tl) {
	if(!tl.abRepeatSet) return
	const left = tl.view.timeToPixel(tl.repeatA),
		right = tl.view.timeToPixel(tl.repeatB),
		ctx = tl.ctx
	ctx.save()
	ctx.fillStyle = tl.colors[tl.abRepeatEnabled?`abRepeat`:`abRepeatLight`]
	ctx.fillRect(left, 0, right-left, tl.height)
	ctx.restore()
}

function renderTimeMarker(tl, x) {
	const ctx = tl.context
	ctx.save()
	ctx.fillStyle = tl.colors.timeMarker
	ctx.fillRect(x, 0, 2, tl.height)
	ctx.restore()
}

function renderTrack(tid) {
	let left, right, ctx,
		track = resolveTrack(this, tid),
		top = this.getTrackTop(track),
		height = this.trackHeight,
		x = this.view.timeToPixel(this.timeMarkerPos)-1

	track.render()
	// redo the peice of the abRepeat that we drew over
	if(this.abRepeatSet) {
		left = this.view.timeToPixel(this.repeatA)
		right = this.view.timeToPixel(this.repeatB)
		if(right >= 0 || left <= this.width) {
			ctx = this.ctx
			ctx.save()
			ctx.fillStyle = this.colors[this.abRepeatEnabled?`abRepeat`:`abRepeatLight`]
			ctx.fillRect(left, top, right-left, height)
			ctx.restore()
		}
	}
	ctx = this.context
	// This copy is expensive; need to make it faster!
	ctx.drawImage(this.cache,0,top,this.width,height,0,top,this.width,height)
	// redo the peice of the timeMarker that we drew over
	if(x >= 0 || x <= this.width) {
		ctx.save()
		ctx.fillStyle = this.colors.timeMarker
		ctx.fillRect(x, top, 2, height)
		ctx.restore()
	}
	this.requestedTrack = null
	this.requestedFrame = 0
}

function render(stable) {
	let aid, x
	if(this.images.complete) {
		if(!stable) {
			renderBackground(this)
			renderKey(this)
			this.tracks.forEach((track) => {
				track.render()
			})
			for(aid in this.audio) this.audio[aid].render()
			renderABRepeat(this)
			this.context.drawImage(this.cache,0,0)
		}
		x = this.view.timeToPixel(this.timeMarkerPos)-1
		if(x >= -1 && x < this.width)
			renderTimeMarker(this,x)

		this.slider.render()
		this.requestedTrack = null
		this.requestedFrame = 0
	}else{
		this.requestedTrack = null
		this.requestedFrame = requestFrame(render.bind(this,stable))
	}
}

Proto.renderTrack = function(tid) {
	const track = resolveTrack(this, tid)
	if(this.requestedFrame !== 0) {
		if(this.requestedTrack === track) return
		cancelFrame(this.requestedFrame)
		this.requestedTrack = null
		this.requestedFrame = requestFrame(render.bind(this,false))
	}else{
		this.requestedTrack = track
		this.requestedFrame = requestFrame(renderTrack.bind(this,track))
	}
}

Proto.render = function(stable) {
	if(this.requestedTrack !== null) {
		this.requestedTrack = null
		cancelFrame(this.requestedFrame)
		this.requestedFrame = requestFrame(render.bind(this,false))
	}else if(this.requestedFrame !== 0) {
		if(!stable) {
			cancelFrame(this.requestedFrame)
			this.requestedFrame = requestFrame(render.bind(this,false))
		}
	}else
		this.requestedFrame = requestFrame(render.bind(this,stable))

}

Proto.restore = function() {
	let x
	if(!this.images.complete) return
	this.context.drawImage(this.cache,0,0)
	x = this.view.timeToPixel(this.timeMarkerPos)-1
	if(x >= -1 && x < this.width)
		renderTimeMarker(this,x)

	this.slider.render()
}

/** Time functions **/

Object.defineProperties(Proto,{
	currentTime: {
		set(time) {
			let x, startTime, endTime, stable = false
			time = Math.min(Math.max(time, 0), this.length)
			if(time === this.timeMarkerPos) return time
			if(this.abRepeatOn && time > this.repeatB) {
				if(this.emit(new Timeline.Event(`jump`,{time:this.repeatA})))
					time = this.repeatA

			}

			isstable: {
				if(this.trackSeeker) {
					// move the view
					if(time < this.view.startTime) {
						this.view.center(time - this.view.length/4)
						break isstable
					}else if(time > this.view.endTime) {
						this.view.center(time + this.view.length/4)
						break isstable
					}
				}
				stable = true
				// erase old time marker if necessary
				x = this.view.timeToPixel(this.timeMarkerPos)-1
				if(x > -1 && x < this.width)
					this.context.drawImage(this.cache,x,0,2,this.height,x,0,2,this.height)

			}

			if(this.autoCueStatus === Timeline.AutoCueCueing) {
				startTime = Math.min(this.autoCueStart,time)
				endTime = Math.max(this.autoCueStart,time)
				this.tracks.forEach((track) => {
					track.textTrack.currentTime = time
					if(track.autoCue) track.setPlaceholder(startTime, endTime)
				})
			}else {
				this.tracks.forEach((track) => {
					track.textTrack.currentTime = time
				})
			}

			this.timeMarkerPos = time
			this.emit(new Timeline.Event(`timeupdate`))
			this.render(stable)
			return time
		},
		get() {
			return this.timeMarkerPos
		},
		enumerable: true,
	},
	timeCode: {
		get() {
			let time = this.timeMarkerPos,
				secs = time % 60,
				mins = Math.floor(time / 60),
				hours = Math.floor(mins / 60)
			mins %= 60
			return hours + (mins<10?`:0`:`:`) + mins + (secs<10?`:0`:`:`) + secs.toFixed(3)
		},enumerable: true,
	},
})

export function updateABPoints(pos) {
	this[pos.x < this.view.timeToPixel((this.repeatA + this.repeatB) / 2)?`repeatA`:`repeatB`] = this.view.pixelToTime(pos.x)
	this.render()
}

export function resetABPoints(pos) {
	this.repeatB = this.repeatA = this.view.pixelToTime(pos.x)
	this.emit(new Timeline.Event(`abrepeatset`))
}

Proto.clearRepeat = function() {
	this.repeatA = null
	this.repeatB = null
	this.abRepeatSetting = false
	this.render()
	this.emit(new Timeline.Event(`abrepeatunset`))
}

Proto.setRepeat = function(start,end) {
	this.repeatA = Math.min(start,end)
	this.repeatB = Math.max(start,end)
	this.abRepeatSetting = false
	// the setter takes care of re-rendering
	if(!this.abRepeatEnabled && this.abRepeatSet)
		this.abRepeatEnabled = true
	else this.render()
	this.emit(new Timeline.Event(`abrepeatset`))
}

Proto.breakPoint = function() {
	const time = this.currentTime,
		tracks = this.tracks.filter((track) => {
			return track.autoCue
		})
	if(!tracks.length) return
	switch(this.autoCueStatus) {
	case Timeline.AutoCueResolved:
		this.autoCueStatus = Timeline.AutoCueCueing
		this.autoCueStart = time
		break
	case Timeline.AutoCueCueing:
		tracks.forEach(function(track) {
			track.setPlaceholder(this.autoCueStart-.01, time+.01)
			track.resolvePlaceholder()
		},this)
		this.autoCueStatus = this.autoCueRepeat?
			Timeline.AutoCueRepeating:
			Timeline.AutoCueResolved
		break
	case Timeline.AutoCueRepeating:
		this.clearRepeat()
	default: // invalid state recovery
		this.autoCueStatus = Timeline.AutoCueResolved
	}
}

/** Persistence functions **/

Proto.exportTracks = function(id) {
	const that = this
	return (function() {
		let track
		if(typeof id === `undefined`) {
			// save all tracks
			return that.tracks
		}
		if(typeof id.map === `function`) { // save multiple tracks
			return id.map((tid) => {
				return resolveTrack(that, tid)
			})
		}

		// save a single track
		return [resolveTrack(that, id)]
	}()).map((track) => {
		return {
			track: track.textTrack, // temporary hack for back-compatibility
			collection:`tracks`,
			mime: track.mime,
			name: TimedText.addExt(track.mime,track.id),
			data: track.serialize(),
		}
	})
}

Proto.loadTextTrack = function(src, kind, lang, name, from, overwrite) {
	const that = this
	return new Promise((resolve,reject) => {
		TextTrack.get({
			src,
			kind,
			lang,
			label: name,
			success(track, mime) {
				that.addTextTrack(track,mime,from,overwrite)
				resolve(track)
			},
			error: reject,
		})
	})
}

/** Scroll Tool Functions **/

function autoScroll() {
	const delta = this.mousePos.x/this.width-0.5
	if(delta) {
		this.view.move(10*delta*this.view.zoom)
		this.render()
	}
}

function initScroll() {
	this.currentCursor = `move`
	this.canvas.style.cursor = this.cursors.move
	this.scrollInterval = setInterval(autoScroll.bind(this),1)
}

function autoSizeL() {
	const mx = this.mousePos.x,
		dx = mx - this.slider.startx
	if(dx) {
		this.view.startTime += dx*this.view.zoom/10
		this.render()
	}
}

function autoSizeR() {
	const mx = this.mousePos.x,
		dx = mx - this.slider.endx
	if(dx) {
		this.view.endTime += dx*this.view.zoom/10
		this.render()
	}
}

function initResize() {
	const diff = this.mouseDownPos.x - this.slider.middle
	if(diff < 0) {
		this.currentCursor = `resizeL`
		this.canvas.style.cursor = this.cursors.resizeL
		this.scrollInterval = setInterval(autoSizeL.bind(this),1)
	}else if(diff > 0) {
		this.currentCursor = `resizeR`
		this.canvas.style.cursor = this.cursors.resizeR
		this.scrollInterval = setInterval(autoSizeR.bind(this),1)
	}
}

/**
 * Event Listeners and Callbacks
 *
 * These listeners include mouseMove, mouseUp, and mouseDown.
 * They check the mouse location and active elements and call their mouse listener function.
 *
 * Author: Joshua Monson
 **/

function updateCursor(pos) {
	if(typeof pos !== `object`) return
	let i, track, cursor = `pointer`

	// Check the slider
	i = this.slider.onHandle(pos)
	if(i === 1) cursor = `resizeR`
	else if(i === -1) cursor = `resizeL`
	else if(this.slider.onBar(pos)) cursor = `move`
	// scrubbing
	else if(pos.y < this.keyHeight+this.trackPadding) cursor = `skip`
	else if(pos.shift) { // navigation
		cursor =	this.mousePos.y < this.height - this.sliderHeight - this.trackPadding?`move`:
			this.mousePos.x < this.slider.middle?`resizeL`:`resizeR`
	}else if(this.currentTool === Timeline.REPEAT)
		cursor = !(this.abRepeatOn || this.abRepeatSetting) || pos.x < this.view.timeToPixel((this.repeatA + this.repeatB) / 2)?`repeatA`:`repeatB`
	else if(track = this.trackFromPos(pos)) { // Are we on a track?
		cursor = 	this.currentTool === Timeline.ORDER?`order`:
			this.currentTool === Timeline.SHIFT?`move`:
				this.currentTool === Timeline.SELECT?`select`:
					track.getCursor(pos)
	}
	if(this.currentCursor !== cursor) {
		this.currentCursor = cursor
		this.canvas.style.cursor = this.cursors[cursor]
	}
}

function mouseMove(ev) {
	let i, active, swap,
		pos = {x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY, ctrl: ev.ctrlKey || ev.metaKey, shift: ev.shiftKey}

	this.mousePos = pos

	if(this.scrollInterval) return
	if(this.scrubActive) {
		i = this.view.pixelToTime(pos.x)
		if(this.emit(new Timeline.Event(`jump`, {time:i})))
			this.currentTime = i

	}else if(this.currentTool === Timeline.REPEAT && this.abRepeatSetting) {
		updateABPoints.call(this,pos)
		updateCursor.call(this,pos)
		this.render()
	}else if(this.currentTool === Timeline.ORDER
		&& this.activeIndex !== -1) {
		i = this.indexFromPos(pos)
		if(i !== -1 && i !== this.activeIndex) {
			swap = this.tracks[i]
			active = this.tracks[this.activeIndex]

			this.tracks[i] = active
			this.tracks[this.activeIndex] = swap

			this.trackIndices[swap.id] = this.activeIndex
			this.trackIndices[active.id] = i

			this.activeIndex = i
			this.render() // could gain efficiency by just copying image segments
		}
	}else if(this.sliderActive)
		this.slider.mouseMove(pos)
	else if(this.activeElement)
		this.activeElement.mouseMove(pos)
	else
		updateCursor.call(this,pos)

	ev.preventDefault()
}

function mouseUp(ev) {
	if(ev.button > 0 || !this.mouseDown) return
	if(this.currentTool === Timeline.REPEAT) {
		this.abRepeatSetting = false
		this.abRepeatEnabled = this.abRepeatEnabled || this.abRepeatSet
	}
	mouseInactive.call(this,{x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY})
	ev.preventDefault()
}

function mouseOut(ev) {
	if(ev.button > 0 || !this.mouseDown) return
	mouseInactive.call(this,{x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY})
	ev.preventDefault()
}

function mouseInactive(pos) {
	let id
	this.mouseDown = false
	this.activeIndex = -1
	if(this.scrubActive) {
		this.scrubActive = false
		updateCursor.call(this,pos)
	}else if(this.sliderActive) {
		this.slider.mouseUp(pos)
		this.sliderActive = false
		for(id in this.audio) this.audio[id].redraw()
	}else if(this.scrollInterval) {
		clearInterval(this.scrollInterval)
		this.scrollInterval = 0
		for(id in this.audio) this.audio[id].redraw()
	}else if(this.activeElement !== null) {
		this.activeElement.mouseUp(pos)
		this.activeElement = null
	}
}

function mouseDown(ev) {
	if(ev.button > 0) return
	let pos = {x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY, ctrl: ev.ctrlKey || ev.metaKey, shift: ev.shiftKey},
		track,i

	ev.preventDefault()
	document.activeElement.blur()
	if(this.activeMenu) {
		this.activeMenu.parentNode.removeChild(this.activeMenu)
		this.activeMenu = null
	}

	this.mouseDown = true
	this.mouseDownPos = pos
	this.mousePos = pos

	// Check the slider
	if(this.slider.containsPoint(pos)) {
		this.slider.mouseDown(pos)
		this.sliderActive = true
	}else if(this.slider.onBar(pos)) {
		if(pos.shift)
			initResize.call(this)
		else{
			this.slider.middle = pos.x
			this.render()
			if(pos.y > this.height - this.sliderHeight) {
				this.slider.mouseDown(pos)
				this.sliderActive = true
				this.canvas.style.cursor = this.cursors.move
			}
		}
	}else
	// Check the key
	if(pos.y < this.keyHeight+this.trackPadding) {
		this.scrubActive = true
		i = this.view.pixelToTime(pos.x)
		if(this.emit(new Timeline.Event(`jump`, {time:i})))
			this.currentTime = i

	}else
	// Check navigation
	if(pos.shift) initScroll.call(this)
	// Check tool activation
	else{
		if(!pos.ctrl) {
			switch(this.currentTool) {
			case Timeline.REPEAT:
				this.abRepeatSetting = true;
				(this.abRepeatSet?updateABPoints:resetABPoints).call(this,pos)
				return
			case Timeline.ORDER:
				this.activeIndex = this.indexFromPos(pos)
				return
			case Timeline.SELECT:
				this.activeElement = new Selection(this,pos)
				return
			}
		}
		// Check tracks
		track = this.trackFromPos(pos)
		this.activeElement = track
		track && track.mouseDown(pos)
	}
}

function mouseWheel(ev) {
	let i, that = this,
		pos = {x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY, ctrl: ev.ctrlKey || ev.metaKey, shift: ev.shiftKey},
		delta = ev.detail?ev.detail>0?-1:1:ev.wheelDelta>0?1:-1

	if(this.activeMenu) {
		this.activeMenu.parentNode.removeChild(this.activeMenu)
		this.activeMenu = null
	}

	this.mousePos = pos

	if(pos.y > this.height - this.sliderHeight - this.trackPadding) { // Check the slider
		this.slider.middle += delta
	}else if(pos.y < this.keyHeight+this.trackPadding) { // Check the key
		i = Math.min(Math.max(this.currentTime + delta*this.view.zoom,0),this.length)
		if(i !== this.currentTime) {
			if(this.emit(new Timeline.Event(`jump`, {time:i})))
				this.currentTime = i

		}
	}else{
		delta /= 10
		this.view.startTime += delta*(this.view.pixelToTime(pos.x)-this.view.startTime)
		this.view.endTime += delta*(this.view.pixelToTime(pos.x)-this.view.endTime)
	}
	this.render()
	Object.keys(this.audio).forEach((key) => {
		that.audio[key].redraw()
	})
	ev.preventDefault()
	return false
}

function addDroppedTrack(track, mime, from) {
	track.mode = `showing`
	this.addTextTrack(track,mime,from,true)
	this.emit(new Timeline.Event(`droptrack`, {track}))
}

function dragDrop(ev) {
	ev.stopPropagation()
	ev.preventDefault()
	let that = this, links, types,
		track = this.trackFromPos({x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY}),
		dataTransfer = ev.dataTransfer,
		files = dataTransfer.files

	if(files.length) { // Load Local Files
		[].forEach.call(files,(file) => {
			let name
			if(file.type.substr(0,6) === `audio/`) { // Load audio waveform
				name = file.name
				that.loadAudioTrack(file,name)
				if(files.length === 1 && track)
					that.setAudioTrack(track.id,name)

			}else{ // Load text track
				// If we don't supply a label, TextTrack will automatically use the file name with extension removed
				// This is simpler than stripping it here, so omit that parameter unless we want to fill it in
				// with user input at some point in the future
				TextTrack.get({
					src: file, // label: file.name,
					kind: `subtitles`, lang: `zxx`,
					success(track, mime) {
						addDroppedTrack.call(that,track,mime,that.dropLocation)
					},
				})
			}
		})
	}else{ // Load from URLs
		types = [].slice.call(dataTransfer.types)
		if(~types.indexOf(`text/x-moz-url`)) {
			links = dataTransfer.getData(`text/x-moz-url`).split(`\n`).filter((e,i) => {
				return !(i%2)
			})
		} else if(~types.indexOf(`text/uri-list`)) {
			links = dataTransfer.getData(`text/uri-list`).split(`\n`).filter((e) => {
				return e[0]!==`#`
			})
		} else if(~types.indexOf(`text/plain`)) {
			links = dataTransfer.getData(`text/plain`).split(`\n`).filter((e) => {
				return e.substr(0,4) === `http`
			})
		} else return
		links.forEach((url) => {
			const xhr = new XMLHttpRequest()
			xhr.onload = function() {
				let name
				if(/audio\//g.test(xhr.getResponseHeader(`Content-Type`))) {	// Load an audio waveform
					name = /([^\/]+)\/?$/g.exec(url)[1]
					that.loadAudioTrack(url,name)
					if(links.length === 1 && track)
						that.setAudioTrack(track.id,name)

				}else{ // Load a text track
					// If we don't supply a label, TextTrack will infer one from the URL,
					// so omit that parameter unless we want to fill it in with user input in the future
					TextTrack.get({
						src: url, // label: name,
						kind: `subtitles`, lang: `zxx`,
						success(track, mime) {
							addDroppedTrack.call(that,track,mime,void 0)
						},
					})
				}
			}
			xhr.open(`HEAD`, url, true)
			xhr.send(null)
		})
	}
}

function dragOver(ev) {
	ev.stopPropagation()
	ev.preventDefault()
	ev.dataTransfer.dropEffect = `copy`
}

function contextMenu(ev) {
	this.menuClick = true
	if(this.activeMenu) {
		this.activeMenu.parentNode.removeChild(this.activeMenu)
		this.activeMenu = null
	}
	this.showMenu({x: ev.offsetX || ev.layerX, y: ev.offsetY || ev.layerY})
	ev.preventDefault()
}

function Selection(tl,pos) {
	this.tl = tl
	this.dragged = false
	this.startPos = pos
}

Selection.prototype.mouseMove = function(npos) {
	const tl = this.tl,
		spos = this.startPos,
		ctx = tl.context

	this.dragged = true
	tl.restore()

	ctx.save()

	ctx.fillStyle = tl.colors.selectBox
	ctx.strokeStyle = tl.colors.selectBorder
	ctx.strokeWidth = 1

	ctx.beginPath()
	ctx.rect(	Math.min(spos.x,npos.x),
		Math.min(spos.y,npos.y),
		Math.abs(spos.x-npos.x),
		Math.abs(spos.y-npos.y))
	ctx.fill()
	ctx.stroke()

	ctx.restore()
}

Selection.prototype.mouseUp = function(epos) {
	const tl = this.tl,
		view = tl.view,
		spos = this.startPos

	this.menuClick = false

	tl.restore()
	if(!this.dragged) {
		(function() {
			const track = tl.trackFromPos(epos),
				seg = tl.segFromPos(epos)
			if(!track) return
			if(seg) seg.toggle()
			else track.clearSelection()
			tl.renderTrack(track)
		}())
	}else{
		(function() {
			let i, n,
				startTime = view.pixelToTime(Math.min(spos.x,epos.x)),
				endTime = view.pixelToTime(Math.max(spos.x,epos.x)),
				top = Math.min(spos.y,epos.y),
				bottom = Math.max(spos.y,epos.y),
				kheight = tl.keyHeight + tl.trackPadding,
				theight = tl.trackHeight + tl.trackPadding

			i = Math.max(0,top-kheight)
			i = Math[i%theight < tl.trackHeight?`floor`:`ceil`](i/theight)
			n = bottom >= tl.height - tl.sliderHeight
				?tl.tracks.length-1
				:Math.floor((bottom-kheight)/theight)
			tl.tracks.slice(i,n+1).forEach((track) => {
				track.visibleSegments.forEach((seg) => {
					if(seg.selected || seg.startTime >= endTime || seg.endTime <= startTime)
						return

					seg.selected = true
					tl.selectedSegments.push(seg)
					tl.emit(new Timeline.Event(`select`, {segments: [seg]}))
				})
			})
			tl.render()
		}())
	}
}

export default Timeline
