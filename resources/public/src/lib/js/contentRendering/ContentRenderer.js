import { AyamelPlayer, Translator, LangUtils } from 'yvideojs'
import xApi from '../xApi.js'
import ContentLoader from './ContentLoader.js'
import ContentCache from './ContentCache.js'
import TranscriptPlayer from './TranscriptPlayer.js'
import EditorWidgets from 'yvideo-editorwidgets'
import axios from 'axios'

const YLex = {}

const ContentRenderer = {

	/* args: resource, content, courseId, contentId, holder, components,
			screenAdaption, startTime, endTime, renderCue, permission, callback */

	render: async args => {

		// Temporary hack to show current source
		// const srcUrlEl = document.getElementById(`sourceUrl`)

		// if (srcUrlEl) {
		// 	const srcFile = args.resource.content.files[0]
		// 	srcUrlEl.value = srcFile.downloadUri || srcFile.streamUri
		// }

		const container = document.createElement(`div`)

		container.id = `player`

		args.holder.appendChild(container)

		// Set up the video player
		const params = {
			content: args.content,
			components: args.components,
			screenAdaption: args.screenAdaption,
			resource: args.resource,
			startTime: args.startTime,
			endTime: args.endTime,
			renderCue: args.renderCue,
			holder: container,
			translate: allowDefinitions(args.content),
			transcripts: showCaptions(args.content) || showTranscript(args.content) ?
				await ContentLoader.getTranscriptWhitelist({
					resource: args.resource,
					courseId: args.courseId,
					contentId: args.contentId,
					permission: args.permission,
				}) : [],
			annotations:
				showAnnotations(args.content) ?
					ContentLoader.getAnnotationWhitelist({
						resource: args.resource,
						courseId: args.courseId,
						contentId: args.contentId,
						permission: args.permission,
					}) : [],
			callback: args.callback,
		}

		const player = setupMainPlayer(params)

		player.then(() => {
			// Resize the panes' content to be correct size onload
			window.dispatchEvent(new Event(`resize`, { bubbles: true }))
		})

		// Handle thumbnail making
		document.addEventListener(`makeThumbnail`, e => {
			e.stopPropagation()
			document.getElementById(`makeThumbnail`).dispatchEvent(new CustomEvent(`timeUpdate`, {
				bubbles: true,
				detail: { currentTime: player.currentTime },
			}))
		}, false)
	},
}

const showTranscript = content => {
	return content.settings.showTranscripts === `true`
}

const showCaptions = content => {
	return content.settings.showCaptions === `true`
}

const showAnnotations = content => {
	return content.settings.showAnnotations === `true`
}

const allowDefinitions = content => {
	return content.settings.allowDefinitions === `true`
}

const showWordList = content => {
	return content.settings.showWordList === `true`
}

// const getDefaultLanguage = languages => {
// 	for (const langObj in languages) {
// 		if (langObj.value === `eng`)
// 			return langObj
// 	}
// 	return languages[0]
// }

