import styled from 'styled-components'

import searchIcon from 'assets/search.svg'

const Style = styled.div`
	padding: 2rem;
	& h4 {
		font-size: 1.4rem;
		font-weight: normal;
		margin-bottom: 1.6rem;
	}
`

export default Style

export const Search = styled.form`
	position: relative;
	& > input {
		background: white;
		border-radius: 1.3rem;
		border: none;
		box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);
		height: 2.6rem;
		margin-bottom: 1.6rem;
		margin-right: 2rem;
		outline: none;
		padding-left: 1rem;
		padding-right: 1rem;
		width: 18rem;
		z-index: 1;
	}
`

export const SearchIcon = styled.span`
	background-size: contain;
	background: url(${searchIcon}) center no-repeat;
	height: 1.4rem;
	left: .7rem;
	position: absolute;
	top: .6rem;
	width: 1.4rem;
	z-index: 10;
`

export const DepartmentSelect = styled.select`
	background: white;
	border-radius: 1.3rem;
	border: none;
	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);
	height: 2.6rem;
	margin-bottom: 1.6rem;
	margin-right: 2rem;
	outline: none;
	padding-left: .6rem;
	padding-right: 1.2rem;
	width: 18rem;
`

export const CatalogInput = styled.input`
	background: ${props => props.disabled ? `#eee` : `white`};

	height: 2.6rem;
	width: 18rem;

	border: none;
	border-radius: 1.3rem;

	margin-bottom: 1.6rem;
	margin-right: 2rem;

	outline: none;

	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);

	padding-left: 1rem;
	padding-right: 1.2rem;
`

export const SectionInput = styled.input`
	background: ${props => props.disabled ? `#eee` : `white`};

	height: 2.6rem;
	width: 18rem;

	border: none;
	border-radius: 1.3rem;

	margin-bottom: 1.6rem;
	margin-right: 2rem;

	outline: none;

	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);

	padding-left: 1rem;
	padding-right: 1.2rem;
`

export const AddButton = styled.button`
	background: ${props => props.disabled ? `#eee` : `#0582CA`};
	color: ${props => props.disabled ? `initial` : `white`};

	height: 2.8rem;
	width: 5rem;

	border: none;
	border-radius: 1.3rem;

	outline: none;
	${props => props.disabled ? `` : `cursor: pointer;`}

	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);

	padding-left: 1rem;
	padding-right: 1.2rem;
`