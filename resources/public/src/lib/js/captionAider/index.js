import ContentRenderer from 'lib/js/contentRendering/ContentRenderer'
import { Ayamel, Translator, ResourceLibrary } from 'yvideojs'
import { CaptionEditor, Timeline } from 'yvideo-subtitle-timeline-editor'
import ContentLoader from '../contentRendering/ContentLoader'
import { CommandStack, Save } from 'yvideo-editorwidgets'

import {
	// EditTrackData,
	// GetLocation,
	// GetLocationNames,
	// LoadAudio,
	// LoadTrackData,
	// LoadTranscript,
	newTrackData,
	// newTrackData,
	// SaveTrackData,
	// ShowTrackData,
} from './modals'

export default class TrackEditor {

	captionEditor
	videoPlayer
	trackResources
	trackMimes
	timeline
	commandStack = new CommandStack()

	langList = Object.keys(Ayamel.utils.p1map).map(p1 => {
		const code = Ayamel.utils.p1map[p1],
			engname = Ayamel.utils.getLangName(code,`eng`),
			localname = Ayamel.utils.getLangName(code,code)

		return {
			value: code,
			text: engname,
			desc: localname !== engname ? localname : void 0,
		}
	})

	constructor(content, contentHolder, toggleModal) {

		this.langList.push({ value: `apc`, text: `North Levantine Arabic`})
		this.langList.push({ value: `arz`, text: `Egyptian Arabic`})
		this.langList.sort((a,b) => a.text.localeCompare(b.text))

		this.content = content

		this.content.settings = {
			...content.settings,
			level: 2,
			enabledCaptionTracks: content.settings.enabledCaptionTracks,
			showTranscripts: `true`,
		}

		this.captionEditor = CaptionEditor({
			stack: this.commandStack,
			refresh(){
				this.videoPlayer.refreshLayout()
			},
			rebuild(){
				this.videoPlayer.rebuildCaptions()
			},
			timeline: this.timeline,
		})

		ContentLoader.castContentObject(content).then(content => {
			return ResourceLibrary.load(content.resourceId).then(resource => {
				content.settings.showCaptions = `true`
				return {
					content,
					resource,
					contentId: content.id,
					holder: contentHolder,
					permission: `edit`,
					screenAdaption: {
						fit: true,
						scroll: true,
						padding: 61,
					},
					startTime: 0,
					endTime: -1,
					renderCue: (renderedCue, renderFunc) => this.captionEditor.make(renderedCue, renderFunc),
					// noUpdate: true, // Disable transcript player updating for now
					callback: this.callback,
				}
			})
		}).then(ContentRenderer.render)

		this.toggleModal = toggleModal
	}

	updateSpacing() {
		// document.getElementById(`bottomSpacer`).style.marginTop = `${document.getElementById(`bottomContainer`).clientHeight}px`
		// $(`html,body`).animate({scrollTop: document.body.scrollHeight - window.innerHeight}, 1000,`swing`)
	}

	loadTranscript(_datalist){
		return new Promise(resolve => {
			const f = document.createElement(`input`)
			f.type = `file`
			f.addEventListener(`change`,(evt) => {
				resolve([evt.target.files[0]])
			})
			f.click()
		})
	}

	loadAudio(datalist){
		return new Promise((resolve, reject) => {
			const f = document.createElement(`input`)
			f.type = `file`

			f.addEventListener(`change`, evt => {

				const f = evt.target.files[0]

				resolve(datalist.map(key => {
					switch(key){
					case `audiosrc`:
						return f
					case `name`:
						return f.name
					default:
						reject()
						return null
					}
				}))

			})

			f.click()
		})
	}

