import React, { Component, createRef } from 'react'
import ReactDOM from 'react-dom'

import { Wrapper, Spinner } from './styles'

class Load extends Component {

	wrapper = createRef()

	render() {
		return ReactDOM.createPortal(
			(
				<Wrapper ref={this.wrapper} className='active'>
					<Spinner />
				</Wrapper>
			),
			document.getElementById(`load`)
		)
	}

	componentDidUpdate = prevProps => {

		if (!this.wrapper.current) return

		if (!prevProps.loading && this.props.loading) {
			this.wrapper.current.classList.add(`active`)
			this.wrapper.current.classList.remove(`hidden`)
		}

		if (prevProps.loading && !this.props.loading) {
			setTimeout(() => {
				this.wrapper.current.classList.remove(`active`)
				setTimeout(() => {
					this.wrapper.current.classList.add(`hidden`)
				}, 250)
			}, 1000)
		}
	}

}

export default Load