import styled from 'styled-components'

import blockView from 'assets/block-view.svg'
import listView from 'assets/list-view.svg'

const Style = styled.div`
max-width: 100rem;

padding: 8.4rem 2.4rem 0 2.4rem;
margin: 0 auto;

& header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 2rem;

	& > div {
		display: flex;
		align-items: center;

		& > h3 {
			font-weight: bold;
			font-size: 1.2rem;
		}

		& a {
			font-weight: 300;
			font-size: 1.2rem;
			text-decoration: none;
			color: #000;
		}

		& > button {
		}
	}
}
`

export default Style

export const ViewToggle = styled.button`
	background: url(${props => props.displayBlocks ? listView : blockView}) center no-repeat;
	background-size: cover;
	border: none;
	height: 1.5rem;
	width: 1.5rem;
	margin-left: 5rem;
	outline: none;
	cursor: pointer;
`
