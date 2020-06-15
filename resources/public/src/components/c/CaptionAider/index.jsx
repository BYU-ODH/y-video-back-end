import React, { PureComponent } from 'react'

import Style from './styles'

export default class CaptionAider extends PureComponent {
	render() {

		const {
			target,
		} = this.props.viewstate

		return (
			<Style>
				<div id='bottomContainer' ref={target} />
				<div id='timeline' />
			</Style>
		)
	}
}