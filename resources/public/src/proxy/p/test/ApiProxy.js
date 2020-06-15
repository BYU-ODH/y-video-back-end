import User, { roles } from 'models/User'

export const testUser = new User({
	email: `clevergrant@gmail.com`,
	id: 14,
	lastLogin: new Date().toISOString(),
	linked: -1,
	name: `Grant Perdue`,
	roles: [roles.admin],
	username: `zgrant12`,
})

const apiProxy = {
	user: {
		/**
		 * endpoint: /api/user
		 * @param broken if true, will throw an error
		 * @returns a User object
		 */
		get: async (broken = false) => {
			console.warn(`Warning: You are using the a mock version of apiProxy. To see actual data, change the export in src/proxy/index.js from: './p/test/ApiProxy' to './p/ApiProxy'`)
			if (broken) throw new Error(`No User Logged In`)
			else {
				console.log(`fake:`, testUser)
				return testUser
			}
		},
		collections: {
			/**
			 * Retrieves the collections for the current user
			 *
			 * @returns a map of the collections, where the key is the collection's ID
			 */
			get: async () => {

				// const result = await axios(`${process.env.REACT_APP_YVIDEO_SERVER}/api/user/collections`, { withCredentials: true }).then(res => res.data)

				const result = [
					{

					},
				]

				return result.reduce((map, item) => {
					map[item.id] = item
					return map
				}, {})
			},
		},
	},
	auth: {
		/**
		 * Sets the current URL to the OAuth2 BYU CAS Login page, then redirects back to the original URL
		 */
		cas: () => {
			// window.location.href = `${process.env.REACT_APP_YVIDEO_SERVER}/auth/cas/redirect${window.location.href}`
		},
		/**
		 * Sets the current URL to the OAuth2 BYU CAS Logout page, then redirects back to the original URL
		 */
		logout: () => {
			// window.location.href = `${process.env.REACT_APP_YVIDEO_SERVER}/auth/logout/redirect${window.location.href}`
		},
	},
	collection: {
		/**
		 * Changes the name of a specified collection
		 *
		 * @param id The ID of the collection
		 * @param name The new name of the collection
		 */
		post: async (id, name) => {
			// return axios.post(`${process.env.REACT_APP_YVIDEO_SERVER}/collection/${id}`, { name }, { withCredentials: true })
		},
		/**
		 * Publishes, Unpublishes, Archives, or Unarchives a collection
		 *
		 * @param id The ID of the collection
		 * @param action The action to perform, must be one of `archive`, `unarchive`, `publish`, `unpublish`
		 */
		edit: async (id, action) => {
			// axios(`${process.env.REACT_APP_YVIDEO_SERVER}/collection/${id}/${action}`, { withCredentials: true }).then(res => res.data)
		},
	},
	content: {
		/**
		 * Retrieves content from a list of content IDs
		 *
		 * @param ids the set of IDs of content you wish to retrieve
		 *
		 * @returns a map of the content, where the key is the content's ID
		 */
		get: async ids => {

			// const results = await Promise.all(ids.map(id => axios(`${process.env.REACT_APP_YVIDEO_SERVER}/api/content/${id}`, { withCredentials: true }).then(res => res.data)))

			const results = [{}]

			return results.reduce((map, item) => {
				map[item.id] = item
				return map
			}, {})
		},
	},
	resources: {
		get: id => {
			// axios(`${process.env.REACT_APP_RESOURCE_LIB}/resources/${id}?${Date.now().toString(36)}`, { withCredentials: true })
		},
	},
}

export default apiProxy
