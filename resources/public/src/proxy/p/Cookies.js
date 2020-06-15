const cookies = {
	set: (cookieName, cookieValue, maxDays, path = ``, domain = ``) => {
		const date = new Date()
		date.setTime(date.getTime() + maxDays * 24 * 60 * 60 * 1000)
		document.cookie = `${cookieName}=${cookieValue};path=${path};domain=${domain};expires=${date.toUTCString()}`
	},

	get: cookieName => {
		const name = `${cookieName}=`
		const decodedCookie = decodeURIComponent(document.cookie)
		const ca = decodedCookie.split(`;`)

		ca.forEach(c => {
			while(c.charAt(0) === ` `)
				c = c.substring(1)
			if (c.indexOf(name) === 0)
				return c.substring(name.length, c.length)
		})

		return ``
	},

	getAll: () => {
		return document.cookie
			.split(`;`)
			.reduce((res, c) => {
				const [key, val] = c.trim().split(`=`).map(decodeURIComponent)
				try {
					return Object.assign(res, { [key]: JSON.parse(val) })
				} catch (e) {
					return Object.assign(res, { [key]: val })
				}
			}, {})
	},

	delete: (cookieName, path, domain) => {
		if (this.get(cookieName))
			document.cookie = `${cookieName}=${path && `;path=${path}`}${domain && `;domain=${domain}`};expires=Thu, 01 Jan 1970 00:00:01 GMT`
	},
}

export default cookies
