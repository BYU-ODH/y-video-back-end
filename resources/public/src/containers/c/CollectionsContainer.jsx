import React, { useEffect } from 'react'
import { connect } from 'react-redux'

import { collectionService, interfaceService, contentService } from 'services'

import { roles } from 'models/User'

import { Collections } from 'components'

// import { objectIsEmpty } from 'lib/util'

const CollectionsContainer = props => {

	const {
		isProf,
		isAdmin,
		displayBlocks,
		content,
		getContent,
		collections,
		getCollections,
		toggleCollectionsDisplay,
		setHeaderBorder,
	} = props

	useEffect(() => {
		getCollections()
		setHeaderBorder(false)

		// Iterate through published collections to get content, then get the ids of all of the content
		const ids = [].concat.apply([], Object.entries(collections).filter(([k,v]) => v.published && !v.archived)
			.map(([k,v]) => v.content.map(item => parseInt(item.id))))
		getContent(ids)

		return () => {
			setHeaderBorder(true)
		}
	}, [collections, getCollections, getContent, setHeaderBorder])

	const viewstate = {
		isProf,
		isAdmin,
		displayBlocks,
		// TODO: When archiving a collection, make sure to unpublish it
		collections: Object.fromEntries(Object.entries(collections).filter(([k,v]) => v.published && !v.archived)),
		// TODO: When recreating the backend, add a collection.content.published value, so that we don't need to call getContent
		contentIds: Object.entries(content).filter(([k, v]) => v.published).map(([k,v]) => parseInt(k)),
	}

	const handlers = {
		toggleCollectionsDisplay,
	}

	return <Collections viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = ({ authStore, interfaceStore, collectionStore, contentStore }) => ({
	isProf: authStore.user.roles.includes(roles.teacher),
	isAdmin: authStore.user.roles.includes(roles.admin),
	displayBlocks: interfaceStore.displayBlocks,
	collections: collectionStore.cache,
	content: contentStore.cache,
})

const mapDispatchToProps = {
	getCollections: collectionService.getCollections,
	getContent: contentService.getContent,
	toggleCollectionsDisplay: interfaceService.toggleCollectionsDisplay,
	setHeaderBorder: interfaceService.setHeaderBorder,
}

export default connect(mapStateToProps, mapDispatchToProps)(CollectionsContainer)
