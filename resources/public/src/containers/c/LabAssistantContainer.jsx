import React, { useState, useEffect } from 'react'
import { connect } from 'react-redux'

import { LabAssistant } from 'components'

import { adminService, interfaceService } from 'services'

const LabAssistantContainer = props => {

	const {
		professors,
		searchProfessors,
		setHeaderBorder,
	} = props

	const category = {
		Users: {
			name: `Professors`,
			placeholder: `Search for a professor`,
			url: `user`,
		},
	}

	const [searchQuery, setSearchQuery] = useState(``)

	useEffect(() => {
		setHeaderBorder(true)
		return () => {
			setHeaderBorder(false)
		}
	}, [setHeaderBorder])

	const updateSearchBar = e => {
		const { value } = e.target
		setSearchQuery(value)
		if (value.length > 1) searchProfessors(value, true)
	}

	const handleSubmit = e => {
		e.preventDefault()
	}

	const viewstate = {
		searchQuery,
		// TODO: Admins who are also Profs, should have `prof` included in their roles because we will only search for that, not admin
		data: professors ? professors.filter(item => item.roles.includes(`prof`) || item.roles.includes(`admin`)) : [],
		placeholder: category.Users.placeholder,
	}

	const handlers = {
		updateSearchBar,
		handleSubmit,
	}

	return <LabAssistant viewstate={viewstate} handlers={handlers} />
}

const mapStateToProps = store => ({
	professors: store.adminStore.professors,
})

const mapDispatchToProps = {
	searchProfessors: adminService.searchProfessors,
	setHeaderBorder: interfaceService.setHeaderBorder,
}

export default connect(mapStateToProps, mapDispatchToProps)(LabAssistantContainer)
