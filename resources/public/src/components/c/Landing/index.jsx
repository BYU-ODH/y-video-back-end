import React, { PureComponent } from 'react'

import { Overlay } from 'components'

import { Wrapper, Comets, Welcome, Logo, Button } from './styles'

class Landing extends PureComponent {
	render() {

		const {
			overlay,
		} = this.props.viewstate

		const {
			toggleOverlay,
			handleLogin,
		} = this.props.handlers

		return (
			<Wrapper>
				<Comets className='left' />
				<Comets className='right' />

				<Welcome>

					<div>
						<Logo />
						<h1>YVIDEO</h1>
					</div>

					<div className='button-wrapper'>
						<Button className='primary' onClick={handleLogin}>Sign In</Button>
						<Button className='secondary' onClick={toggleOverlay}>About</Button>
					</div>

				</Welcome>

				{ overlay && <Overlay toggleOverlay={toggleOverlay} /> }

			</Wrapper>
		)
	}
}

export default Landing
