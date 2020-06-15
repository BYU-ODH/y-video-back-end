import styled from 'styled-components'

import radioChecked from 'assets/radio-checked.svg'
import radioUnchecked from 'assets/radio-unchecked.svg'

export const RadioButton = styled.div`
	width: 1.5rem;
	height: 1.5rem;
	background: url(${props => props.checked ? radioChecked : radioUnchecked}) center no-repeat;
	background-size: contain;
	margin-right: 1rem;
	position: relative;
	top: -.2rem;
`