	// Set Save Location
	/*
	getLocation = (function(){
		let ractive, datalist, resolver,
			targets = EditorWidgets.Save.targets
		ractive = new Dialog({
			el: document.getElementById(`setLocModal`),
			data: {
				dialogTitle: `Set Save Location`,
				saveLocations: Object.keys(targets).map((key) => {
					return {
						value: key,
						name: targets[key].label,
					}
				}), saveLocation: `server`,
				buttons: [{event:`save`,label:`Set Location`}],
			},
			partials:{ dialogBody: document.getElementById(`setLocTemplate`).textContent },
			actions: {
				save(event){
					const that = this
					$(`#setLocModal`).modal(`hide`)
					resolver(datalist.map((key) => {
						return key === `location`?that.get(`saveLocation`):void 0
					}))
				},
			},
		})

		return function(dl){
			$(`#setLocModal`).modal(`show`)
			datalist = dl
			return new Promise((resolve, reject) => {
				resolver = resolve
			})
		}
	}())
*/

	getLocationNames = async datalist => {
		const names = {
			server: `Server`,
		}
		const targets = Save.targets
		Object.keys(targets).forEach(key => {
			names[key] = targets[key].label
		})
		return datalist.map(key => key === `names` ? names : void 0)
	}

	callback = args => {
		this.trackResources = args.trackResources

		this.translator = new Translator()
		this.commandStack = new CommandStack()
		this.trackMimes = args.trackMimes
		this.videoPlayer = args.mainPlayer

		this.timeline = new Timeline(document.getElementById(`timeline`), {
			stack: this.commandStack,
			syncWith: this.videoPlayer,
			saveLocation: `server`,
			dropLocation: `file`,
			width: document.body.clientWidth - 100 || window.innerWidth - 100,
			length: 3600, start: 0, end: 240,
			trackMode: `showing`,
			tool: Timeline.SELECT,
			showControls: true,
			canGetFor: (whatfor) => {
				switch(whatfor){
				case `newtrack`:
				case `edittrack`:
				case `savetrack`:
				case `loadtrack`:
				case `showtrack`:
				case `loadlines`:
				case `loadaudio`:
				case `location`:
				case `locationNames`:
					return true
				default:
					return false
				}
			},
			getFor: async (whatfor, datalist) => {
				switch(whatfor) {
				case `newtrack`: return newTrackData(datalist, this.toggleModal)
				// case `edittrack`: return editTrackData(datalist)
				// case `savetrack`: return saveTrackData(datalist)
				// case `loadtrack`: return loadTrackData(datalist)
				// case `showtrack`: return showTrackData(datalist)
				case `loadlines`: return this.loadTranscript(datalist)
				case `loadaudio`: return this.loadAudio(datalist)
				// case `location`: return getLocation(datalist)
				case `locationNames`: return this.getLocationNames(datalist)
				default:
					return Promise.reject(new Error(`Can't get data for ${whatfor}`))
				}
			},
		})

		this.captionEditor = CaptionEditor({
			stack: this.commandStack,
			refresh(){
				this.videoPlayer.refreshLayout()
			},
			rebuild(){
				this.videoPlayer.rebuildCaptions()
			},
			timeline: this.timeline,
		})

		// Check for unsaved tracks before leaving
		window.addEventListener(`beforeunload`,(e) => {
			const warning = `You have unsaved tracks. Your unsaved changes will be lost.`
			if(!this.commandStack.isSavedAt(this.timeline.saveLocation)){
				e.returnValue = warning
				return warning
			}
		}, false)

		window.addEventListener(`resize`,() => {
			this.timeline.width = window.innerWidth - 100
		}, false)

		// Preload tracks into the editor
		this.videoPlayer.addEventListener(`addtexttrack`, (event) => {
			this.track = event.detail.track
			if(this.timeline.hasCachedTextTrack(this.track)) return
			this.timeline.cacheTextTrack(this.track, this.trackMimes.get(this.track), `server`)
		})

		// EVENT TRACK EDITOR event listeners
		this.timeline.on(`select`, (selected) => {
			selected.segments[0].makeEventTrackEditor(selected.segments[0].cue, this.videoPlayer)
		})

		this.timeline.on(`unselect`, (deselected) => {
			deselected.segments[0].destroyEventTrackEditor()
		})

		// Auto delete eventTrackEditor when track is deleted
		this.timeline.on(`delete`, (deleted) => {
			deleted.segments[0].destroyEventTrackEditor()
		})

		// keep the editor and the player menu in sync
		this.timeline.on(`altertrack`, () => {
			this.videoPlayer.refreshCaptionMenu()
		})

		// TODO: Integrate the next listener into the timeline editor
		this.timeline.on(`activechange`, () => {
			this.videoPlayer.rebuildCaptions()
		})

		this.timeline.on(`cuechange`, (evt) => {
			if(evt.fields.indexOf(`text`) === -1) return
		})

		this.timeline.on(`addtrack`,(evt) => {
			this.videoPlayer.addTextTrack(evt.track.textTrack)
			this.updateSpacing()
		})

		this.timeline.on(`removetrack`, (evt) => {
			this.updateSpacing()
		})

		this.timeline.addMenuItem([`Track`,`Clone`, `Clone with Translation`], {
			name:`Clone with Translation`,
			action() {
				const trackId = this.track.id
				this.timeline.getFor(
					`newtrack`,
					[`kind`,`lang`,`name`,`mime`,`overwrite`],
					{
						kind: void 0,
						lang: void 0,
						mime: void 0,
						overwrite: false,
					},
				).then(values => {
					this.timeline.cloneTrack(
						trackId,
						{
							kind: values[0],
							lang: values[1],
							name: values[2],
							mime: values[3],
						},
						(cue, ott, ntt, mime) => {
							const txt = Ayamel.utils.extractPlainText(cue.getCueAsHTML())

							if(ott.language === ntt.language)
								return txt

							return this.translator.translate({
								srcLang: ott.language,
								destLang: ntt.language,
								text: txt,
							}).then((data) => {
								return data.translations[0].text
							}).catch(() => {
								return txt
							})
						},
						values[4],
					)
				})
			},
		})
	}
}

