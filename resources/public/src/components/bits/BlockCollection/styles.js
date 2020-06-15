import styled from 'styled-components'

import arrowLeft from 'assets/arrow-left.svg'
import arrowRight from 'assets/arrow-right.svg'

export const Container = styled.div`
	padding: 2rem;

	border-top: 1px solid #ccc;

	& > div {
		position: relative;
	}
`

export const Header = styled.div`
	display: grid;
	grid-template-columns: 18rem auto;
	justify-items: start;
	padding-bottom: 2rem;

	& > p {
		color: #a4a4a4;
	}

	& a {
		color: black;
		font-weight: 500;
		font-size: 1.17em;
		text-decoration: none;
	}
`

export const SlideWrapper = styled.div`
	display: grid;
	grid-auto-flow: column;
	grid-template-columns: ${props => `repeat(${props.count}, 17.8rem)`};
	grid-gap: 5rem;

	overflow-x: scroll;
	overflow-y: hidden;

	will-change: overflow;

	scroll-behavior: smooth;

	::-webkit-scrollbar {
		background: transparent;
	}

	& > a:last-child {
		margin-right: 6rem;
	}
`

export const Arrow = styled.div`

	display: flex;
	align-items: center;
	justify-content: center;

	position: absolute;
	top: 0;

	height: 10rem;
	width: 6rem;

	cursor: pointer;

	&.right{
		right: 0;
		background-image: linear-gradient(to left, rgba(255,255,255,1), rgba(255,255,255,0));

		& > div {
			height: 1.5rem;
			width: 1.5rem;

			transition: opacity .25s ease-in-out;
			opacity: ${props => props.right ? `0` : `1`};
			background-image: url(${arrowRight});
			background-size: cover;
		}
	}

	&.left {
		left: ${props => props.hideLeft ? `-100rem` : `0`};

		transition: opacity .25s ease-in-out;
		opacity: ${props => props.left ? `0` : `1`};
		background-image: linear-gradient(to right, rgba(255,255,255,1), rgba(255,255,255,0));

		& > div {
			height: 1.5rem;
			width: 1.5rem;

			transition: opacity .25s ease-in-out;
			opacity: ${props => props.left ? `0` : `1`};
			background-image: url(${arrowLeft});
			background-size: cover;
		}
	}
`

export const BlockEnd = styled.div`
	width: .1rem;
	height: 10rem;
`