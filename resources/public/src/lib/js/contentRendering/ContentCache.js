import axios from 'axios'

class ContentCache {
	cache = {}

	load(id, callback) {
		if (this.cache[id])
			callback(this.cache[id])
		else {
			axios(`${process.env.REACT_APP_YVIDEO_SERVER}/content/${id}/json`)
				.then(res => res.json())
				.then((data) => {
					this.cache[id] = data
					callback(data)
				})
		}
	}
}

export default ContentCache