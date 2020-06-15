export default class AdminService {

	// types

	types = {
		ADMIN_START: `ADMIN_START`,
		ADMIN_ABORT: `ADMIN_ABORT`,
		ADMIN_CLEAN: `ADMIN_CLEAN`,
		ADMIN_CREATE_COLLECTION: `ADMIN_CREATE_COLLECTION`,
		ADMIN_CREATE_CONTENT: `ADMIN_CREATE_CONTENT`,
		ADMIN_ERROR: `ADMIN_ERROR`,
		ADMIN_GET_COLLECTION_CONTENT: `ADMIN_GET_COLLECTION_CONTENT`,
		ADMIN_SEARCH: `ADMIN_SEARCH`,
		ADMIN_SEARCH_PROFESSORS: `ADMIN_SEARCH_PROFESSORS`,
		ADMIN_SET_PROFESSOR: `ADMIN_SET_PROFESSOR`,
		ADMIN_SEARCH_COLLECTIONS: `ADMIN_SEARCH_COLLECTIONS`,
	}

	// action creators

	actions = {
		adminStart: () => ({ type: this.types.ADMIN_START }),
		adminAbort: () => ({ type: this.types.ADMIN_ABORT }),
		adminClean: () => ({ type: this.types.ADMIN_CLEAN }),
		adminCreateCollection: () => ({ type: this.types.ADMIN_CREATE_COLLECTION }),
		adminCreateContent: (content) => ({ type: this.types.ADMIN_CREATE_CONTENT, payload: { content } }),
		adminError: error => ({ type: this.types.ADMIN_ERROR, payload: { error } }),
		adminGetCollectionContent: content => ({ type: this.types.ADMIN_GET_COLLECTION_CONTENT, payload: { content }}),
		adminSearch: results => ({ type: this.types.ADMIN_SEARCH, payload: { results } }),
		adminSearchProfessors: results => ({ type: this.types.ADMIN_SEARCH_PROFESSORS, payload: { results }}),
		adminSetProfessor: professor => ({ type: this.types.ADMIN_SET_PROFESSOR, payload: { professor }}),
		adminSearchCollections: results => ({ type: this.types.ADMIN_SEARCH_COLLECTIONS, payload: { results }}),
	}

	// default store

	store = {
		data: null,
		cache: {},
		professors: [],
		professor: {},
		professorCollections: null,
		profCollectionContent: null,
		loading: false,
		lastFetched: 0,
		lastFetchedProfContent: 0,
		lastFetchedProfessors: 0,
		lastFetchedCollections: 0,
	}

	// reducer

	reducer = (store = this.store, action) => {

		const {
			ADMIN_START,
			ADMIN_ABORT,
			ADMIN_CLEAN,
			ADMIN_CREATE_COLLECTION,
			ADMIN_CREATE_CONTENT,
			ADMIN_ERROR,
			ADMIN_GET_COLLECTION_CONTENT,
			ADMIN_SEARCH,
			ADMIN_SEARCH_PROFESSORS,
			ADMIN_SET_PROFESSOR,
			ADMIN_SEARCH_COLLECTIONS,
		} = this.types

		switch (action.type) {

		case ADMIN_START:
			return {
				...store,
				loading: true,
			}

		case ADMIN_ABORT:
			return {
				...store,
				data: null,
				loading: false,
			}

		case ADMIN_CLEAN:
			return {
				...store,
				data: null,
				cache: {},
			}

		case ADMIN_CREATE_COLLECTION:
			return {
				...store,
				professor: {},
				professorCollections: null,
				profCollectionContent: null,
				loading: false,
			}

		case ADMIN_CREATE_CONTENT:
			return {
				...store,
				profCollectionContent: {
					...store.profCollectionContent,
					...action.payload.content,
				},
				loading: false,
			}

		case ADMIN_ERROR:
			console.error(action.payload.error)
			return {
				...store,
				data: null,
				loading: false,
			}

		case ADMIN_GET_COLLECTION_CONTENT:
			return {
				...store,
				profCollectionContent: action.payload.content,
				lastFetchedProfContent: Date.now(),
			}

		case ADMIN_SEARCH:
			return {
				...store,
				data: action.payload.results,
				cache: {
					...action.payload.results,
				},
				loading: false,
				lastFetched: Date.now(),
			}

		case ADMIN_SEARCH_PROFESSORS:
			return {
				...store,
				professors: action.payload.results,
				professor: {},
				professorCollections: null,
				profCollectionContent: null,
				loading: false,
				lastFetchedProfessors: Date.now(),
			}

		case ADMIN_SET_PROFESSOR:
			return {
				...store,
				professor: action.payload.professor,
				loading: false,
			}

		case ADMIN_SEARCH_COLLECTIONS:
			return {
				...store,
				professors: [],
				professorCollections: action.payload.results,
				loading: false,
				lastFetchedCollections: Date.now(),
			}

		default:
			return store
		}
	}

	// thunks

	search = (searchCategory, searchQuery, force = false) => async (dispatch, getState, { apiProxy }) => {

		const time = Date.now() - getState().adminStore.lastFetched

		const stale = time >= process.env.REACT_APP_STALE_TIME

		if (stale || force) {

			dispatch(this.actions.adminStart())

			try {

				const results = await apiProxy.admin.search.get(searchCategory, searchQuery)

				dispatch(this.actions.adminSearch(results))

			} catch (error) {
				console.error(error.message)
				dispatch(this.actions.adminError(error))
			}

		} else dispatch(this.actions.adminAbort())
	}

	searchProfessors = (searchQuery, force = false) => async (dispatch, getState, { apiProxy }) => {

		const time = Date.now() - getState().adminStore.lastFetchedProfessors

		const stale = time >= process.env.REACT_APP_STALE_TIME

		if (stale || force) {

			dispatch(this.actions.adminStart())

			try {

				const results = await apiProxy.admin.search.get(`user`, searchQuery)

				dispatch(this.actions.adminSearchProfessors(results))

			} catch (error) {
				console.error(error.message)
				dispatch(this.actions.adminError(error))
			}

		} else dispatch(this.actions.adminAbort())
	}

	setProfessor = (professorId, force = false) => async (dispatch, getState, { apiProxy }) => {

		dispatch(this.actions.adminStart())

		try {

			const results = await apiProxy.admin.search.get(`user`, professorId)

			const professor = results.filter(user => user.id = professorId)[0] || {}

			dispatch(this.actions.adminSetProfessor(professor))

		} catch (error) {
			console.error(error.message)
			dispatch(this.actions.adminError(error))
		}
	}

	getCollectionContent = (id, force = false) => async (dispatch, getState, { apiProxy }) => {

		const time = Date.now() - getState().adminStore.lastFetchedProfContent

		const stale = time >= process.env.REACT_APP_STALE_TIME

		if (stale || force) {

			dispatch(this.actions.adminStart())

			try {

				const content = await apiProxy.admin.collection.content.get(id)

				dispatch(this.actions.adminGetCollectionContent(content))

			} catch (error) {
				console.error(error.message)
				dispatch(this.actions.adminError(error))
			}

		} else dispatch(this.actions.adminAbort())
	}

	createCollection = (name) => async (dispatch, getState, { apiProxy }) => {

		dispatch(this.actions.adminStart())

		try {

			const ownerId = getState().adminStore.professor.id

			await apiProxy.admin.collection.create(name, parseInt(ownerId))

			dispatch(this.actions.adminCreateCollection())

			this.searchCollections(ownerId)

		} catch (error) {
			console.log(error.message)
			dispatch(this.actions.adminError(error))
		}
	}

	createContent = (content, collectionId) => async (dispatch, { apiProxy }) => {

		dispatch(this.actions.adminStart())

		try {

			const result = await apiProxy.admin.collection.content.post(content, collectionId)

			const data = { [result.data.id]: result.data }

			dispatch(this.actions.adminCreateContent(data))

		} catch (error) {
			dispatch(this.actions.adminError(error))
		}
	}

	createContentFromResource = (collectionId, resourceId) => async (dispatch, getState, { apiProxy }) => {

		dispatch(this.actions.adminStart())

		try {

			console.log(resourceId)
			const result = await apiProxy.admin.collection.content.createFromResource(collectionId, resourceId)

			console.log(result.data)

			// const data = { [result.data.id]: result.data }

			// dispatch(this.actions.adminCreateContent(data))

		} catch (error) {
			dispatch(this.actions.adminError(error))
		}
	}

	searchCollections = (searchQuery, force = false) => async (dispatch, getState, { apiProxy }) => {

		const time = Date.now() - getState().adminStore.lastFetchedCollections

		const stale = time >= process.env.REACT_APP_STALE_TIME

		if (stale || force) {

			dispatch(this.actions.adminStart())

			try {

				const results = await apiProxy.admin.search.get(`collection`, searchQuery)

				dispatch(this.actions.adminSearchCollections(results))

			} catch (error) {
				console.error(error.message)
				dispatch(this.actions.adminError(error))
			}

		} else dispatch(this.actions.adminAbort())
	}

	clean = () => async (dispatch) => {
		dispatch(this.actions.adminClean())
	}

}
