import React from 'react'

import Style, { Remove } from './styles'

const Tag = props => {
	return (
		<Style>
			{props.children}
			<Remove onClick={props.onClick} data-value={props.children} />
		</Style>
	)
}

export default Tag