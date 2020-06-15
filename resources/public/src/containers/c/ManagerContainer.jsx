import React, { useEffect } from 'react'
import { useParams, useLocation } from 'react-router-dom'
import { connect } from 'react-redux'

import { roles } from 'models/User'

import { collectionService, interfaceService } from 'services'

import { Manager } from 'components'

import CreateCollectionContainer from 'components/modals/containers/CreateCollectionContainer'

import { objectIsEmpty } from 'lib/util'

const ManagerContainer = props => {

	const {
		admin,
		collections,
		getCollections,
		setHeaderBorder,
		toggleModal,
	} = props

	const params = useParams()
	const location = useLocation()

	useEffect(() => {
		setHeaderBorder(true)
		getCollections()

		if(location.createCollection) {
			toggleModal({
				component: CreateCollectionContainer,
				route: `manager`,
			})
		}
	}, [collections, getCollections, setHeaderBorder, location.createCollection, toggleModal])

	if (objectIsEmpty(collections)) return null

	const sideLists = {
		published: [],
		unpublished: [],
		archived: [],
	}

	Object.keys(collections).forEach(id => {
		const { archived, published, name } = collections[id]

		if (archived) sideLists.archived.push({ id, name })
		else if (published) sideLists.published.push({ id, name })
		else sideLists.unpublished.push({ id, name })
	})

	const createNew = () => {
		toggleModal({
			component: CreateCollectionContainer,
		})
	}

	const viewstate = {
		admin,
		collection: collections[params.id],
		path: `manager`,
		sideLists,
	}

	const handlers = {
		createNew,
	}

	return <Manager viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	collections: store.collectionStore.cache,
	admin: store.authStore.user.roles.includes(roles.admin),
})

const mapDispatchToProps = {
	getCollections: collectionService.getCollections,
	setHeaderBorder: interfaceService.setHeaderBorder,
	toggleModal: interfaceService.toggleModal,
}

export default connect(mapStateToProps, mapDispatchToProps)(ManagerContainer)
