import styled, { keyframes } from 'styled-components'

const fadeIn = keyframes`
		from {
			opacity: 0;
		}

		to {
			opacity: 1;
		}
	`,

	fadeOut = keyframes`
		from {
			opacity: 1;
		}

		to {
			opacity: 0;
		}
	`

export const Wrapper = styled.div`
	position: absolute;
	width: 100%;
	height: 100vh;

	visibility: ${props => props.out ? `hidden` : `visible`};
	animation: ${props => props.out ? fadeOut : fadeIn} .25s linear;
	transition: visibility .25s linear;

	background-color: rgba(0,0,0,0.25);

	display: flex;
	justify-content: center;
	align-items: center;

	& > div {
		background-color: white;
		box-shadow: 0px .4rem .7rem -.1rem rgba(0,0,0,0.25);

		min-width: 32rem;
		width: auto;
		padding: 6rem 15rem;

		display: flex;
		flex-wrap: wrap;
		justify-content: space-around;

		& > button {
			background: transparent;
			border: none;
			font-size: 18px;
			color: #0582CA;
			flex-basis: 100%;
			margin-top: 5rem;
			outline: none;
			cursor: pointer;
		}

		& > div {
			width: 27.5rem;
			padding: 2rem;

			& > h3 {
				font-weight: bold;
				font-size: 1.8rem;
				text-justify: center;
				margin-bottom: 1.2rem;
			}
		}
	}
`