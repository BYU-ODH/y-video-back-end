class xApi {
	constructor(course, user, ADL) {
		this.course = course
		this.user = user
		this.ADL = ADL
		// this.page = course = user = {}
		this.record = false
		// this.resourceName = ``
		this.baseUri = `${window.location.origin}/`
	}

	send = async args => {
		const { record, user, baseUri, course } = this

		if (!record) return

		// create statement
		// Agent = User, Action = Verb, Activity = Content Object
		// const stmt = new ADL.XAPIStatement(new ADL.XAPIStatement.Agent(`mailto:${user.email ? user.email : `placeholder@some.org`}`, user.name),
		// new ADL.XAPIStatement.Verb(baseUri + args.verb, args.verb),
		// new ADL.XAPIStatement.Activity(page.name, resourceName))

		// stmt.timestamp = (new Date()).toISOString()

		// if (args.type) stmt.object.definition.extensions = args.type

		// some universal xApi extensions that should be included in each request
		if (args.extensions) {
			args.extensions[`${baseUri}contextId`] = course.id || -1
			args.extensions[`${baseUri}authScheme`] = user.authScheme
			// stmt.object.definition.extensions = args.extensions
		}

		// send statement and log response
		// await ADL.XAPIWrapper.sendStatement(stmt, (resp, obj) => {
		// console.log(`[${obj.id}]: ${resp.status} - ${resp.statusText}`)
		// })
	}

	pageLoad = () => {
		this.send({ verb: `started`, extensions: {} })
	}

	ended = time => {
		this.send({
			verb: `ended`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	playClick = time => {
		this.send({
			verb: `played`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	pauseClick = time => {
		this.send({
			verb: `paused`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	rateChange = (time, rate) => {
		this.send({
			verb: `changed_playrate`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}playRate`]: rate,
			},
		})
	}

	volumeChange = (time, volume) => {
		this.send({
			verb: `changed_volume`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}volume`]: volume,
			},
		})
	}

	timeJump = (oldTime, newTime) => {
		this.send({
			verb: `jumped`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}oldTime`]: oldTime,
				[`${this.baseUri}newTime`]: newTime,
			},
		})
	}

	repeatCaption = time => {
		this.send({
			verb: `repeated_caption`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	transcriptCueClick = (captionTrackId, cueNumber, time) => {
		this.send({
			verb: `clicked_transcript_cue`,
			type: `${this.baseUri}transcription`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}cueNumber`]: cueNumber,
				[`${this.baseUri}captionTrackId`]: captionTrackId,
			},
		})
	}

	captionTranslation = (captionTrackId, text, time) => {
		this.send({
			verb: `translated_word`,
			type: `${this.baseUri}caption`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}captionTrackId`]: captionTrackId,
				[`${this.baseUri}text`]: `"${text}"`,
			},
		})
	}

	transcriptionTranslation = (captionTrackId, cueNumber, text, time) => {
		this.send({
			verb: `translateda_word`,
			type: `${this.baseUri}caption`,
			extensions: {
				[`${this.baseUri}captionTrackId`]: captionTrackId,
				[`${this.baseUri}cueNumber`]: cueNumber,
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}text`]: `"${text}"`,
			},
		})
	}

	viewTextAnnotation = (annotationDocId, text, time) => {
		this.send({
			verb: `viewed_annotation`,
			type: `${this.baseUri}annotation`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}text`]: text,
				[`${this.baseUri}annotationDocId`]: annotationDocId,
			},
		})
	}

	enterFullscreen = time => {
		this.send({
			verb: `enter_fullscreen`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	exitFullscreen = time => {
		this.send({
			verb: `exit_fullscreen`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	enableCaptionTrack = (captionTrack, time) => {
		this.send({
			verb: `enabled_closed_caption`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}captionTrack`]: captionTrack.label,
			},
		})
	}

	disableCaptionTrack = (captionTrack, time) => {
		this.send({
			verb: `disabled_closed_caption`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}captionTrack`]: captionTrack.label,
			},
		})
	}

	changeSpeed = (speedLevel, time) => {
		this.send({
			verb: `changed_speed`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
				[`${this.baseUri}speedLevel`]: speedLevel,
			},
		})
	}

	mute = time => {
		this.send({
			verb: `muted`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	unmute = time => {
		this.send({
			verb: `unmuted`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	changedResolution = time => {
		this.send({
			verb: `changed_resolution`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	watched = time => {
		this.send({
			verb: `watched`,
			type: `${this.baseUri}mediaPlayer`,
			extensions: {
				[`${this.baseUri}playerTime`]: time,
			},
		})
	}

	addListeners = player => {

		this.throttle = (func, wait) => {
			let timeout
			return args => {
				if (!timeout) {
					timeout = setTimeout(() => {
						timeout = null
						func.apply(this, args)
					}, wait)
				}
			}
		}

		player.addEventListener(`play`, this.throttle(() => {
			this.playClick(`${player.currentTime}`)
		}, 500))

		player.addEventListener(`pause`, () => {
			this.pauseClick(`${player.currentTime}`)
		})

		player.addEventListener(`ended`, () => {
			this.ended(`${player.currentTime}`)
		})

		player.addEventListener(`timejump`, e => {
			this.timeJump(`${e.detail.oldtime}${e.detail.newtime}`)
		})

		player.addEventListener(`captionJump`, () => {
			this.repeatCaption(`${player.currentTime}`)
		})

		player.addEventListener(`ratechange`, this.throttle(() => {
			this.rateChange(`${player.currentTime}`, `${player.playbackRate}`)
		}, 1000))

		player.addEventListener(`volumechange`, this.throttle(() => {
			return player.muted && this.volumeChange(`${player.currentTime}`, `${player.volume}`)
		}, 1000))

		player.addEventListener(`mute`, () => {
			this.mute(`${player.currentTime}`, `0`)
		})

		player.addEventListener(`unmute`, () => {
			this.unmute(`${player.currentTime}`, player.volume)
		})

		player.addEventListener(`enterfullscreen`, () => {
			this.enterFullscreen(`${player.currentTime}`)
		})

		player.addEventListener(`exitfullscreen`, () => {
			this.exitFullscreen(`${player.currentTime}`)
		})

		player.addEventListener(`enabletrack`, e => {
			this.enableCaptionTrack(e.detail.track)
		})

		player.addEventListener(`disabletrack`, e => {
			this.disableCaptionTrack(e.detail.track)
		})

		player.addEventListener(`watched`, () => {
			this.watched(`${player.currentTime}`)
		})
	}

	registerPage = args => {
		this.page = args.page
		this.course = args.course ? args.course : this.course
		this.user = args.user
		this.resourceName =
			args.resource ?
				args.resource.label
				:
				args.content ?
					args.content.name
					:
					this.resourceName

		this.addListeners(args.player)
	}

	connect = () => { }

	record = b => {
		this.record = !!b
	}
}

export default xApi
