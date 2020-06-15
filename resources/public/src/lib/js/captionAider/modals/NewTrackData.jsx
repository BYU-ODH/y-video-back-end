// import React from 'react'

import { Ayamel } from 'yvideojs'
import { TimedText } from 'yvideo-timedtext'

import Dialog from './modalTools/Dialog'

/**
 * Create a new Track from scratch
 */
const newTrackData = function (datalist, toggleModal) {

	const types = TimedText.getRegisteredTypes().map((mime) => {
		return {name: TimedText.getTypeInfo(mime).name, mime}
	})

	const langList = Object.keys(Ayamel.utils.p1map)
		.map((p1) => {
			const code = Ayamel.utils.p1map[p1]
			const engname = Ayamel.utils.getLangName(code, `eng`)
			const localname = Ayamel.utils.getLangName(code, code)

			return {
				value: code,
				text: engname,
				desc: localname !== engname
					? localname
					: void 0}
		})

	const viewstate = {
		dialogTitle: `Create a new track`,
		languages: langList,
		trackLang: [],
		trackKind: `subtitles`,
		trackName: ``,
		trackMime: `text/vtt`,
		types,
		modalId: `newTrackModal`,
		buttons: [{event:`create`,label:`Create`}],
		defaultOption: {value:`zxx`,text:`No Linguistic Content`},
		selectOpen: false,
	}

	let resolver

	const handlers = {
		create: state => () => {

			toggleModal()

			resolver(datalist.map(key => {
				switch(key){
				case `kind`:
					return state[`trackKind`]
				case `name`:
					return state[`trackName`] || `Untitled`
				case `lang`:
					return state[`trackLang`][0]
				case `mime`:
					return state[`trackMime`]
				case `overwrite`:
					return true
				case `handler`:
					return function(tp){
						tp.then((track) => {
							track.mode = `showing`
						})
					}
				default:
					return undefined
				}
			}))
		},
	}

	toggleModal({
		component: Dialog,
		props: {
			viewstate,
			handlers,
		},
	})

	return new Promise(resolve => {
		resolver = resolve
	})
}

export default newTrackData

// const newTrackData = async (datalist, toggleModal) => {

// 	const types = TimedText.getRegisteredTypes()
// 		.map(mime => ({
// 			name: TimedText.getTypeInfo(mime).name,
// 			mime,
// 		}))

// 	const langList = Object.keys(Ayamel.utils.p1map)
// 		.map((p1) => {
// 			const code = Ayamel.utils.p1map[p1]
// 			const engname = Ayamel.utils.getLangName(code, `eng`)
// 			const localname = Ayamel.utils.getLangName(code, code)

// 			return {
// 				value: code,
// 				text: engname,
// 				desc: localname !== engname
// 					? localname
// 					: void 0}
// 		})

// 	const viewstate = {
// 		dialogTitle: `Create a new track`,
// 		languages: langList,
// 		trackLang: [],
// 		trackKind: `subtitles`,
// 		trackName: ``,
// 		trackMime: `text/vtt`,
// 		types,
// 		modalId: `newTrackModal`,
// 		buttons: [{event:`create`,label:`Create`}],
// 		defaultOption: {value:`zxx`,text:`No Linguistic Content`},
// 		selectOpen: false,
// 	}

// 	let resolver

// 	const handlers = {
// 		create: () => {

// 			toggleModal()

// 			console.log(`newTrackData:57 - datalist: `, datalist)

// 			resolver(datalist.map(key => {
// 				switch(key){
// 				case `kind`: return viewstate.trackKind
// 				case `name`: return viewstate.trackName || `Untitled`
// 				case `lang`: return viewstate.trackLang[0]
// 				case `mime`: return viewstate.trackMime
// 				case `overwrite`: return true
// 				case `handler`:
// 					return function(tp){
// 						tp.then((track) => {
// 							track.mode = `showing`
// 						})
// 					}
// 				default:
// 					return null
// 				}
// 			}))
// 		},
// 	}

// 	const component = <Dialog viewstate={viewstate} handlers={handlers} />

// 	toggleModal({
// 		component,
// 	})

// 	console.log(`newTrackData:85 - resolver: `, resolver)
// 	return resolver
// }

// export default newTrackData

