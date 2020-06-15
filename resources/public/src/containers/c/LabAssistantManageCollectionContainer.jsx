import React, { useState, useEffect } from 'react'

import { connect } from 'react-redux'

import {
	adminService,
	collectionService,
	interfaceService,
} from 'services'

import { ManageCollection } from 'components'

import CreateContentContainer from 'components/modals/containers/CreateContentContainer'

const LabAssistantManageCollectionContainer = props => {

	const {
		collection,
		content,
		getCollectionContent,
		updateCollectionStatus,
	} = props

	const [isContent, setIsContent] = useState(true)

	useEffect(() => {
		getCollectionContent(collection.id, true)
	}, [collection, getCollectionContent])

	const togglePublish = e => {
		e.preventDefault()
		updateCollectionStatus(collection.id, collection.published ? `unpublish` : `publish`)
	}

	const createContent = () => {
		props.toggleModal({
			component: CreateContentContainer,
			collectionId: collection.id,
			isLabAssistantRoute: true,
		})
	}

	const archive = e => {
		e.preventDefault()
		updateCollectionStatus(collection.id, `archive`)
	}

	const setTab = isContent => _e => {
		setIsContent(isContent)
	}

	if(!content) return null

	const viewstate = {
		collection,
		content: Object.keys(content).map(key => content[key]),
		isContent,
	}

	const handlers = {
		togglePublish,
		createContent,
		archive,
		setTab,
	}

	return <ManageCollection viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	content: store.adminStore.profCollectionContent,
})

const mapDispatchToProps = {
	getCollectionContent: adminService.getCollectionContent,
	toggleModal: interfaceService.toggleModal,
	updateCollectionStatus: collectionService.updateCollectionStatus,
}

export default connect(mapStateToProps, mapDispatchToProps)(LabAssistantManageCollectionContainer)
