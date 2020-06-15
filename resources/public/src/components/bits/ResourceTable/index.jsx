import React, { PureComponent } from 'react'

import Style, { Table, StyledLink } from './styles'

export default class ResourceTable extends PureComponent {

	render() {
		const { data, addResource } = this.props

		if (data === null || !data.length || data[0] === undefined) return null

		// Display message if empty

		return (
			<Style>
				<Table>
					<thead>
						<tr>
							<th>
								Name
							</th>
							<th>
								Content Type
							</th>
							<th>
								{/* Action */}
							</th>
							<th>
							</th>
						</tr>
					</thead>
					<tbody>
						{data.map((item, index) =>
							<tr key={item.id}>
								<td>{item.name}</td>
								<td>{item.contentType}</td>
								<td><StyledLink key={index} onClick={() => {
									console.log(`adding resource`, item)
									addResource(item, true)
								}}>Add to Collection</StyledLink></td>
							</tr>,
						)}
					</tbody>
				</Table>
			</Style>
		)
	}
}