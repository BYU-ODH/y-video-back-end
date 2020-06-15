import styled from 'styled-components'

export const Wrapper = styled.div`
	position: fixed;
	top: 0;
	left: 0;
	width: 100vw;
	height: 100vh;
	background: rgba(0,0,0,0.5);
	z-index: 40;

	opacity: ${props => props.done ? 0 : 1};
	transition: opacity .25s ease-in-out;

	display: flex;
	justify-content: center;
	align-items: center;

	& > div {
		border-radius: .3rem;
		padding: 4rem 5rem;
		background: white;

		position: relative;
	}
`
