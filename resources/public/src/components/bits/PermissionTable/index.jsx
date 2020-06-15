/* eslint-disable array-bracket-newline */
import React from 'react'
import { connect } from 'react-redux'

import Style, { Table, RemoveButton } from './styles'

const PermissionTable = props => {

	const {
		data,
		removeFunc,
		user,
	} = props

	return (
		<Style>
			<Table>
				<thead>
					{data.length > 0 &&
						<tr>
							{Object.keys(data[0])
								.filter(key => key !== `id`)
								.map((column, index) => <th key={index}>{column}</th>)
							}
							<th className='small'></th>
						</tr>
					}
				</thead>
				<tbody>
					{data.length > 0 && data.map((item, index) =>
						<tr key={index}>
							{Object.keys(item)
								.filter(key => key !== `id`)
								.map((key, index2) => <td key={index2}>{item[key]}</td>)
							}
							<td>
								{item.NetID === user.username ||
										<RemoveButton
											data-item={`${JSON.stringify(item)}`}
											onClick={removeFunc}
										/>
								}
							</td>
						</tr>
					)}
				</tbody>
			</Table>
		</Style>
	)
}

const mapStateToProps = store => ({
	user: store.authStore.user,
})

export default connect(mapStateToProps)(PermissionTable)
