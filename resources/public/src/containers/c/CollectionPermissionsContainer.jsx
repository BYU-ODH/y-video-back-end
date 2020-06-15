import React, { useEffect, useState } from 'react'
import { connect } from 'react-redux'

import services from 'services'

import { CollectionPermissions } from 'components'

const CollectionPermissionsContainer = props => {

	const { roleEndpoints } = services.collectionService

	const {
		collection, // from ManageCollection
		roles, // from collectionService
		getCollectionRoles, // from collectionService
		updateCollectionRoles, // from collectionService
	} = props

	const collectionRoles = roles[collection.id]

	const [state, setState] = useState({
		department: `*`,
		catalog: ``,
		section: ``,
		taFaculty: ``,
		exception: ``,
		disabled: {
			catalog: true,
			section: true,
			submit: true,
			taFaculty: true,
			exception: true,
		},
	})

	useEffect(
		() => {
			if (!collectionRoles) getCollectionRoles(collection.id)
		},
		[collection.id, collectionRoles, getCollectionRoles],
	)

	if (!collectionRoles) return null

	const handlers = {
		handleDepartmentChange: e => {
			setState({
				...state,
				department: e.target.value,
				catalog: ``,
				section: ``,
				disabled: {
					...state.disabled,
					catalog: e.target.value === `*`,
					section: true,
					submit: e.target.value === `*`,
				},
			})
		},
		handleCatalogChange: e => {
			setState({
				...state,
				catalog: e.target.value,
				section: ``,
				disabled: {
					...state.disabled,
					catalog: false,
					section: e.target.value === ``,
					submit: false,
				},
			})
		},
		handleSectionChange: e => {
			setState({
				...state,
				section: e.target.value,
				disabled: {
					...state.disabled,
					catalog: false,
					section: false,
					submit: false,
				},
			})
		},
		handleTaFacultyChange: e => {
			setState({
				...state,
				taFaculty: e.target.value,
				disabled: {
					...state.disabled,
					taFaculty: e.target.value === ``,
				},
			})
		},
		handleExceptionChange: e => {
			setState({
				...state,
				exception: e.target.value,
				disabled: {
					...state.disabled,
					exception: e.target.value === ``,
				},
			})
		},
		addCourse: e => {
			e.preventDefault()

			const {
				department,
				catalog,
				section,
			} = state

			const body = {
				department,
			}

			if (catalog) body.catalogNumber = catalog
			if (section) body.sectionNumber = section

			updateCollectionRoles(collection.id, roleEndpoints.linkCourses, [body])

			setState({
				...state,
				department: `*`,
				catalog: ``,
				section: ``,
				disabled: {
					...state.disabled,
					catalog: true,
					section: true,
				},
			})
		},
		removeCourse: e => {
			e.preventDefault()

			const data = JSON.parse(e.target.dataset.item)

			const body = {
				id: data.id || null,
				department: data.Department || null,
			}

			if (data.Catalog) body.catalogNumber = data.Catalog
			if (data.Section) body.sectionNumber = data.Section

			if (body.department !== null && body.id !== null)
				updateCollectionRoles(collection.id, roleEndpoints.unlinkCourses, [body])
			else alert(`Error, department not found`)
		},
		addTaFaculty: e => {
			e.preventDefault()
			updateCollectionRoles(collection.id, roleEndpoints.addTA, state.taFaculty)
			setState({
				...state,
				taFaculty: ``,
			})
		},
		removeFaculty: e => {
			e.preventDefault()

			const data = JSON.parse(e.target.dataset.item)
			const body = data.NetID || null

			if (body !== null) updateCollectionRoles(collection.id, roleEndpoints.removeTA, body)

			else alert(`Error, netId not found`)
		},
		addException: e => {
			e.preventDefault()
			updateCollectionRoles(collection.id, roleEndpoints.addException, this.state.exception)
			setState({
				...state,
				exception: ``,
			})
		},
		removeException: e => {
			e.preventDefault()
			const data = JSON.parse(e.target.dataset.item)
			const body = data.NetID || null
			if (body !== null)
				updateCollectionRoles(collection.id, roleEndpoints.removeException, body)
			else alert(`Error, netId not found`)
		},
	}

	const viewstate = {
		collection,
		roles: collectionRoles,
		state,
	}

	return <CollectionPermissions viewstate={viewstate} handlers={handlers} />
}

const mapStoreToProps = store => ({
	roles: store.collectionStore.roles,
})

const mapDispatchToProps = {
	getCollectionRoles: services.collectionService.getCollectionRoles,
	updateCollectionRoles: services.collectionService.updateCollectionRoles,
}

export default connect(mapStoreToProps, mapDispatchToProps)(CollectionPermissionsContainer)
