import React, { useState, useEffect } from 'react'
import { connect } from 'react-redux'

import { Admin } from 'components'

import { adminService, interfaceService } from 'services'

const AdminContainer = props => {

	const {
		data,
		search,
		clean,
		setHeaderBorder,
	} = props

	const category = {
		Users: {
			name: `Users`,
			placeholder: `Search for a user`,
			url: `user`,
		},
		Collections: {
			name: `Collections`,
			placeholder: `Search for a collection`,
			url: `collection`,
		},
		Content: {
			name: `Content`,
			placeholder: `Search for content`,
			url: `content`,
		},
	}

	const [searchQuery, setSearchQuery] = useState(``)
	const [searchCategory, setSearchCategory] = useState(category.Users.name)
	const [placeholder, setPlaceholder] = useState(category.Users.placeholder)

	useEffect(() => {
		setHeaderBorder(true)
		return () => {
			setHeaderBorder(false)
		}
	}, [setHeaderBorder])

	const updateCategory = e => {
		e.preventDefault()
		clean()
		setSearchQuery(``)
		setSearchCategory(e.target.value)
		setPlaceholder(category[e.target.value].placeholder)
	}

	const updateSearchBar = e => {
		const { value } = e.target
		setSearchQuery(value)
		if (value.length > 1) search(category[searchCategory].url, value, true)
	}

	const handleSubmit = e => {
		e.preventDefault()
	}

	const viewstate = {
		searchQuery,
		searchCategory,
		category,
		data,
		placeholder,
	}

	const handlers = {
		updateCategory,
		updateSearchBar,
		handleSubmit,
	}

	return <Admin viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	data: store.adminStore.data,
})

const mapDispatchToProps = {
	search: adminService.search,
	clean: adminService.clean,
	setHeaderBorder: interfaceService.setHeaderBorder,
}

export default connect(mapStateToProps, mapDispatchToProps)(AdminContainer)
