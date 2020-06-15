import React, { PureComponent } from 'react'

import { Container, List, Arrow } from './styles'

import arrow from 'assets/carrot.svg'

class AccordionMenu extends PureComponent {

	constructor(props) {
		super(props)
		this.state = {
			active: props.active,
		}
	}

	handleToggle = e => {
		e.preventDefault()

		this.setState({
			active: !this.state.active,
		})
	}

	render() {

		const { handleToggle } = this
		const { header, children = [] } = this.props
		const { active } = this.state

		return (
			<Container>
				<h6 onClick={handleToggle}>{header}<Arrow src={arrow} active={this.state.active} /></h6>
				<List numChildren={children.length} active={active}>
					{children}
				</List>
			</Container>
		)
	}

}

export default AccordionMenu
