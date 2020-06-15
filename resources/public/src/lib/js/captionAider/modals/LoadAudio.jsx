import React from 'react'

const LoadAudio = props => {
	return (
		<div></div>
	)
}

export default LoadAudio

/*
function loadAudio(datalist){
	return new Promise(function(resolve,reject){
		var f = document.createElement('input');
		f.type = "file";
		f.addEventListener('change',function(evt){
			var f = evt.target.files[0];
			resolve(datalist.map(function(key){
				switch(key){
				case 'audiosrc': return f;
				case 'name': return f.name;
				}
			}));
		});
		f.click();
	});
}
*/