import styled, { keyframes } from 'styled-components'
import logo from 'assets/hexborder.svg'

const fadein = keyframes`
from {
	opacity: 0;
}
to {
	opacity: 1;
}
`

const fadeout = keyframes`
from {
	opacity: 1;
}
to {
	opacity: 0;
}
`

export const Wrapper = styled.div`
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;

	background-color: white;

	z-index: 50;

	opacity: 0;
	animation: ${fadeout} .25s ease-in-out;

	&.active {
		opacity: 1;
		animation: ${fadein} .01s;
	}

	&.hidden {
		display: none;
	}
`

const rotate = keyframes`
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(720deg);
	}
`

export const Spinner = styled.div`
	background: url(${logo}) center no-repeat;
	background-size: cover;
	width: 20rem;
	height: 20rem;

	animation: ${rotate} 2.5s ease-in-out infinite;
`