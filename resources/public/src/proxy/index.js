import apiProxy from
	'./p/ApiProxy'
// './p/test/ApiProxy'

import cookies from './p/Cookies'
import browserStorage from './p/BrowserStorage'

export {
	apiProxy,
	cookies,
	browserStorage,
}

const proxies = {
	apiProxy,
	cookies,
	browserStorage,
}

export default proxies
