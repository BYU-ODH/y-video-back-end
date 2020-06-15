import styled, { keyframes } from 'styled-components'

const shimmer = keyframes`
	0% {
		background-position: -30rem 0;
	}
	100% {
			background-position: 30rem 0;
	}
`

const Style = styled.div`
	height: ${props => props.height};
	width: ${props => props.width};

	@media screen and (max-width: 455px) {
		height: ${props => props.heightSm};
		width: ${props => props.widthSm};
	}

	animation: ${shimmer} 2s linear 1s infinite;
	animation-fill-mode: forwards;
	background-color: #eee;
	background-image: linear-gradient(to right, #eee 0%, #fff 50%, #eee 100%);
	background-repeat: no-repeat;

	overflow: hidden;

	& > img {
		object-fit: cover;
		object-position: center;

		height: ${props => props.height};
		width: ${props => props.width};

		@media screen and (max-width: 455px) {
			height: ${props => props.heightSm};
			width: ${props => props.widthSm};
		}
	}
`

export default Style
