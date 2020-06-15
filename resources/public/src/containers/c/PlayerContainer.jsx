import React, { useEffect, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { connect } from 'react-redux'

import { contentService } from 'services'

import { roles } from 'models/User'
import { objectIsEmpty } from 'lib/util'

import { Player } from 'components'

const PlayerContainer = props => {

	const {
		userId,
		content,
		getContent,
		addView,
	} = props

	const params = useParams()
	const ref = useRef(null)

	useEffect(() => {
		getContent([params.id])
	}, [getContent, params.id])

	if(!objectIsEmpty(content)) addView(params.id)

	const viewstate = {
		ref,
		userId,
		videoId: params.id,
		content: content[params.id],
	}

	return <Player viewstate={viewstate} />
}

const mapStateToProps = ({ authStore, contentStore }) => ({
	isProf: authStore.user.roles.includes(roles.teacher),
	isAdmin: authStore.user.roles.includes(roles.admin),
	userId: authStore.user.id,
	content: contentStore.cache,
})

const mapDispatchToProps = {
	getContent: contentService.getContent,
	addView: contentService.addView,
}

export default connect(mapStateToProps, mapDispatchToProps)(PlayerContainer)
