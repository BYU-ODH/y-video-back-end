import React, { useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { connect } from 'react-redux'

import { roles } from 'models/User'

import { interfaceService, adminService } from 'services'

import { Manager } from 'components'

import CreateCollectionContainer from 'components/modals/containers/CreateCollectionContainer'
import { objectIsEmpty } from 'lib/util'

const LabAssistantManagerContainer = props => {

	const {
		admin,
		collections,
		professor,
		searchCollections,
		setHeaderBorder,
		setProfessor,
		toggleModal,
	} = props

	const { professorId, collectionId } = useParams()

	useEffect(() => {
		setHeaderBorder(true)

		if(objectIsEmpty(professor)){
			setProfessor(professorId)

			if (!collections)
				searchCollections(professorId, true)
			else console.log(collections)
		}
		return () => {
			setHeaderBorder(false)
		}
	}, [collections, professor, professorId, searchCollections, setHeaderBorder, setProfessor])

	if(!professor || objectIsEmpty(professor) || !collections) return null

	const professorCollections = collections.reduce((accumulator, collection) => {
		if (collection.owner === parseInt(professor.id)) {
			return {
				...accumulator,
				[collection.id]: collection,
			}
		}
		return accumulator
	}, {})

	const sideLists = {
		published: [],
		unpublished: [],
		archived: [],
	}

	Object.keys(professorCollections).forEach(id => {
		const { archived, published, name } = professorCollections[id]

		if (archived) sideLists.archived.push({ id, name })
		else if (published) sideLists.published.push({ id, name })
		else sideLists.unpublished.push({ id, name })
	})

	const createNew = () => {
		toggleModal({
			component: CreateCollectionContainer,
			isLabAssistantRoute: true,
		})
	}

	const viewstate = {
		admin,
		collection: professorCollections[collectionId],
		path: `lab-assistant-manager/${professor.id}`,
		sideLists,
		user: professor,
	}

	const handlers = {
		createNew,
	}

	return <Manager viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	professor: store.adminStore.professor,
	collections: store.adminStore.professorCollections,
	admin: store.authStore.user.roles.includes(roles.admin),
})

const mapDispatchToProps = {
	searchCollections: adminService.searchCollections,
	setHeaderBorder: interfaceService.setHeaderBorder,
	setProfessor: adminService.setProfessor,
	toggleModal: interfaceService.toggleModal,
}

export default connect(mapStateToProps, mapDispatchToProps)(LabAssistantManagerContainer)
