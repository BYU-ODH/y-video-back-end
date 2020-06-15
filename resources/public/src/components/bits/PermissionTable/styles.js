import styled from 'styled-components'

import remove from 'assets/delete.svg'

const Style = styled.div`
	overflow-x: auto;
`

export default Style

export const Table = styled.table`
	background: white;
	border-collapse: collapse;
	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);
	margin-bottom: 3rem;
	table-layout: fixed;
	&>thead {
		border-bottom: 2px solid #eee;
		& th {
			min-width: 20rem;
			padding: 1rem;
			text-align: left;
			&.small {
				min-width: 2rem;
			}
		}
	}
	&>tbody {
		& td {
			padding: 1rem;
		}
	}
`

export const RemoveButton = styled.button`
	background-size: contain;
	background: url(${remove}) center no-repeat;
	border: none;
	cursor: pointer;
	height: 2rem;
	outline: none;
	width: 2rem;
`