import React, { Component, createRef } from 'react'
import ReactDOM from 'react-dom'
import { connect } from 'react-redux'

import { Wrapper } from './styles'

// TODO: Separate or move this so that it doesn't break the container pattern

class Modal extends Component {

	wrapper = createRef()

	render() {
		const Comp = this.props.modal.component

		if (!Comp) return null

		return ReactDOM.createPortal(
			(
				<Wrapper ref={this.wrapper} className=''>
					<div>
						<Comp {...this.props.modal.props} />
					</div>
				</Wrapper>
			),
			document.getElementById(`modal`),
		)
	}

	componentDidUpdate = prevProps => {

		if (!this.wrapper.current) return

		if (!prevProps.active && this.props.active) {
			this.wrapper.current.classList.add(`active`)
			this.wrapper.current.classList.remove(`hidden`)
		}

		if (prevProps.active && !this.props.active) {
			setTimeout(() => {
				this.wrapper.current.classList.remove(`active`)
				setTimeout(() => {
					this.wrapper.current.classList.add(`hidden`)
				}, 250)
			}, 1000)
		}

	}

}

const mapStoreToProps = store => ({
	modal: store.interfaceStore.modal,
})

export default connect(mapStoreToProps)(Modal)
