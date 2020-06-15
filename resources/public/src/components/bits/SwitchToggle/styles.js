import styled from 'styled-components'

const Style = styled.button`
	position: relative;
	border: none;
	background: transparent;
	outline: none;
	cursor: pointer;

	padding: 0;

	height: calc(1rem * ${props => props.size ? props.size : `1`});
	width: calc(1.8rem * ${props => props.size ? props.size : `1`});
`

export default Style

export const Circle = styled.div`
	height: calc(1rem * ${props => props.size ? props.size : `1`});
	width: calc(1rem * ${props => props.size ? props.size : `1`});

	border-radius: 50%;

	position: absolute;

	left: ${props => props.on ? `calc(.8rem * ${props.size ? props.size : `1`})` : `0`};

	transition: left .3s ease-out;

	background-color: ${props => props.on ? `#0582CA` : `#CCC`};
`

export const Groove = styled.div`
	height: calc(.8rem * ${props => props.size ? props.size : `1`});
	width: calc(1.6rem * ${props => props.size ? props.size : `1`});

	border-radius: calc(.4rem * ${props => props.size ? props.size : `1`});

	margin: calc(.1rem * ${props => props.size ? props.size : `1`});

	background-color: ${props => props.on ? `#E2EEF5` : `#EEE`};
`
