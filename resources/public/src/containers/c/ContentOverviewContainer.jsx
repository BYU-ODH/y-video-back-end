import React, { useState } from 'react'
import { connect } from 'react-redux'

import {
	collectionService,
	contentService,
} from 'services'

import {
	ContentOverview,
} from 'components'

import { objectIsEmpty } from 'lib/util'

const ContentOverviewContainer = props => {

	const {
		content,
		removeCollectionContent,
		updateContent,
	} = props

	const [editing, setEditing] = useState(false)
	const [showing, setShowing] = useState(false)

	const [contentState, setContentState] = useState(content)

	if (objectIsEmpty(content)) return null

	const handleToggleEdit = async () => {
		if (editing) {
			await updateContent(contentState)
			setShowing(false)
			setTimeout(() => {
				setEditing(false)
			}, 500)
		} else setEditing(true)
	}

	const handleNameChange = e => {
		setContentState({
			...contentState,
			name: e.target.value,
			resource: {
				...contentState.resource,
				title: e.target.value,
			},
		})
	}

	const handleRemoveContent = e => {
		removeCollectionContent(content.collectionId, content.id)
	}

	const handleTogglePublish = e => {
		setContentState({
			...contentState,
			published: !contentState.published,
		})
	}

	const viewstate = {
		content: contentState,
		showing,
		editing,
	}

	const handlers = {
		handleNameChange,
		handleRemoveContent,
		handleToggleEdit,
		handleTogglePublish,
		setContentState,
		setShowing,
	}

	return <ContentOverview viewstate={viewstate} handlers={handlers} />
}

const mapDispatchToProps = {
	removeCollectionContent: collectionService.removeCollectionContent,
	updateContent: contentService.updateContent,
}

export default connect(null, mapDispatchToProps)(ContentOverviewContainer)