// const getCaptionAider = async (content, resource, contentHolder, toggleModal) => {

// 	// eslint-disable-next-line no-unused-vars
// 	let trackResources
// 	let captionEditor
// 	let track
// 	let timeline

// 	const langList = Object.keys(Ayamel.utils.p1map).map(p1 => {
// 		const code = Ayamel.utils.p1map[p1],
// 			engname = Ayamel.utils.getLangName(code,`eng`),
// 			localname = Ayamel.utils.getLangName(code,code)

// 		return {
// 			value: code,
// 			text: engname,
// 			desc: localname !== engname ? localname : void 0,
// 		}
// 	})

// 	const saveDiv = document.createElement(`div`)
// 	const saveImg = new Image()
// 	const saveText = document.createElement(`p`)

// 	saveDiv.setAttribute(`id`, `saveDiv`)
// 	saveImg.setAttribute(`id`, `saveImg`)
// 	saveText.setAttribute(`id`, `saveText`)
// 	saveImg.style.cssText = `width:25px; height:25px; margin-right:15px; margin-top:-3px;`
// 	saveText.style.display = `inline`

// 	saveDiv.appendChild(saveImg)
// 	saveDiv.appendChild(saveText)
// 	document.body.appendChild(saveDiv)

// 	function renderCue(renderedCue, renderFunc) {
// 		return captionEditor.make(renderedCue, renderFunc)
// 	}

// 	// Render the content

// 	content.settings = {
// 		...content.settings,
// 		level: 2,
// 		enabledCaptionTracks: content.settings.enabledCaptionTracks,
// 		showTranscripts: `true`,
// 	}

// 	function updateSpacing() {
// 		// document.getElementById(`bottomSpacer`).style.marginTop = `${document.getElementById(`bottomContainer`).clientHeight}px`
// 		// $(`html,body`).animate({scrollTop: document.body.scrollHeight - window.innerHeight}, 1000,`swing`)
// 	}

// 	function loadTranscript(datalist){
// 		return new Promise(resolve => {
// 			const f = document.createElement(`input`)
// 			f.type = `file`
// 			f.addEventListener(`change`,(evt) => {
// 				resolve([evt.target.files[0]])
// 			})
// 			f.click()
// 		})
// 	}

