/* eslint-disable eqeqeq */
/* eslint-disable no-prototype-builtins */

export default class Resource {
	constructor(data, id) {
		let key
		this.id = id
		this.relations = []
		for (key in data) {
			if (data.hasOwnProperty(key))
				this[key] = data[key]
		}

	}
	loadResourcesFromRelations(relationRole, test, callback) {
		const filteredRelations = typeof test === `function` ? this.relations.filter(test, this) : this.relations
		const p = Promise.all(filteredRelations.map((relation) => {
			return load(relation[relationRole])
		}))
		if (typeof callback === `function`)
			p.then(callback)

		return p
	}
	getTranscriptIds() {
		return this.relations.filter(function (relation) {
			return relation.type == `transcript_of` && relation.objectId == this.id
		}, this).map((relation) => {
			return relation.subjectId
		})
	}
	getTranscripts(additionalTest, callback) {
		return this.loadResourcesFromRelations(`subjectId`, function (relation) {
			const isTranscript = relation.type == `transcript_of` && relation.objectId == this.id, passTest = typeof additionalTest === `function` ? additionalTest(relation) : true
			return isTranscript && passTest
		}, callback)
	}
	getAnnotationIds() {
		return this.relations.filter(function (relation) {
			return relation.type == `references` && relation.objectId == this.id
		}, this).map((relation) => {
			return relation.subjectId
		})
	}
	getAnnotations(additionalTest, callback) {
		return this.loadResourcesFromRelations(`subjectId`, function (relation) {
			const isAnnotations = relation.type == `references` && relation.objectId == this.id, passTest = typeof additionalTest === `function` ? additionalTest(relation) : true
			return isAnnotations && passTest
		}, callback)
	}
}

const reqcache = {}

function load(id, callback){
	if(!reqcache.hasOwnProperty(id)) reqcache[id] = getResourcePromise(id)
	if(typeof callback === `function`) reqcache[id].then(callback)
	return reqcache[id]
}

function getResourcePromise(id){
	return new Promise((resolve, reject) => {
		const xhr = new XMLHttpRequest()
		xhr.addEventListener(`load`, function(){
			if(this.status >= 200 && this.status < 400){
				try {
					resolve(new Resource(JSON.parse(this.responseText).resource, id))
				}catch(e){
					reject(e)
				}
			}else
				reject(new Error(this.responseText))
		}, false)
		xhr.addEventListener(`error`, () => {
			reject(new Error(`Request Failed`))
		}, false)
		xhr.addEventListener(`abort`, () => {
			reject(new Error(`Request Aborted`))
		}, false)
		xhr.open(`GET`,`${baseUrl}resources/${id}?${Date.now().toString(36)}`,true)
		xhr.send(null)
	})
}