import React, { PureComponent } from 'react'

import {
	ContentOverviewContainer,
	CollectionPermissionsContainer,
} from 'containers'

import Style, {
	Title,
	TitleEdit,
	TitleEditButton,
	PublishButton,
	ArchiveButton,
	TabHeader,
	Selector,
	Tab,
	NewContent,
	Icon,
} from './styles'

import plus from 'assets/plus_gray.svg'

export default class ManageCollection extends PureComponent {
	render() {
		const {
			admin,
			collection,
			collectionName,
			isEditingCollectionName,
			isContent,
			content,
		} = this.props.viewstate

		const {
			unarchive,
			toggleEdit,
			handleNameChange,
			togglePublish,
			archive,
			setTab,
			createContent,
		} = this.props.handlers

		console.log(admin)

		return (
			<Style>
				<header>
					<Title>
						{isEditingCollectionName ? (
							// TODO When switching between collections, it uses the same value
							<TitleEdit
								type='text'
								value={collectionName}
								contenteditable='true'
								onChange={handleNameChange}
								onKeyPress={event => {
									if (event.key === `Enter`) toggleEdit()
								}}
								size={collectionName.length > 0 ? collectionName.length : 1}
								autoFocus
							/>
						) : (
							<h6 onClick={toggleEdit}>{collection.name}</h6>
						)}
						<TitleEditButton
							editing={isEditingCollectionName}
							onClick={toggleEdit}
						>
							{isEditingCollectionName ? `Save` : `Edit`}
						</TitleEditButton>
					</Title>
					<div>
						{collection.archived ? (
							<>{admin[0] === `admin` || admin[0] === `professor` ? (
								<ArchiveButton onClick={unarchive}>Unarchive</ArchiveButton>
							) : ( <p>Cannot unarchive</p> )}
							</>
						) : (
							<>
								<PublishButton
									published={collection.published}
									onClick={togglePublish}
								>
									{collection.published ? `Unpublish` : `Publish`}
								</PublishButton>
								<ArchiveButton onClick={archive}>Archive</ArchiveButton>
							</>
						)}
					</div>
				</header>
				<TabHeader>
					<button onClick={setTab(true)}>Content</button>
					<button onClick={setTab(false)}>Permissions</button>
					<Selector isContent={isContent} />
				</TabHeader>
				<Tab>
					{isContent ?
						content.map(item => (
							<ContentOverviewContainer key={item.id} content={item} />
						))
						: (
							<CollectionPermissionsContainer collection={collection} />
						)}

					{isContent && (
						<NewContent onClick={createContent}>
							<Icon src={plus} />
						</NewContent>
					)}
				</Tab>
			</Style>
		)
	}
}