// 	function loadAudio(datalist){
// 		return new Promise((resolve, reject) => {
// 			const f = document.createElement(`input`)
// 			f.type = `file`
// 			f.addEventListener(`change`, evt => {
// 				const f = evt.target.files[0]
// 				resolve(datalist.map(key => {
// 					switch(key){
// 					case `audiosrc`:
// 						return f
// 					case `name`:
// 						return f.name
// 					default:
// 						reject()
// 						return null
// 					}
// 				}))
// 			})
// 			f.click()
// 		})
// 	}

// 	// Set Save Location
// 	/*
// 	const getLocation = (function(){
// 		let ractive, datalist, resolver,
// 			targets = EditorWidgets.Save.targets
// 		ractive = new Dialog({
// 			el: document.getElementById(`setLocModal`),
// 			data: {
// 				dialogTitle: `Set Save Location`,
// 				saveLocations: Object.keys(targets).map((key) => {
// 					return {
// 						value: key,
// 						name: targets[key].label,
// 					}
// 				}), saveLocation: `server`,
// 				buttons: [{event:`save`,label:`Set Location`}],
// 			},
// 			partials:{ dialogBody: document.getElementById(`setLocTemplate`).textContent },
// 			actions: {
// 				save(event){
// 					const that = this
// 					$(`#setLocModal`).modal(`hide`)
// 					resolver(datalist.map((key) => {
// 						return key === `location`?that.get(`saveLocation`):void 0
// 					}))
// 				},
// 			},
// 		})

// 		return function(dl){
// 			$(`#setLocModal`).modal(`show`)
// 			datalist = dl
// 			return new Promise((resolve, reject) => {
// 				resolver = resolve
// 			})
// 		}
// 	}())
// */

// 	async function getLocationNames(datalist){
// 		const names = {
// 			server: `Server`,
// 		}
// 		const targets = Save.targets
// 		Object.keys(targets).forEach(key => {
// 			names[key] = targets[key].label
// 		})
// 		return datalist.map(key => key === `names` ? names : void 0)
// 	}

// 	const getFor = async (whatfor, datalist) => {
// 		switch(whatfor) {
// 		case `newtrack`: return newTrackData(datalist, toggleModal)
// 		// case `edittrack`: return editTrackData(datalist)
// 		// case `savetrack`: return saveTrackData(datalist)
// 		// case `loadtrack`: return loadTrackData(datalist)
// 		// case `showtrack`: return showTrackData(datalist)
// 		case `loadlines`: return loadTranscript(datalist)
// 		case `loadaudio`: return loadAudio(datalist)
// 		// case `location`: return getLocation(datalist)
// 		case `locationNames`: return getLocationNames(datalist)
// 		default:
// 			return Promise.reject(new Error(`Can't get data for ${whatfor}`))
// 		}
// 	}

// 	function canGetFor(whatfor){
// 		switch(whatfor){
// 		case `newtrack`:
// 		case `edittrack`:
// 		case `savetrack`:
// 		case `loadtrack`:
// 		case `showtrack`:
// 		case `loadlines`:
// 		case `loadaudio`:
// 		case `location`:
// 		case `locationNames`:
// 			return true
// 		default:
// 			return false
// 		}
// 	}

// 	ContentLoader.castContentObject(content).then(content => {
// 		return ResourceLibrary.load(content.resourceId).then(resource => {
// 			content.settings.showCaptions = `true`
// 			return {
// 				content,
// 				resource,
// 				contentId: content.id,
// 				holder: contentHolder,
// 				permission: `edit`,
// 				screenAdaption: {
// 					fit: true,
// 					scroll: true,
// 					padding: 61,
// 				},
// 				startTime: 0,
// 				endTime: -1,
// 				renderCue,
// 				// noUpdate: true, // Disable transcript player updating for now
// 				callback,
// 			}
// 		})
// 	}).then(ContentRenderer.render)

// 	const callback = args => {
// 		trackResources = args.trackResources

// 		const translator = new Translator()
// 		const commandStack = new CommandStack()
// 		const trackMimes = args.trackMimes
// 		const videoPlayer = args.mainPlayer

