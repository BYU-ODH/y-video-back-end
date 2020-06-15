import React from 'react'

const LoadTranscript = props => {
	return (
		<div></div>
	)
}

export default LoadTranscript

/*
function loadTranscript(datalist){
	//datalist is always the array ['linesrc']
	return new Promise(function(resolve,reject){
		var f = document.createElement('input');
		f.type = "file";
		f.addEventListener('change',function(evt){
				resolve([evt.target.files[0]]);
		});
		f.click();
	});
}
*/