import React from 'react'
import Style, { Circle, Groove } from './styles'

const SwitchToggle = props => {
	const { on, setToggle, size } = props
	return (
		<Style onClick={setToggle} size={size} className={`switch-toggle`} data-key={props.data_key}>
			<Circle on={on ? 1 : 0} size={size} data-key={props.data_key} />
			<Groove on={on ? 1 : 0} size={size} data-key={props.data_key} />
		</Style>
	)
}

export default SwitchToggle
