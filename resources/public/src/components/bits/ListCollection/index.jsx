import React, { PureComponent } from 'react'

import { ListItem } from 'components/bits'

import { Header, Body } from './styles'

class ListCollection extends PureComponent {
	state = {
		isOpen: false,
	}

	togglePanel = () => {
		this.setState(prevState => ({ isOpen: !prevState.isOpen }))
	}

	render() {
		const {
			isOpen,
		} = this.state

		const {
			name,
			content,
		} = this.props.collection

		const contentIds = this.props.contentIds

		if (!content) return null

		return (
			<div>
				<Header isOpen={isOpen} onClick={this.togglePanel} >
					<h3>{name}</h3>
					<p>{content.length} Videos</p>
					<div />
				</Header>
				<Body isOpen={isOpen} count={content.length}>
					{
						content.map(item => {
							if(!contentIds.includes(item.id)) return null
							return <ListItem key={item.id} data={item} />
						})
					}
				</Body>
			</div>
		)
	}
}

export default ListCollection