import Settings from './Settings'

export default class Content {
	authKey = ``
	collectionId = null
	contentType = ``
	dateValidated = ``
	description = ``
	expired = false
	fullVideo = false
	id = null
	isCopyrighted = false
	name = ``
	physicalCopyExists = false
	published = false
	requester = ``
	resourceId = ``
	thumbnail = ``
	views = 0
	resource = {
		keywords: [],
	}
	settings = new Settings()
}
