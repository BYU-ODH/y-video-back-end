export default class ResourceService {

	// types

	types = {
		RESOURCE_START: `RESOURCE_START`,
		RESOURCE_ABORT: `RESOURCE_ABORT`,
		RESOURCE_CLEAN: `RESOURCE_CLEAN`,
		RESOURCE_ERROR: `RESOURCE_ERROR`,
		RESOURCE_GET: `RESOURCE_GET`,
	}

	// action creators

	actions = {
		resourcesStart: () => ({ type: this.types.RESOURCE_START }),
		resourcesAbort: () => ({ type: this.types.RESOURCE_ABORT }),
		resourcesClean: () => ({ type: this.types.RESOURCE_CLEAN }),
		resourcesError: error => ({ type: this.types.RESOURCE_ERROR, payload: { error } }),
		resourcesGet: resource => ({ type: this.types.RESOURCE_GET, payload: { resource } }),
	}

	// default store

	store = {
		cache: {},
		loading: false,
		lastFetched: 0,
	}

	// reducer

	reducer = (store = this.store, action) => {

		const {
			RESOURCE_START,
			RESOURCE_ABORT,
			RESOURCE_CLEAN,
			RESOURCE_ERROR,
			RESOURCE_GET,
		} = this.types

		switch (action.type) {

		case RESOURCE_START:
			return {
				...store,
				loading: true,
			}

		case RESOURCE_ABORT:
			return {
				...store,
				loading: false,
			}

		case RESOURCE_CLEAN:
			return {
				...store,
				cache: {},
			}

		case RESOURCE_ERROR:
			console.error(action.payload.error)
			return {
				...store,
				loading: false,
			}

		case RESOURCE_GET:
			return {
				...store,
				cache: {
					...store.cache,
					[action.payload.resource.id]: action.payload.resource,
				},
				loading: false,
				lastFetched: Date.now(),
			}

		default:
			return store
		}
	}

	// thunks

	getResources = (id, force = false) => async (dispatch, getState, { apiProxy }) => {

		const { resourceStore } = getState()

		const time = Date.now() - resourceStore.lastFetched

		const stale = time >= process.env.REACT_APP_STALE_TIME

		const { cache } = getState().resourceStore
		const cachedIds = Object.keys(cache)
		const cached = cachedIds.includes(id)

		if (stale || !cached || force) {

			dispatch(this.actions.resourcesStart())

			try {

				const result = await apiProxy.resources.get(id)

				const { resource } = result.data
				resource.resources = {}
				resource.relations.forEach(item => resource.resources[item.type] = [])

				Promise.all(resource.relations.map(async item => {
					const result = await apiProxy.resources.get(item.subjectId)
					resource.resources[item.type].push(result.data.resource)
				}))

				if (typeof resource.keywords === `string`) resource.keywords = resource.keywords.split(`,`)

				dispatch(this.actions.resourcesGet(resource))

			} catch (error) {
				dispatch(this.actions.resourcesError(error))
			}

		} else dispatch(this.actions.resourcesAbort())
	}

	addResource = resource => async dispatch => {
		dispatch(this.actions.resourcesGet(resource))
	}
}