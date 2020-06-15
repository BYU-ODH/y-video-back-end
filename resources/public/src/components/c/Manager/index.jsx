import React, { PureComponent } from 'react'
import { Link } from 'react-router-dom'

import { LabAssistantManageCollectionContainer, ManageCollectionContainer } from 'containers'

import { Accordion } from 'components/bits'

import {
	Body,
	Container,
	CreateButton,
	NoCollection,
	Plus,
	SideMenu,
} from './styles'

import plus from 'assets/plus.svg'

export default class Manager extends PureComponent {
	render() {

		const {
			admin,
			collection,
			path,
			sideLists,
			user,
		} = this.props.viewstate

		const {
			createNew,
		} = this.props.handlers

		return (
			<Container>
				<SideMenu>

					<h4>{user ? `${user.name.endsWith(`s`) ? `${user.name}'` : `${user.name}'s`} Collections` : `My Collections`}</h4>

					<Accordion header={`Published`} active>
						{sideLists.published.map(({ id, name }, index) => <Link key={index} to={`/${path}/${id}`}>{name}</Link>)}
					</Accordion>

					<Accordion header={`Unpublished`} active>
						{sideLists.unpublished.map(({ id, name }, index) => <Link key={index} to={`/${path}/${id}`}>{name}</Link>)}
					</Accordion>

					{
						admin && <Accordion header={`Archived`}>
							{sideLists.archived.map(({ id, name }, index) => <Link key={index} to={`/${path}/${id}`}>{name}</Link>)}
						</Accordion>
					}

					<CreateButton onClick={createNew}><Plus src={plus} />Create New Collection</CreateButton>

				</SideMenu>
				<Body>
					{collection ?
						user ?
							<LabAssistantManageCollectionContainer collection={collection} published={collection.published} archived={collection.archived} />
							:
							<ManageCollectionContainer collection={collection} published={collection.published} archived={collection.archived} />
						:
						<NoCollection>Select a Collection to get started.</NoCollection>}
				</Body>
			</Container>
		)
	}
}