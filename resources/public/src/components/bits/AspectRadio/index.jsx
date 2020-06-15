import React from 'react'

import { RadioButton } from './styles'

const AspectRadio = props => {

	const { id, selected, ratio, children, onChange, contentId } = props

	const HtmlId = `c${contentId}b${id}`

	return (
		<label htmlFor={HtmlId}>
			<input id={HtmlId} name={`ratio${props.contentId}`} type='radio' value={ratio} onChange={onChange} hidden />
			<RadioButton checked={selected} />
			{children}
		</label>
	)
}

export default AspectRadio