const setupTranslatorPane = (tab, player, content, resourceMap) => {
	let codes = (content.settings.targetLanguages || ``)
		.split(`,`).filter((s) => {
			return !!s
		})
	const selectHolder = document.createElement(`div`),
		translationsHolder = document.createElement(`div`)

	// Fallback procedure when no languages have been selected
	if (!codes.length) {
		codes = Object.keys(LangUtils.p1map)
			.map((p1) => {
				return LangUtils.p1map[p1]
			})
	}

	const targetLanguages = codes.map((code) => {
		return { value: code, text: LangUtils.getLangName(code) }
	}).sort((a, b) => {
		return a.text.localeCompare(b.text)
	})

	translationsHolder.className = `definitionsContent`
	new EditorWidgets.SuperSelect({
		el: selectHolder,
		id: `transLang`,
		value: [
			codes.indexOf(`eng`) !== -1 ?
				`eng` : codes[0],
		],
		icon: `icon-globe`,
		button: `left`,
		text: `Select Language`,
		options: targetLanguages,
		multiple: false,
	}).addEventListener(`valuechange`, () => {
		player.targetLang = this.value[0]
	})

	// Player Event Listeners

	// Translation started
	player.addEventListener(`translate`, event => {
		const detail = event.detail,
			data = detail.data
		let activityType
		switch (data.sourceType) {
		case `caption`: activityType = `captionTranslation`
			break
		case `transcript`: activityType = `transcriptionTranslation`
			break
		default: return
		}
		xApi.predefined[activityType](resourceMap.get(data.cue.track).id, data.cue.id, detail.text)
		player.pause()
	})

	// Translation succeeded
	player.addEventListener(`translation`, event => {

		let translationSize
		const detail = event.detail,
			// translations = detail.translations,
			wordList = `<div class='addToWordList'><button class='btn btn-small'><i class='icon-paste'></i> Add to Word List</button></div>`,
			html = document.createElement(`div`)
		html.innerHTML = YLex.renderResult(event.detail) + wordList
		translationsHolder.appendChild(html)
		tab.select()

		if (wordList !== ``) {
			html.querySelector(`button`).addEventListener(`click`, () => {
				const addWord = this.parentNode
				const Data = new FormData()
				Data.append(`srcLang`, detail.src)
				Data.append(`destLang`, detail.dst)
				Data.append(`word`, detail.text)
				axios(`${process.env.REACT_APP_YVIDEO_SERVER}/words`, {
					method: `post`,
					cache: false,
					contentType: false,
					processData: false,

					data: Data,
					success() {
						addWord.innerHTML = `<span class='color-blue'>Added to word list.</span>`
					},
					error() {
						alert(`Error adding to word list`)
						addWord.parentNode.removeChild(addWord)
					},
				})
			})
		}

		// keep the top of the new translation visible.
		if (html.offsetHeight > translationsHolder.offsetHeight) {
			translationSize = html.offsetHeight + html.querySelector(`.sourceText`).offsetHeight + 15
			translationsHolder.scrollTop = translationsHolder.scrollHeight - translationSize
		} else
			translationsHolder.scrollTop = translationsHolder.scrollHeight

	})

	// Handle errors
	player.addEventListener(`translationError`, event => {
		alert(`We couldn't translate '${event.detail.text}' for you.`)
	})

	const pane = document.createElement(`div`)

	pane.className = `definitionsContainer`
	pane.appendChild(selectHolder)
	pane.appendChild(document.createElement(`hr`))
	pane.appendChild(translationsHolder)
	return pane
}

const setupTranscriptPane = (tab, player, content, trackResources, trackMimes) => {
	const DOM = document.createDocumentFragment(),
		transcriptPlayer = new TranscriptPlayer({
			// requires the actual TextTrack objects; should be fixed up to take resource IDs, I think
			captionTracks: [],
			holder: DOM,
			sync: true,
			annotator: null,
		})

	// Cue clicking
	transcriptPlayer.addEventListener(`cueclick`, event => {
		const trackID = trackResources.get(event.detail.track).id

		player.currentTime = event.detail.cue.startTime
		xApi.predefined.transcriptCueClick(trackID, event.detail.cue.id)
	})

	player.addEventListener(`timeupdate`, () => {
		transcriptPlayer.currentTime = player.currentTime
	})

	player.addEventListener(`addtexttrack`, event => {
		const track = event.detail.track
		transcriptPlayer.addTrack(track)
		trackResources.set(track, event.detail.resource)
		trackMimes.set(track, event.detail.mime)
	}, false)

	player.addEventListener(`enableannset`, () => {
		transcriptPlayer.update()
	})

	player.addEventListener(`disableannset`, () => {
		transcriptPlayer.update()
	})

	player.mediaPlayer.promise.then(() => {
		transcriptPlayer.setAnnotator(player.mediaPlayer.annotator)
		transcriptPlayer.update()
	})

	return DOM
}

const setupAnnotatorPane = (tab, player) => {
	const display = document.createElement(`div`)

	let data = null,
		newplayer = null

	display.style.overflow = `scroll`
	display.style.height = `100%`
	player.addEventListener(`annotation`, (event) => {
		player.pause()

		if (data !== event.detail.data) {
			data = event.detail.data
			if (newplayer) {
				newplayer.destroy()
				newplayer = null
			}

			switch (event.detail.data.type) {
			case `text`:
				display.innerHTML = event.detail.data.value;
				[].forEach.call(display.querySelectorAll(`a`), (link) => {
					link.target = `_blank`
				})
				break
			case `image`:
				display.innerHTML = `<img src='${event.detail.data.value}'>`
				break
			case `content`:
				ContentCache.load(event.detail.data.value, content => {
					display.innerHTML = ``
					// Don't allow annotations, transcriptions, or certain controls
					content.settings.showTranscripts = `false`
					content.settings.showAnnotations = `false`
					content.settings.allowDefinitions = `false`

					ContentLoader.render({
						content,
						holder: display,
						annotate: false,
						screenAdaption: {
							fit: false,
						},
						aspectRatio: window.Ayamel.aspectRatios.hdVideo,
						components: {
							left: [`play`],
							right: [`captions`, `timeCode`],
						},
						callback(args) {
							newplayer = args.mainPlayer
						},
					})
				})
				break
			default: break
			}
		}
		// Find the annotation doc
		const annotationDocId = `Unknown`
		xApi.predefined.viewTextAnnotation(annotationDocId, event.detail.text)

		tab.select()
	})

	return display
}

