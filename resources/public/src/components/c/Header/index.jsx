import React, { PureComponent } from 'react'

import { Wrapper, LogoWrapper, Name, Shadow, Logo} from './styles'

export default class Header extends PureComponent {
	render() {

		const {
			lost,
			border,
		} = this.props.viewstate

		return (
			<Wrapper lost={lost} border={border}>
				<LogoWrapper to='/'>
					<Logo />
					<Name>YVIDEO</Name>
					<Shadow>YVIDEO</Shadow>
				</LogoWrapper>
			</Wrapper>
		)
	}
}
