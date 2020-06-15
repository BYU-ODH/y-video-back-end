import React, { useState } from 'react'
import { connect } from 'react-redux'

import { roles } from 'models/User'

import {
	contentService,
	interfaceService,
	adminService,
} from 'services'

import CreateContent from 'components/modals/components/CreateContent'

const CreateContentContainer = props => {

	const {
		adminContent,
		adminCreateContent,
		adminCreateContentFromResource,
		createContent,
		modal,
		search,
		toggleModal,
	} = props

	const [tab, setTab] = useState(`url`)
	const [searchQuery, setSearchQuery] = useState(``)
	const [selectedResource, setSelectedResource] = useState(``)
	const [data, setData] = useState({
		url: ``,
		resourceId: ``,
		contentType: `video`,
		title: ``,
		description: ``,
		keywords: [],
	})

	const changeTab = e => {
		setTab(e.target.name)
	}

	const onKeyPress = e => {
		if (e.which === 13) {
			e.preventDefault()
			addKeyword(e.target)
		}
	}

	const handleTextChange = e => {
		setData({
			...data,
			[e.target.name]: e.target.value,
		})
	}

	const handleSearchTextChange = e => {
		const { value } = e.target
		setSearchQuery(value)
		if (value.length > 1) search(`content`, value, true)
	}

	const handleSelectResourceChange = e => {
		const { target } = e
		console.log(target.value)
		setSelectedResource(target.value)
	}

	const handleTypeChange = e => {
		const contentType = e.target.dataset.type
		setData({
			...data,
			contentType,
		})
	}

	const addKeyword = element => {
		if (element.id !== `keyword-datalist-input` || element.value === ``) return

		setData({
			...data,
			keywords: [...data.keywords, element.value],
		})

		document.getElementById(`create-content-form`).reset()
	}

	const handleSubmit = e => {
		e.preventDefault()
		if(modal.isLabAssistantRoute) adminCreateContent(data, modal.collectionId)
		else createContent(data, modal.collectionId)
		toggleModal()
	}

	const handleAddResourceSubmit = e => {
		e.preventDefault()
		adminCreateContentFromResource(modal.collectionId, selectedResource)
		toggleModal()
	}

	const remove = e => {
		const badkeyword = e.target.dataset.keyword
		setData({
			...data,
			keywords: data.keywords.filter(keyword => keyword !== badkeyword),
		})
	}

	const viewstate = {
		adminContent,
		data,
		searchQuery,
		tab,
	}

	const handlers = {
		changeTab,
		handleAddResourceSubmit,
		handleSearchTextChange,
		handleSelectResourceChange,
		handleSubmit,
		handleTextChange,
		handleTypeChange,
		onKeyPress,
		remove,
		toggleModal,
	}

	return <CreateContent viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	admin: store.authStore.user.roles.includes(roles.admin),
	adminContent: store.adminStore.data,
	modal: store.interfaceStore.modal,
	collections: store.collectionStore.cache,
})

const mapDispatchToProps = {
	adminCreateContent: adminService.createContent,
	adminCreateContentFromResource: adminService.createContentFromResource,
	createContent: contentService.createContent,
	toggleModal: interfaceService.toggleModal,
	search: adminService.search,
}

export default connect(mapStateToProps, mapDispatchToProps)(CreateContentContainer)