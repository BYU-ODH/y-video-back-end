import styled from 'styled-components'

import searchIcon from 'assets/search.svg'

const Style = styled.div`
	display: flex;
	flex-direction: column;
	align-items: center;
	height: calc(100vh - 8.4rem);
	background-color: #fafafa;
	padding-top: 8.4rem;

	& > div {
		display: flex;
	}

	& > h1 {
		margin: 5rem 0 4rem 0;
		font-weight: normal;
	}
`

export default Style

export const Search = styled.form`
	position: relative;

	& > input {
		z-index: 1;
		background: white;

		height: 4rem;
		width: 30rem;

		font-size: 1.5rem;

		border: none;
		border-radius: 2rem;

		margin-left: 1rem;

		padding: 0 1.25rem 0 3.25rem;

		outline: none;
		box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);
	}
`

export const SearchIcon = styled.span`
	position: absolute;
	z-index: 10;
	top: 1rem;
	left: 2rem;
	background: url(${searchIcon}) center no-repeat;
	background-size: contain;
	height: 2rem;
	width: 2rem;
`

export const CategorySelect = styled.select`
	background: white;

	height: 4rem;
	width: 12rem;

	font-size: 1.5rem;

	border: none;
	border-radius: 2rem;

	padding: 0 1.25rem;

	outline: none;
	box-shadow: 0px 2px 5px -1px rgba(0,0,0,0.15);
`