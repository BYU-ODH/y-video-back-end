import React, { useState } from 'react'

import Dialog from './modalTools/Dialog'
import { SetLocationTemplate } from './modalTools/ModalTemplates'

const GetLocation = ({ targets }) => {
	const [show, setShow] = useState(false)
	const buttons = [{event: `save`, label: `Set Location`}]

	const dialogBody = SetLocationTemplate({ saveLocations: Object.keys(targets).map((key) => {
		return {
			value: key,
			name: targets[key].label,
		}
	}), saveLocation: `server`})

	const handleShow = () => {
		setShow(true)
	}

	const handleClose = () => {
		setShow(false)
	}

	const actions = {
		save: (event) => {
			handleClose()
			/* resolver(datalist.map(function(key){
					return key === 'location'?that.get("saveLocation"):void 0;
			}));*/
		},
	}

	return (
		<Dialog show={show} handleShow={handleShow} handleClose={handleClose} actions={actions} dialogTitle='Set Save Location' dialogBody={dialogBody} buttons={buttons} />
	)
}

export default GetLocation

/*
//Set Save Location
var getLocation = (function(){
	var ractive, datalist, resolver,
			targets = EditorWidgets.Save.targets;
	ractive = new Dialog({
			el: document.getElementById('setLocModal'),
			data: {
					dialogTitle: "Set Save Location",
					saveLocations: Object.keys(targets).map(function(key){
							return {
									value: key,
									name: targets[key].label
							};
					}), saveLocation: "server",
					buttons: [{event:"save",label:"Set Location"}]
			},
			partials:{ dialogBody: document.getElementById('setLocTemplate').textContent },
			actions: {
					save: function(event){
							var that = this;
							$("#setLocModal").modal("hide");
							resolver(datalist.map(function(key){
									return key === 'location'?that.get("saveLocation"):void 0;
							}));
					}
			}
	});

	return function(dl){
			$('#setLocModal').modal('show');
			datalist = dl;
			return new Promise(function(resolve, reject){
					resolver = resolve;
			});
	};
}());
*/