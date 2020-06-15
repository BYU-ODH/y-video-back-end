export const roles = {
	student: `student`,
	teacher: `teacher`,
	manager: `manager`,
	admin: `admin`,
}

export default class User {

	id
	username
	name
	email
	roles
	lastLogin

	constructor(obj) {

		// type checking
		if (typeof obj.id !== `number`) console.error(`Error: id must be of type 'number'`)
		if (typeof obj.username !== `string`) console.error(`Error: username must be of type 'string'`)
		if (typeof obj.name !== `string`) console.error(`Error: name must be of type 'string'`)
		if (typeof obj.email !== `string`) console.error(`Error: email must be of type 'string'`)
		if (typeof obj.roles !== `object` || obj.roles.length < 1) console.error(`Error: roles must be an array`)
		if (typeof obj.lastLogin !== `string`) console.error(`Error: lastLogin must be of type 'string'`)

		this.id = obj.id
		this.username = obj.username
		this.name = obj.name
		this.email = obj.email
		this.roles = obj.roles
		this.lastLogin = new Date(obj.lastLogin)
	}

}
