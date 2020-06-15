export default class AuthService {

	// types

	types = {
		AUTH_START: `AUTH_START`,
		AUTH_ABORT: `AUTH_ABORT`,
		AUTH_CLEAN: `AUTH_CLEAN`,
		AUTH_ERROR: `AUTH_ERROR`,
		AUTH_GET: `AUTH_GET`,
	}

	// action creators

	actions = {
		authStart: () => ({ type: this.types.AUTH_START }),
		authAbort: () => ({ type: this.types.AUTH_ABORT }),
		authError: ({ message }) => ({
			type: this.types.AUTH_ERROR,
			payload: { message },
		}),
		authGet: user => ({
			type: this.types.AUTH_GET,
			payload: { user },
		}),
	}

	// default store

	store = {
		user: null,
		loading: true,
		message: ``,
		tried: false,
	}

	// reducer

	reducer = (store = this.store, { type, payload }) => {
		switch (type) {

		case this.types.AUTH_START:
			return {
				...store,
				loading: true,
			}

		case this.types.AUTH_ERROR:
			console.error(payload.message)
			return {
				...store,
				user: null,
				loading: false,
				message: payload.message,
				tried: true,
			}

		case this.types.AUTH_ABORT:
			return {
				...store,
				user: null,
				loading: false,
				tried: false,
			}

		case this.types.AUTH_GET:
			return {
				...store,
				user: payload.user,
				loading: false,
				tried: true,
			}

		default:
			return store
		}
	}

	// thunks

	/**
	 * Tries to get the current user from the API. If unsuccessful, we know the user isn't logged in.
	 */
	checkAuth = () => async (dispatch, _getState, { apiProxy }) => {
		dispatch(this.actions.authStart())
		try {
			const user = await apiProxy.user.get()
			dispatch(this.actions.authGet(user))
		} catch (error) {
			dispatch(this.actions.authError(error))
		}
	}

	/**
	 * Redirects to the BYU CAS Login page
	 */
	login = () => async (_dispatch, _getState, { apiProxy }) => {
		apiProxy.auth.cas()
	}

	/**
	 * Redirects to the BYU CAS Logout page
	 */
	logout = () => async (_dispatch, _getState, { apiProxy }) => {
		apiProxy.auth.logout()
	}

}
