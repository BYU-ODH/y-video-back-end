import styled from 'styled-components'

export const Wrapper = styled.form`
	display: grid;
	grid: repeat(3, 1fr) / 1fr;
	grid-gap: 2rem;

	min-width: 30rem;

	& > input {
		border: none;
		border-bottom: 1px solid #ccc;
		outline: none;
	}

	& > div {
		display: flex;
		justify-content: space-between;
	}
`

export const Button = styled.button`
	font-size: 1.5rem;
	color: ${props => props.color || `black`};
	background: transparent;
	border: none;
	outline: none;
	cursor: pointer;
`
