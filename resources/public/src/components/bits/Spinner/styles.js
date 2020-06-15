import styled, { keyframes } from 'styled-components'
import logo from 'assets/hexborder.svg'

const rotate = keyframes`
	from {
		transform: rotate(0deg);
	}
	to {
		transform: rotate(720deg);
	}
`

const Style = styled.div`
	position: relative;

	height: 100%;
	width: 100%;

	min-height: 290px;

	display: flex;

	justify-content: center;
	align-items: center;

	& > div {
		height: 8rem;
		width: 8rem;
		background: url(${logo}) center no-repeat;
		background-size: contain;

		animation: ${rotate} 2.5s ease-in-out infinite;
	}
`

export default Style
