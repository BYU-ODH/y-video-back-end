import React, { useState, useRef, useEffect } from 'react'

import { SuperSelect } from 'yvideo-editorwidgets'

const Dialog = props => {

	const {
		viewstate,
		handlers,
	} = props

	const {
		dialogTitle,
		// languages,
		// trackLang,
		// trackKind,
		// trackName,
		// trackMime,
		types,
		modalId,
		buttons,
		// defaultOption,
		// selectOpen,
	} = viewstate

	const [state, setState] = useState(viewstate)

	const changeData = key => e => setState({
		...state,
		[key]: e.target.value,
	})

	// <SuperSelect icon='icon-globe' text='Select Language' value={state.trackLang} onChange={changeData(`trackLang`)} button='left' open={selectOpen} multiple='false' options={languages} modal={modalId} defaultOption={defaultOption} />
	const ss = useRef(null)

	const [supsel, setSupsel] = useState()

	useEffect(() => {
		if (ss !== null) {
			setSupsel(new SuperSelect({
				el: ss.current,
				icon: `icon-globe`,
				text: `Select Language`,
				value: state.trackLang,
				onChange: changeData(`trackLang`),
				button: `left`,
				open: state.selectOpen,
				multiple: `false`,
				options: viewstate.languages,
				modal: state.modalId,
				defaultOption: state.defaultOption,
			}))
		}
	// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [state.defaultOption, state.modalId, state.selectOpen, state.trackLang, viewstate.languages])

	console.log(supsel)

	return (
		<div id={modalId} className='modal-dialog'>
			<div className='modal-content'>
				<div className='modal-header'>
					<button type='button' className='close' data-dismiss='modal' aria-hidden='true'>X</button>
					<h3>{dialogTitle}</h3>
				</div>

				<div className='modal-body'>
					<div className='container-fluid'>

						<span className='form-horizontal'>
							<div className='control-group'>
								<label className='control-label'>Name</label>
								<div className='controls'>
									<input type='text' value={state.trackName} onChange={changeData(`trackName`)} placeholder='Name' id='createTrackAutofocus'/>
								</div>
							</div>

							<div className='form-group'>
								<label className='control-label'>Kind</label>
								<div className='controls'>
									<select defaultValue={state.trackKind || `subtitles`} onChange={changeData(`trackKind`)}>
										<option value='subtitles'>Subtitles</option>
										<option value='captions'>Captions</option>
										<option value='descriptions'>Descriptions</option>
										<option value='chapters'>Chapters</option>
										<option value='metadata'>Metadata</option>
									</select>
								</div>
							</div>

							<div className='control-group'>
								<label className='control-label'>Format</label>
								<div className='controls'>
									<select value={state.trackMime} onChange={changeData(`trackMime`)}>
										{types.map(type =>
											<option key={type.name} value={type.mime}>{type.name}</option>,
										)}
									</select>
								</div>
							</div>

							<div className='form-group'>
								<label className='control-label'>Language</label>
								<div className='controls'>
									<div ref={ss}></div>
									{/* <SuperSelect icon='icon-globe' text='Select Language' value={state.trackLang} onChange={changeData(`trackLang`)} button='left' open={selectOpen} multiple='false' options={languages} modal={modalId} defaultOption={defaultOption} /> */}
								</div>
							</div>

						</span>

					</div>
				</div>

				<div className='modal-footer'>
					{buttons.map(button =>
						<button key={button.label} className='btn btn-blue' onClick={handlers[button.event](state)}>{button.label}</button>)
					}
					<button className='btn btn-gray' data-dismiss='modal' aria-hidden='true'>Close</button>
				</div>
			</div>
		</div>
	)
}

export default Dialog

// import { Modal, Button } from 'react-bootstrap'

// const Dialog = (props) => {

// 	const actions = props.actions

// 	const buttonpress = (event, eventType) => {
// 		if(typeof actions[eventType] !== `function`) return
// 		actions[eventType].call(event)
// 	}

// 	return (
// 		<Modal show={props.show} onShow={props.handleShow} onHide={props.handleClose}>
// 			<Modal.Header closeButton>
// 				<Modal.Title>{props.dialogTitle}</Modal.Title>
// 			</Modal.Header>
// 			<Modal.Body>
// 				<div class='container-fluid'>
// 					{props.dialogBody}
// 				</div>
// 			</Modal.Body>
// 			<Modal.Footer>
// 				{props.buttons.map( button => {
// 					return <Button variant='secondary' class='btn btn-gray' on-tap={buttonpress(button.event)}>{button.label}</Button>
// 				})}
// 				<Button variant='primary' onClick={props.handleClose}>
// 					Close
// 				</Button>
// 			</Modal.Footer>
// 		</Modal>
// 	)
// }

// export default Dialog
