import React, { useState } from 'react'

import EditorWidgets from 'yvideo-editorwidgets'

import Dialog from './modalTools/Dialog'
import LoadTrackDataTemplate from './modalTools/ModalTemplates'

const LoadTrackData = ({ datalist, langList }) => {
	const [show, setShow] = useState(false)
	const [source, setSource] = useState(``)
	const [trackInfo, setTrackInfo] = useState({ trackName: ``, trackKind: `subtitles`, trackLang: [] })

	const sources = EditorWidgets.LocalFile.sources
	const buttons = [{event:`load`,label:`Load`}]
	const trackLang = []

	const changeSource = (event) => {
		setSource(event.target.value)
	}

	const changeTrackKind = (event) => {
		setTrackInfo({ trackName: trackInfo.trackName, trackKind: event.target.value, trackLang: trackInfo.trackLang })
	}

	const dialogBody = LoadTrackDataTemplate({ sources, langList, source, changeSource, changeTrackKind })

	const handleShow = () => {
		setShow(true)
	}

	const handleClose = () => {
		setShow(false)
	}

	const actions = {
		load: () => {
			handleClose()
			EditorWidgets.LocalFile(source, /.*\.(vtt|srt|ass|ttml|sub|sbv|lrc|stl)/, (fileObj) => {
				/*
				// If the label is omitted, it will be filled in with the file name stripped of extension
				// That's easier than doing the stripping here, so leave out that parameter unless we can
				// fill it with user input in the future
				resolver(datalist.map((key) => {
					switch(key){
					case 'tracksrc': return fileObj;
					case 'kind': return that.get("trackKind");
					case 'lang': return that.get("trackLang")[0];
					case 'location': return that.get("loadSource");
					case 'overwrite': return true;
					case 'handler':
						return function(trackp){
							trackp.then(function(track){
								track.mode = "showing";
								commandStack.setFileUnsaved(track.label);
							},function(_){
								alert("There was an error loading the track.");
							})
						}
					}
				}))
				*/
			})
		},
	}

	return (
		<Dialog show={show} handleShow={handleShow} handleClose={handleClose} actions={actions} dialogTitle='Load Track' dialogBody={dialogBody} buttons={buttons} />
	)
}

export default LoadTrackData

/*
// Load a track
let loadTrackData = (function(){
	let ractive, datalist, resolver, failer,
		sources = EditorWidgets.LocalFile.sources
	ractive = new Dialog({
		el: document.getElementById(`loadTrackModal`),
		data: {
			dialogTitle: `Load Track`,
			languages: langList,
			trackLang: [],
			trackKind: `subtitles`,
			modalId: `loadTrackModal`,
			defaultOption: {value:`zxx`,text:`No Linguistic Content`},
			sources: Object.keys(sources).map((key) => { return {name: key, label: sources[key].label}; }),
			buttons: [{event:`load`,label:`Load`}],
		},
		partials:{ dialogBody: document.getElementById(`loadTrackTemplate`).textContent },
		actions: {
			load(event){
							var that = this;
							$("#loadTrackModal").modal("hide");
							this.set({selectOpen: false});

							EditorWidgets.LocalFile(this.get("loadSource"),/.*\.(vtt|srt|ass|ttml|sub|sbv|lrc|stl)/,function(fileObj){
									//If the label is omitted, it will be filled in with the file name stripped of extension
									//That's easier than doing the stripping here, so leave out that parameter unless we can
									//fill it with user input in the future
									resolver(datalist.map(function(key){
											switch(key){
											case 'tracksrc': return fileObj;
											case 'kind': return that.get("trackKind");
											case 'lang': return that.get("trackLang")[0];
											case 'location': return that.get("loadSource");
											case 'overwrite': return true;
											case 'handler':
													return function(trackp){
															trackp.then(function(track){
																	track.mode = "showing";
																	commandStack.setFileUnsaved(track.label);
															},function(_){
																	alert("There was an error loading the track.");
															});
													};
											}
									}));
							});
					},
		},
	})

	return function(dl){
		$(`#loadTrackModal`).modal(`show`)
			datalist = dl
			return new Promise(((resolve, reject) => {
					resolver = resolve;
					failer = reject;
			}))
	};
}())

*/