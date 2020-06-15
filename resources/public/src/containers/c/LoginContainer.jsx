import { Component } from 'react'
import { connect } from 'react-redux'
import {
	authService,
} from 'services'

class LoginContainer extends Component {

	// caslogin = () => {
	// 	const url = `${process.env.REACT_APP_YVIDEO_SERVER}/auth/cas/redirect${window.location.origin}/success`
	// 	const name = `BYU CAS Secure Login`
	// 	const popup = window.open(url, name, `width=500,height=1030`)

	// 	const popuppoll = setInterval(() => {
	// 		try {
	// 			if (popup.location.origin === window.location.origin || popup.closed) {
	// 				clearInterval(popuppoll)
	// 				popup.close()
	// 				this.props.login().then(() => {
	// 					this.props.history.push(`/`)
	// 				})
	// 			}
	// 		} catch ({ message }) {
	// 			console.error(message)
	// 		}
	// 	}, 300)
	// }

	render() {
		return null
	}

	// componentDidMount = () => {
	// 	const {
	// 		user,
	// 	} = this.props

	// 	if (!user) this.caslogin()
	// }
}

const mapStoreToProps = ({ authStore, interfaceStore }) => ({
	user: authStore.user,
	loading: authStore.loading,
})

const mapDispatchToProps = {
	login: authService.login,
}

export default connect(mapStoreToProps, mapDispatchToProps)(LoginContainer)