// 		timeline = new Timeline(document.getElementById(`timeline`), {
// 			stack: commandStack,
// 			syncWith: videoPlayer,
// 			saveLocation: `server`,
// 			dropLocation: `file`,
// 			width: document.body.clientWidth || window.innerWidth,
// 			length: 3600, start: 0, end: 240,
// 			trackMode: `showing`,
// 			tool: Timeline.SELECT,
// 			showControls: true,
// 			canGetFor,
// 			getFor,
// 		})

// 		updateSpacing()

// 		captionEditor = CaptionEditor({
// 			stack: commandStack,
// 			refresh(){
// 				videoPlayer.refreshLayout()
// 			},
// 			rebuild(){
// 				videoPlayer.rebuildCaptions()
// 			},
// 			timeline,
// 		})

// 		// Check for unsaved tracks before leaving
// 		window.addEventListener(`beforeunload`,(e) => {
// 			const warning = `You have unsaved tracks. Your unsaved changes will be lost.`
// 			if(!commandStack.isSavedAt(timeline.saveLocation)){
// 				e.returnValue = warning
// 				return warning
// 			}
// 		}, false)

// 		window.addEventListener(`resize`,() => {
// 			timeline.width = window.innerWidth
// 		}, false)

// 		// Preload tracks into the editor
// 		videoPlayer.addEventListener(`addtexttrack`, (event) => {
// 			track = event.detail.track
// 			if(timeline.hasCachedTextTrack(track)) return
// 			timeline.cacheTextTrack(track,trackMimes.get(track),`server`)
// 		})

// 		// EVENT TRACK EDITOR event listeners
// 		timeline.on(`select`, (selected) => {
// 			selected.segments[0].makeEventTrackEditor(selected.segments[0].cue, videoPlayer)
// 		})

// 		timeline.on(`unselect`, (deselected) => {
// 			deselected.segments[0].destroyEventTrackEditor()
// 		})

// 		// Auto delete eventTrackEditor when track is deleted
// 		timeline.on(`delete`, (deleted) => {
// 			deleted.segments[0].destroyEventTrackEditor()
// 		})

// 		// keep the editor and the player menu in sync
// 		timeline.on(`altertrack`, () => {
// 			videoPlayer.refreshCaptionMenu()
// 		})

// 		// TODO: Integrate the next listener into the timeline editor
// 		timeline.on(`activechange`, () => {
// 			videoPlayer.rebuildCaptions()
// 		})

// 		timeline.on(`cuechange`, (evt) => {
// 			if(evt.fields.indexOf(`text`) === -1) return
// 		})

// 		timeline.on(`addtrack`,(evt) => {
// 			videoPlayer.addTextTrack(evt.track.textTrack)
// 			updateSpacing()
// 		})

// 		timeline.on(`removetrack`, (evt) => {
// 			updateSpacing()
// 		})

// 		timeline.addMenuItem([`Track`,`Clone`, `Clone with Translation`], {
// 			name:`Clone with Translation`,
// 			action() {
// 				const trackId = track.id
// 				timeline.getFor(
// 					`newtrack`,
// 					[`kind`,`lang`,`name`,`mime`,`overwrite`],
// 					{
// 						kind: void 0,
// 						lang: void 0,
// 						mime: void 0,
// 						overwrite: false,
// 					},
// 				).then(values => {
// 					console.log(`values`, values)
// 					timeline.cloneTrack(
// 						trackId,
// 						{
// 							kind: values[0],
// 							lang: values[1],
// 							name: values[2],
// 							mime: values[3],
// 						},
// 						(cue, ott, ntt, mime) => {
// 							const txt = Ayamel.utils.extractPlainText(cue.getCueAsHTML())

// 							if(ott.language === ntt.language)
// 								return txt

// 							return translator.translate({
// 								srcLang: ott.language,
// 								destLang: ntt.language,
// 								text: txt,
// 							}).then((data) => {
// 								return data.translations[0].text
// 							}).catch(() => {
// 								return txt
// 							})
// 						},
// 						values[4],
// 					)
// 				})
// 			},
// 		})
// 	}
// }

// export default getCaptionAider
