import { ResourceLibrary } from 'yvideojs'
import ContentRenderer from './ContentRenderer.js'
import axios from 'axios'

// ResourceLibrary.setBaseUrl(process.env.REACT_APP_YVIDEO_SERVER)
ResourceLibrary.setBaseUrl(`${process.env.REACT_APP_RESOURCE_LIB}/`)

const ContentLoader = (() => {

	const getDocumentWhitelist = async (args, type, ids) => {

		if (ids.length === 0) return Promise.resolve([])

		const data = {
			courseId: args.courseId || 1,
			contentId: args.contentId,
			permission: args.permission || `view`,
			documentType: type,
			ids: ids.join(`,`),
		}

		const formBody = Object.keys(data).map(key => `${encodeURIComponent(key)}=${encodeURIComponent(data[key])}`).join(`&`)

		console.log(formBody)

		try {

			const test = await axios.post(`${process.env.REACT_APP_YVIDEO_SERVER}/ajax/permissionChecker?${formBody}`, {
				headers: {
					"Content-Type": `application/x-www-form-urlencoded`,
				},
			})

			console.log(`test`)

			return test
		} catch (error) {
			console.error(error)
		}
	}

	/* args: resource, courseId, contentId, permission */
	const getTranscriptWhitelist = args => {
		return getDocumentWhitelist(args,
			`captionTrack`,
			args.resource.getTranscriptIds())
	}

	/* args: resource, courseId, contentId, permission */
	const getAnnotationWhitelist = args => {
		return getDocumentWhitelist(args,
			`annotationDocument`,
			args.resource.getAnnotationIds())
	}

	const renderContent = async args => {

		// Check if we are rendering something from the resource library
		if ([`video`, `audio`, `image`, `text`].indexOf(args.content.contentType) >= 0) {
			ResourceLibrary.setBaseUrl(`https://api.ayamel.org/api/v1/`)

			try {

				const resource = await ResourceLibrary.load(args.content.resourceId)

				args.resource = resource

				ContentRenderer.render({
					getTranscriptWhitelist,
					getAnnotationWhitelist,
					resource,
					content: args.content,
					courseId: args.courseId,
					contentId: args.content.id,
					holder: args.holder,
					components: args.components,
					screenAdaption: args.screenAdaption,
					startTime: args.startTime,
					endTime: args.endTime,
					renderCue: args.renderCue,
					permission: args.permission,
					callback: args.callback,
				})

			} catch (error) {
				console.error(error)
			}

		} else if (args.content.contentType === `playlist`)
			console.error(`Playlists are not supported.`)
	}

	const castContentObject = content => {
		switch (typeof content) {
		case `number`:
			return axios(`/content/${content}/json?${Date.now().toString(36)}`)
		case `object`:
			return Promise.resolve(content)
		default:
			return Promise.reject(new Error(`Invalid Content Type`))
		}
	}

	return {
		getTranscriptWhitelist,
		getAnnotationWhitelist,
		castContentObject,
		render(args) {
			castContentObject(args.content).then(async data => {
				args.content = data
				await	renderContent(args)
			})
		},
	}
})()

export default ContentLoader