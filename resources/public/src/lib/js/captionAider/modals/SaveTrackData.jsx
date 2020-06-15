import React from 'react'

const SaveTrackData = () => {
	return (
		<div>

		</div>
	)
}

export default SaveTrackData

/*
// Save Tracks
let saveTrackData = (function(){
	let ractive, datalist, resolver, failer, saver,
		availableTracks=[], stracks=[]
	ractive = new Dialog({
		el: document.getElementById(`saveTrackModal`),
		data: {
			dialogTitle: `Save Tracks`,
			tracks: availableTracks,
			selectedTracks: stracks,
			modalId: `saveTrackModal`,
			buttons: [{event:`save`,label:`Save`}],
		},
		partials:{ dialogBody: document.getElementById(`saveTrackTemplate`).textContent },
		actions: {
			save(event){
							var stracks = this.get("selectedTracks");
							var tracks = stracks;

							$("#saveTrackModal").modal("hide");
							this.set({selectOpen: false});
							if(!tracks.length) {
									failer(new Error('Cancel Save'));
									return;
							}

							resolver(datalist.map(function(key){
									switch(key){
									case 'tidlist': return stracks;
									case 'location': return timeline.saveLocation;
									case 'saver': return function(listp){ return listp.then(saver); };
									}
							}));
					},
		},
	})
	// Saving modal opening
	$(`#saveTrackModal`).on(`shown.bs.modal`, () => {

			// We do this because ractive can't seem to update correctly with partials
			// even when we use ractive.set
			while(availableTracks.length > 0){ availableTracks.pop(); }
			while(stracks.length > 0){ stracks.pop(); }

			// Because of the issue noted above, we do not use filter either
			timeline.trackNames.forEach(function(track){
					if(timeline.commandStack.isFileSaved(track, timeline.saveLocation)){ return; }
					availableTracks.push({value:track,text:track});
			});
	})

	function serverSaver(exportedTracks){
		let savep = Promise.all(exportedTracks.map((fObj) => {
					var data = new FormData(),
							textTrack = fObj.track,
							resource = trackResources.get(textTrack);
					data.append("file", new Blob([fObj.data],{type:fObj.mime}), fObj.name);
					data.append("label", textTrack.label);
					data.append("language", textTrack.language);
					data.append("kind", textTrack.kind);
					data.append("resourceId", resource?resource.id:"");
					data.append("contentId", content.id);
					return new Promise(function(resolve, reject){
							var xhr = new XMLHttpRequest();
							xhr.responseType = "json";
							xhr.open("POST", "/captionaider/save", true);
							xhr.addEventListener('load', function(){
									if(xhr.status < 200 || xhr.status > 299){ reject(); }
									else{
											trackResources.set(textTrack, xhr.response)
											resolve(textTrack.label);
									}
							}, false);
							xhr.addEventListener('error', reject, false);
							xhr.send(data);

							saveImg.src = "/assets/images/captionAider/loading.gif";
							saveDiv.style.cssText = 'position:fixed; top:45px; right:0px; width:auto; height:auto; z-index:1000; border-radius:3px; padding:15px; background:linear-gradient(#f7e488, #edc912);';
							saveText.innerHTML = "Saving...";
							saveDiv.style.visibility = "visible";

					}).catch(function(){

							saveDiv.style.background = "linear-gradient(#F59D9D, #F95454)";
							saveImg.src = "/assets/images/captionAider/Cancel.png";
							saveText.innerHTML = "Error occurred while saving \""+textTrack.label+ "\".";
							setTimeout(function(){
									saveDiv.style.visibility = "hidden";
							},5000);
							return null;
					});
			})).then((savedTracks) => {
					return savedTracks.filter(function(t){ return t !== null; });
			})
			savep.then((savedTracks) => {
					if(savedTracks.length === 0){ return; }

					saveDiv.style.background = "linear-gradient(#D6E0D7, #6CC36E)";
					saveImg.src = "/assets/images/captionAider/CheckCircle.png";
					saveText.innerHTML = "Successfully saved track \""+savedTracks+"\".";
					setTimeout(function(){
							saveDiv.style.visibility = "hidden";
					},5000);
			})
			return savep
	}

	function widgetSaver(exportedTracks){
		let savep = new Promise(((resolve, reject) => {
					EditorWidgets.Save(
							exportedTracks, timeline.saveLocation,
							function(){
									resolve(exportedTracks.map(function(fObj){ return fObj.track.label; }));
							},
							function(){
									alert("Error Saving; please try again.");
									reject(new Error("Error saving."));
							}
					);
			}))
			return savep
	}

	return function(dl){
		let savableTracks = timeline.trackNames.filter((track) => {
					return !timeline.commandStack.isFileSaved(track, timeline.saveLocation);
			})

			saver = timeline.saveLocation === "server"?serverSaver:widgetSaver
			if(savableTracks.length < 2){
			return Promise.resolve(dl.map((key) => {
							switch(key){
							case 'tidlist': return savableTracks;
							case 'location': return timeline.saveLocation;
							case 'saver': return function(listp){ return listp.then(saver); };
							}
					}))
			}
		$(`#saveTrackModal`).modal(`show`)
			datalist = dl
			return new Promise(((resolve, reject) => {
					resolver = resolve;
					failer = reject;
			}))
	};
}())

*/