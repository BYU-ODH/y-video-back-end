import styled from 'styled-components'

export const Container = styled.div`
	& > h6 {
		font-size: 1.2rem;
		font-weight: normal;
		padding: .8rem 0;

		cursor: pointer;
	}
`

export const List = styled.div`
	display: flex;
	flex-direction: column;

	height: ${props => props.active ? `calc(4.16rem * ${props.numChildren})` : `0`};

	transition: height .25s ease-in-out;

	overflow: hidden;

	& > a {
		padding: 1.4rem;
		background-color: transparent;

		font-weight: lighter;

		border-radius: .3rem;
	}
`

export const Arrow = styled.div`
	float: right;

	height: 1.2rem;
	width: 1.2rem;

	background: url(${props => props.src}) center no-repeat;
	background-size: contain;

	transform: ${props => props.active ? `rotate(-180deg)` : `rotate(0deg)`};

	transition: transform .25s ease-in-out;
`