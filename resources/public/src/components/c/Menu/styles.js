import styled from 'styled-components'
import { Link } from 'react-router-dom'

export const Wrapper = styled.div`
	position: fixed;
	top: 0;
	left: 100%;

	height: 100vh;
	width: 32rem;

	padding: 2.4rem;

	display: flex;
	flex-direction: column;

	background-color: white;

	z-index: 32;

	transition: all .4s ease-in-out;

	& > button:first-of-type {
		position: relative;
		left: -8.4rem;
		transition: left .4s ease-in-out;
	}

	&.active {
		left: calc(100% - 32rem);
		box-shadow: 0 -.5rem 3rem 0 rgba(0,0,0,0.25);

		& > button:first-of-type {
			position: relative;
			left: 0;
		}
	}

	& > h4 {
		margin-top: 2.4rem;
		font-weight: 500;
	}

	& > hr {
		margin: 1rem 0;
		border: none;
		border-top: 1px solid #c4c4c4;
		max-width: 27rem;
	}
`

export const LinkStyled = styled(Link)`
	margin-bottom: 1rem;

	text-decoration: none;
	font-weight: 300;

	color: black;

	background: transparent;
	border: none;
`

export const Header = styled.h4`
	text-transform: uppercase;
	font-weight: 500;
`

export const UserPic = styled.button`
	display: flex;
	align-items: center;
	justify-content: center;

	height: 3.6rem;
	width: 3.6rem;

	color: white;
	line-height: 1.9;

	border: .125rem solid white;
	border-radius: 50%;

	outline: none;

	font-weight: 500;
	font-size: 1.8rem;

	background: linear-gradient(to bottom, #0582CA 0%, #0157b8 100%);

	z-index: 18;

	:hover {
		cursor: pointer;
	}
`

export const LogoutButton = styled.button`
	margin-bottom: 1rem;
	padding: 0;

	width: fit-content;

	text-decoration: none;
	outline: none;
	font-weight: 300;

	color: #FF6161;

	background: transparent;
	border: none;

	cursor: pointer;
`
