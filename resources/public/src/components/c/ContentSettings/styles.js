import styled from 'styled-components'

const Style = styled.div`
	overflow: hidden;
	transition: max-height .5s ease-in-out;
	height: auto;
	max-height: ${props => props.active ? `400px` : `0`};
`

export default Style

export const InnerContainer = styled.div`
	display: grid;
	grid-gap: 2rem;
	grid-template-columns: 1fr 1fr 2fr;
	padding: 2rem 0;
	& .tags {
		display: flex;
		flex-wrap: wrap;
	}
	& .tag-input {
		width: calc(100% - 4px);
	}
`

export const Column = styled.div`
	margin-right: 1rem;
	& > h4 {
		align-items: center;
		border-bottom: 1px solid #c4c4c4;
		display: grid;
		grid-gap: 1rem;
		grid-template-columns: 1fr 1.8rem;
		line-height: 2rem;
		margin-bottom: 1rem;
	}
	& textarea {
		width: 100%;
	}
`

export const Setting = styled.div`
	display: grid;
	grid-gap: 1rem;
	grid-template-columns: 1fr 1.8rem;
	justify-content: space-between;
	margin-bottom: .5rem;
	& > p {
		display: block;
		text-overflow: ellipsis;
		white-space: nowrap;
		width: inherit;
	}
`

export const RatioList = styled.div`
	display: flex;

	& > div {
		flex: 1;
	}

	& > div > label {
		cursor: pointer;
		display: flex;
		margin: .5rem 0;
	}
`
