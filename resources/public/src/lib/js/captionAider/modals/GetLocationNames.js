import { Save } from 'yvideo-editorwidgets'

const GetLocationNames = ( datalist ) => {
	const names = {server:`Server`}
	const targets = Save.targets
	Object.keys(targets).forEach((key) => {
		names[key] = targets[key].label
	})
	return new Promise((resolve,reject) => {
		resolve(datalist.map(key => {
			return key === `names` ? names : void 0
		}))
	})
}

export default GetLocationNames

/*
function getLocationNames(datalist){
	var names = {server:"Server"},
		targets = EditorWidgets.Save.targets;
	Object.keys(targets).forEach(function(key){
		names[key] = targets[key].label;
	});
	return new Promise(function(resolve,reject){
		resolve(datalist.map(function(key){
			return key === 'names'?names:void 0;
		}));
	});
}
*/