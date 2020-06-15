import { applyMiddleware, combineReducers, createStore } from 'redux'

import { composeWithDevTools } from 'redux-devtools-extension'

import thunk from 'redux-thunk'

import proxies from 'proxy'

import {
	adminService,
	authService,
	collectionService,
	interfaceService,
	contentService,
	resourceService,
} from 'services'

// Use this const to change the settings in Redux Dev Tools. Set
// the options here, and then replace `composeWithDevTools` with
// `composeEnhancers` down below.

// const composeEnhancers = composeWithDevTools({
// 	trace: true,
// })

const store = createStore(

	// This is what the store looks like
	combineReducers({
		adminStore: adminService.reducer,
		authStore: authService.reducer,
		collectionStore: collectionService.reducer,
		contentStore: contentService.reducer,
		interfaceStore: interfaceService.reducer,
		resourceStore: resourceService.reducer,
	}),

	// This is the initial state of the store
	{
		adminStore: adminService.store,
		authStore: authService.store,
		collectionStore: collectionService.store,
		contentStore: contentService.store,
		interfaceStore: interfaceService.store,
		resourceStore: resourceService.store,
	},

	composeWithDevTools(
		applyMiddleware(thunk.withExtraArgument(proxies)),
	),
)

export default store