const setupWordListPane = (tab, player) => {
	const display = document.createElement(`div`)
	axios(`${process.env.REACT_APP_YVIDEO_SERVER}/wordList`)
	// , { withCredentials: true, mode: `cors` }
		.then(res => res.json()).then(json => {
			json.wordlist.forEach(myWord => {
				const element = document.createElement(`p`)
				// element.textContent = JSON.stringify(myWord.word);
				element.textContent = myWord.word
				display.appendChild(element)
			})
		}).catch(error => console.error(`Failed to get wordlist.`, error))

	return display
}

/* args: components, transcripts, content, screenAdaption, holder, resource,
		startTime, endTime, renderCue, annotator, translate */
const setupMainPlayer = args => {
	// eslint-disable-next-line prefer-const
	let player
	const content = args.content,
		trackResources = new Map(),
		trackMimes = new Map(),
		transcriptPlayer = null,
		components = args.components || {
			left: [`play`, `lastCaption`, `volume`, `captions`, `annotations`],
			right: [`rate`, `fullScreen`, `sideToggle`, `timeCode`],
		}

	if (!showCaptions(args.content)) {
		[`left`, `right`].forEach(side => {
			[`lastCaption`, `captions`, `annotations`].forEach(control => {
				const index = components[side].indexOf(control)
				if (~index) components[side].splice(index, 1)
			})
		})
	} else if (!showAnnotations(args.content)) {
		[`left`, `right`].forEach((side) => {
			const index = components[side].indexOf(`annotations`)
			if (~index) components[side].splice(index, 1)
		})
	}

	// Set the priority of players
	window.Ayamel.prioritizedPlugins.video = [`html5`, `flash`, `brightcove`, `youtube`, `vimeo`, `ooyala`]
	window.Ayamel.prioritizedPlugins.audio = [`html5`]

	// padding to account for the control bar
	// TODO: Dynamically check the actual control bar height
	args.holder.style.paddingBottom = `61px`

	// Spacebar to play/pause video
	window.addEventListener(`keydown`, e => {
		if (e.keyCode === 32 && document.querySelectorAll(`input:focus, textarea:focus`).length === 0) {
			player[player.paused ? `play` : `pause`]()
			e.preventDefault()
		}
	})

	window.addEventListener(window.Ayamel.utils.FullScreen.fullScreenEvent, event => {
		if (window.Ayamel.utils.FullScreen.isFullScreen) {
			// Need to remove the player's css so that the fullscreen can resize properly
			player.element.style.removeProperty(`width`)
			player.element.style.removeProperty(`height`)
		} else {
			// call a resize event so that css can be put back
			window.dispatchEvent(new Event(`resize`, { bubbles: true, cancelable: true }))
		}
	}, false)

	window.addEventListener(`resize`, event => {
		if (!args.screenAdaption || !args.screenAdaption.fit) return
		player.maxHeight = document.documentElement.clientHeight
	}, false)

	const tabs = []
	if (allowDefinitions(content)) {
		tabs.push({
			title: `Definitions`,
			content(tab, player) {
				return setupTranslatorPane(tab, player, content, trackResources)
			},
		})
	}

	if (args.transcripts.length && showTranscript(content)) {
		tabs.push({
			title: `Transcripts`,
			content(tab, player) {
				return setupTranscriptPane(tab, player, content, trackResources, trackMimes)
			},
		})
	}

	if (args.annotations.length) {
		tabs.push({
			title: `Annotations`,
			content: setupAnnotatorPane,
		})
	}

	if (showWordList(content)) {
		tabs.push({
			title: `Word List`,
			content: setupWordListPane,
		})
	}

	player = new AyamelPlayer({
		components,
		holder: args.holder,
		resource: args.resource,
		captions: {
			renderCue: args.renderCue,
			whitelist: args.transcripts,
		},
		annotations: {
			classList: [`annotation`],
			whitelist: args.annotations,
		},
		translator: args.translate ? {
			endpoint: Translator.endpoint,
			key: Translator.key,
			targetLang: `eng`,
		} : null,
		startTime: args.startTime,
		endTime: args.endTime,
		translate: args.translate,
		aspectRatio: parseFloat(args.content.settings.aspectRatio) || window.Ayamel.aspectRatios.hdVideo,
		tabs,
	})

	player.addEventListener(`play`, () => {
		player.restoreTabs()
	}, false)

	if (typeof args.callback === `function`) {
		setTimeout(() => {
			args.callback({
				mainPlayer: player,
				transcriptPlayer,
				trackResources,
				trackMimes,
			})
		}, 1)
	}

	return player
}

export default ContentRenderer