import React, { PureComponent } from 'react'

import Style, { Table, ItemEdit } from './styles'

export default class AdminTable extends PureComponent {

	constructor(props) {
		super(props)

		this.state = {
			Users: {
				sortBy: ``,
				descending: false,
				columns: [
					{
						title: `ID`,
					},
					{
						title: `NetID`,
					},
					{
						title: `Name`,
					},
					{
						title: `Roles`,
						filter: {
							Admin: false,
							Manager: false,
							Professor: false,
							TA: false,
							Student: false,
						},
					},
					{
						title: `Email`,
					},
					{
						title: `Last Login`,
					},
				],
			},
			Collections: {
				sortBy: ``,
				descending: false,
				columns: [
					{
						title: `ID`,
					},
					{
						title: `Name`,
					},
					{
						title: `Owner`,
					},
					// {
					// 	title: `# Students`,
					// },
					// {
					// 	title: `# Content`,
					// },
					// {
					// 	title: `Email`,
					// },
				],
			},
			Content: {
				sortBy: ``,
				descending: false,
				columns: [
					{
						title: `ID`,
					},
					{
						title: `Name`,
					},
					{
						title: `Collection`,
					},
					{
						title: `Requester`,
					},
					// {
					// 	title: `Language`,
					// 	filter: {},
					// },
					{
						title: `Type`,
						filter: {
							Video: false,
							Image: false,
							Text: false,
							Audio: false,
						},
					},
					{
						title: `Expired`,
					},
					{
						title: `ResourceID`,
					},
				],
			},
		}
	}

	static getDerivedStateFromProps(props, state) {
		return {
			...state,
			data: props.data ? props.data.map(item => {
				if (props.category === `Users`) {
					return {
						ID: item.id,
						NetID: item.username,
						Name: item.name,
						Roles: item.roles,
						Email: item.email,
						"Last Login": new Date(item.lastLogin).toDateString(),
					}
				} else if (props.category === `Collections`) {
					return {
						ID: item.id,
						Name: item.name,
						Owner: item.owner,
						// "# Students",
						// "# Content",
						// Email,
					}
				} else if (props.category === `Content`){

					// const lang = item.settings.targetLanguages[0]
					// if (lang && !langs.includes(lang)) langs.push(lang)

					return {
						ID: item.id,
						Name: item.name,
						Collection: item.collectionId,
						Requester: item.requester,
						// Language: lang || ``,
						Type: item.contentType,
						Expired: item.expired,
						ResourceID: item.resourceId,
					}
				} else return item
			}) : [],
		}
	}

	render() {
		const { category } = this.props
		const { data } = this.state
		if (!data.length || data[0] === undefined) return null

		// const mappedData = data.map(item => {
		// 	if (this.props.category === `Users`) {
		// 		return {
		// 			ID: item.id,
		// 			NetID: item.username,
		// 			Name: item.name,
		// 			Roles: item.roles,
		// 			Email: item.email,
		// 			"Last Login": new Date(item.lastLogin).toDateString(),
		// 		}
		// 	} else if (this.props.category === `Collections`) {
		// 		return {
		// 			ID: item.id,
		// 			Name: item.name,
		// 			Owner: item.owner,
		// 			// "# Students",
		// 			// "# Content",
		// 			// Email,
		// 		}
		// 	} else if (this.props.category === `Content`){

		// 		// const lang = item.settings.targetLanguages[0]
		// 		// if (lang && !langs.includes(lang)) langs.push(lang)

		// 		return {
		// 			ID: item.id,
		// 			Name: item.name,
		// 			Collection: item.collectionId,
		// 			Requester: item.requester,
		// 			// Language: lang || ``,
		// 			Type: item.contentType,
		// 			Expired: item.expired,
		// 			ResourceID: item.resourceId,
		// 		}
		// 	} else return item
		// })

		const headers = this.state[category].columns

		return (
			<Style>
				<Table>
					<thead>
						<tr>
							{headers.map((header, index) => <th key={`${header.title}-${index}`}>{header.title}{header.filter && `f`}</th>)}
							<th/>
						</tr>
					</thead>
					<tbody>
						{data.map(
							item => <tr key={item.ID}>
								{headers.map(
									(header, index) => {
										return <td key={`${header}-${index}`}>
											{item[header.title]}
										</td>
									},
								)}
								<td><ItemEdit /></td>
							</tr>,
						)}
					</tbody>
				</Table>
			</Style>
		)
	}
}