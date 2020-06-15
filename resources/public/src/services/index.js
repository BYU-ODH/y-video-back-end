import AdminService from './s/admin.redux'
import AuthService from './s/auth.redux'
import CollectionService from './s/collections.redux'
import ContentService from './s/content.redux'
import InterfaceService from './s/interface.redux'
import ResourceService from './s/resources.redux'

export const adminService = new AdminService()
export const authService = new AuthService()
export const collectionService = new CollectionService()
export const contentService = new ContentService()
export const interfaceService = new InterfaceService()
export const resourceService = new ResourceService()

const services = {
	adminService,
	authService,
	collectionService,
	contentService,
	interfaceService,
	resourceService,
}

export default services
