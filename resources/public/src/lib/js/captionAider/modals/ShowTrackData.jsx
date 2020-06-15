import React from 'react'

const ShowTrackData = () => {
	return (
		<div>

		</div>
	)
}

export default ShowTrackData

/*
// Show a track
    var showTrackData = (function(){
        var ractive, datalist, resolver, failer, availableTracks=[], stracks=[],
            sources = EditorWidgets.LocalFile.sources;
        ractive = new Dialog({
            el: document.getElementById('showTrackModal'),
            data: {
                dialogTitle: "Show Track",
                tracks: availableTracks,
                selectedTracks: stracks,
                trackKind: "subtitles",
                modalId: 'showTrackModal',
                sources: Object.keys(sources).map(function(key){ return {name: key, label: sources[key].label}; }),
                buttons: [{event:"showT",label:"Show"}]
            },
            partials:{ dialogBody: document.getElementById('showTrackTemplate').textContent },
            actions: {
                showT: function(event){
                    var that = this;
                    $("#showTrackModal").modal("hide");
                    this.set({selectOpen: false});

                    resolver(datalist.map(function(key){
                        switch(key){
                        case 'tracks': return that.get("selectedTracks");
                        }
                    }));
                }
            }
        });

        $('#showTrackModal').on('shown.bs.modal',function(){
            // We do this because ractive can't seem to update correctly with partials
            // even when we use ractive.set
            while(availableTracks.length > 0){ availableTracks.pop(); }
            while(stracks.length > 0){ stracks.pop(); }

            // Because of the issue noted above, we do not use filter either
            [].forEach.call(timeline.getCachedTextTracks(), function(track){
                availableTracks.push({value:track,text:track.label});
                if(timeline.hasTextTrack(track.label)){ stracks.push(track); }
            });
        });

        return function(dl){
            $('#showTrackModal').modal('show');
            datalist = dl;
            return new Promise(function(resolve, reject){
                resolver = resolve;
                failer = reject;
            });
        };
		}());

*/