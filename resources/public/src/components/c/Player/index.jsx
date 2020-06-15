import React, { Component } from 'react'

import { Ayamel } from 'yvideojs'

import ContentLoader from 'lib/js/contentRendering/ContentLoader'

import { CollectionsContainer } from 'containers'

import Style from './styles'
import 'yvideojs/css/player.css'

export default class Player extends Component {
	render() {
		const { ref } = this.props.viewstate
		return (
			<Style>
				<div ref={ref} />
				<CollectionsContainer />
			</Style>
		)
	}

	componentDidUpdate = async () => {
		const {
			content,
			userId,
			ref,
		} = this.props.viewstate

		try {
			// Render the content
			ContentLoader.render({
				ContentLoader,
				content,
				userId,
				owner: true,
				teacher: false,
				collectionId: 0,
				holder: ref.current,
				annotate: true,
				open: true,
				screenAdaption: false,
				aspectRatio: Ayamel.aspectRatios.hdVideo,
				startTime: `0`,
				endTime: `-1`,
			})
		} catch (error) {
			console.error(error)
		}
	}
}