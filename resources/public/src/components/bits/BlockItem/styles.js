import styled, { keyframes, css } from 'styled-components'

const shimmer = keyframes`
	0% {
	background-position: -30rem 0;
	}
	100% {
		background-position: 30rem 0;
	}
`

export const ItemContainer = styled.div`
	& h4 {
		font-weight: 500;
	}
`

export const Thumbnail = styled.div`
	width: 17.8rem;
	height: 10rem;

	margin-bottom: 1rem;

	${
	props => !props.loaded ?
		css`
			background-color: #eee;
			background-image: linear-gradient(to right, #eee 0%, #fff 50%, #eee 100%);
			background-repeat: no-repeat;

			animation: ${shimmer} 2s linear infinite;
			animation-fill-mode: forwards;
		`
		:
		css`
			background-color: gray;
			background-image: url(${props => props.src});
			background-size: cover;
		`
}
`
