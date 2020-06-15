import styled from 'styled-components'

import { Link } from 'react-router-dom'

import HexLogo from 'assets/hexborder.svg'

export const Wrapper = styled.div`
height: 8.4rem;
width: 100%;
/* box-shadow: 0 -.5rem 3rem 0 rgba(0,0,0,0.25); */
/* padding: 0 2.4rem; */
display: flex;
align-items: center;
justify-content: space-between;

border-bottom: ${props => props.border ? `1px solid #c4c4c4` : `none`};

position: fixed;
background-color: ${props => props.lost ? `transparent` : `white`};

z-index: 16;

& > button {
	background-color: #0157b8;
	border: none;
	color: white;
	padding: 1rem;
	border-radius: .3rem;
	margin-right: 7.4rem;
	cursor: pointer;
}
`

export const LogoWrapper = styled(Link)`
	display: flex;
	align-items: center;

	text-decoration: none;
	color: black;
	display: flex;
	align-items: center;
	margin-left: 2.4rem;
`

export const Name = styled.h1`
	font-family: 'Roboto Mono';
	font-weight: bold;
	font-size: 1.8rem;
	line-height: 2.1rem;
	margin: 0 0 0 1.3rem;

	z-index: 2;

	background: linear-gradient(to right, #0582CA 0%, #002E5D 100%);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
`

export const Shadow = styled.h1`
	position: absolute;
	z-index: 1;

	color: white;

	font-family: 'Roboto Mono';
	font-weight: bold;
	font-size: 1.8rem;
	line-height: 2.1rem;
	margin: 0 0 0 4.9rem;

	text-shadow: -.125rem 0 white, 0 .125rem white, .125rem 0 white, 0 -.125rem white, -.125rem .125rem white, .125rem .125rem white, .125rem -.125rem white, -.125rem -.125rem white;
`

export const Logo = styled.div`
	background: url(${HexLogo}) no-repeat center;
	background-size: contain;
	height: 3.6rem;
	width: 3.6rem;
`