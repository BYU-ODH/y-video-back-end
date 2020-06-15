import styled from 'styled-components'

export const Container = styled.div`
	padding-top: 8.4rem;
	display: flex;
`

export const SideMenu = styled.div`
	flex: none;

	border-right: 1px solid #c4c4c4;
	width: 22rem;
	min-height: calc(100vh - 14.4rem);

	height: 100%;

	display: flex;
	flex-direction: column;

	padding: 3rem;

	overflow-y: scroll;

	& > h4 {
		padding: .8rem 0;
		margin-bottom: 1rem;
	}
`

export const Body = styled.div`
	flex: auto;
	height: calc(100vh - 16rem);
`

export const CreateButton = styled.button`
	padding: 0;

	border: none;
	background: transparent;

	font-weight: normal;
	line-height: 3.7rem;

	height: 3.5rem;

	display: flex;
	align-items: center;

	outline: none;

	cursor: pointer;
`

export const Plus = styled.div`
	height: 1.7rem;
	width: 1.7rem;

	margin-right: 1rem;

	background: url(${props => props.src}) center no-repeat;
	background-size: contain;
`

export const NoCollection = styled.div`
	height: 100%;
	font-size: 2.4rem;
	color: #ccc;

	display: flex;
	align-items: center;
	justify-content: center;
`
