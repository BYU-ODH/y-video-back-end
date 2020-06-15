import React, { PureComponent } from 'react'

import { LabAssistantTable } from 'components/bits'

import Style, { Search, SearchIcon } from './styles'

export class LabAssistant extends PureComponent {
	render() {

		const {
			data,
			placeholder,
			searchQuery,
		} = this.props.viewstate

		const {
			updateSearchBar,
			handleSubmit,
		} = this.props.handlers

		return (
			<Style>
				<h1>Lab Assistant Dashboard</h1>
				<Search onSubmit={handleSubmit}>
					<SearchIcon />
					<input type='search' placeholder={placeholder} onChange={updateSearchBar} value={searchQuery} />
				</Search>
				<LabAssistantTable data={data} />
			</Style>
		)
	}
}

export default LabAssistant
