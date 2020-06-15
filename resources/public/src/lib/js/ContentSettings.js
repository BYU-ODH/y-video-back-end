class ContentSettings {

	settingsTemplate =
		`<form class="form-horizontal">\
			{{#controls:c}}\
			<div class="container-fluid">\
				<div class="form-group">\
					{{#(controlsSettings[c].include(context, content))}}\
					{{#(type == 'radio')}}\
					{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
					<div class="controls">\
						{{#items}}\
						<label style="display:block;">\
							<input type="radio" name="{{setting}}" value="{{.value}}">{{.text}}\
						</label>\
						{{/items}}\
					</div>\
					{{/type}}\
					{{#(type == 'checkbox')}}\
					{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
					<div class="controls">\
						<input type="checkbox" checked="{{setting}}">\
					</div>\
					{{/type}}\
					{{#(type == 'multicheck')}}\
					{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
					<div class="controls">\
						{{#items}}\
						<label style="display:block;">\
							<input type="checkbox" name="{{setting}}" value="{{.value}}">{{.text}}\
						</label>\
						{{/items}}\
					</div>\
					{{/type}}\
					{{#(type == 'button')}}\
					<div class="controls">\
						<button class="btn {{classes}}" on-click="click:{{name}}">{{label}}</button>\
					</div>\
					{{/type}}\
					{{#(type == 'superselect')}}\
					<div>\
						{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
						<SuperSelect icon="icon-globe" text="Select Language" value="{{setting}}" btnpos="left" multiple="true" options="{{items}}" modal="configurationModal">\
					</div>\
					{{/type}}\
					{{/include}}\
					{{^(controlsSettings[c].include(context, content))}}\
					{{#(type == 'radio')}}\
					{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
					{{#none}}<div class="controls">\{{none}}</div>{{/none}}\
					{{/type}}\
					{{#(type != 'radio')}}\
					{{#label}}<span class="control-label">{{label}}</span>{{/label}}\
					{{#none}}<div class="controls">\<i>{{none}}</i></div>{{/none}}\
					{{/type}}\
					{{/include}}\
				</div>\
			</div>\
			{{/controls}}\
		</form>\
		<style>\
			.controls { white-space: pre-line; }\
		</style>`

	predefined = {
		saveButton: {
			type: `button`,
			label: `Save`,
			name: `save`,
			// none: "Save option not available",
			classes: `btn-blue`,
			include: () => true,
			setting: () => { },
			items: () => { },
		},

		aspectRatio: {
			type: `radio`,
			label: `Player Aspect Ratio:`,
			name: `aspectRatio`,
			include: () => true,
			setting: content => content.settings.aspectRatio,
			items: () => Object.keys(Ayamel.aspectRatios).map(name => (
				{ text: name, value: Ayamel.aspectRatios[name] }
			)),
		},

		showCaptions: {
			type: `checkbox`,
			label: `Show Captions:`,
			name: `showCaptions`,
			none: `No captions to show`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => content.settings.showCaptions === `true`,
			items: () => { },
		},

		showAnnotations: {
			type: `checkbox`,
			label: `Show text annotations:`,
			name: `showAnnotations`,
			none: `No annotations to show`,
			include: content => !!content.enableableAnnotationDocuments.length,
			setting: content => content.settings.showAnnotations === `true`,
			items: () => { },
		},

		allowDefinitions: {
			type: `checkbox`,
			label: `Allow automatic definitions:`,
			name: `allowDefinitions`,
			none: `No tracks available`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => content.settings.allowDefinitions === `true`,
			items: () => { },
		},

		targetLanguages: {
			type: `superselect`,
			label: `Definition Languages:`,
			name: `targetLanguages`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => (content.settings.targetLanguages || ``).split(`,`).filter(s => !!s),
			items: () => {
				const langList = Object.keys(Ayamel.utils.p1map).map(p1 => {
					const code = Ayamel.utils.p1map[p1],
						engname = Ayamel.utils.getLangName(code, `eng`),
						localname = Ayamel.utils.getLangName(code, code)
					return {
						value: code,
						text: engname,
						desc: localname !== engname ?
							localname
							:
							void 0,
					}
				})

				langList.push({ value: `apc`, text: `North Levantine Arabic` })
				langList.push({ value: `arz`, text: `Egyptian Arabic` })

				return langList.sort((a, b) => a.text.localeCompare(b.text))
			},
		},

		showTranscripts: {
			type: `checkbox`,
			label: `Show Transcripts:`,
			name: `showTranscripts`,
			none: `No transcripts to show`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => content.settings.showTranscripts === `true`,
			items: () => { },
		},

		showWordList: {
			type: `checkbox`,
			label: `Show Word List:`,
			name: `showWordList`,
			none: `No wordlists to show`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => content.settings.showWordList === `true`,
			items: () => { },
		},

		enabledCaptionTracks: {
			type: `multicheck`,
			label: `Enabled Caption Tracks:`,
			name: `captionTracks`,
			none: `No captions to enable`,
			include: content => !!content.enableableCaptionTracks.length,
			setting: content => (content.settings.captionTrack || ``).split(`,`).filter(s => !!s),
			items: content => content.enableableCaptionTracks.map(resource => ({
				text: `${resource.title} (${Ayamel.utils.getLangName(resource.languages.iso639_3[0])})`,
				value: resource.id,
			})),
		},

		enabledAnnotations: {
			type: `multicheck`,
			label: `Enabled Annotations:`,
			name: `annotationDocs`,
			none: `No annotations to enable`,
			include: content => !!content.enableableAnnotationDocuments.length,
			setting: content => (content.settings.annotationDocument || ``).split(`,`).filter(s => !!s),
			items: content => content.enableableAnnotationDocuments.map(resource => ({
				text: `${resource.title} (${Ayamel.utils.getLangName(resource.languages.iso639_3[0])})`,
				value: resource.id,
			})),
		},

		visibility: {
			type: `radio`,
			label: `Visibility:`,
			name: `visibility`,
			include: () => true,
			setting: content => {
				return content.visibility || 1
			},
			items: () => [
				{
					text: `Private`,
					value: 1,
				},
				{
					text: `Tightly Restricted (Me and courses I add this to can see this)`,
					value: 2,
				},
				{
					text: `Loosely Restricted (Me, teachers, and courses we add this to can see this)`,
					value: 3,
				},
				{
					text: `Public (Everybody can see this)`,
					value: 4,
				},
			],
		},
	}

	settings = {
		video: [this.predefined.aspectRatio, this.predefined.allowDefinitions, this.predefined.targetLanguages, this.predefined.showTranscripts, this.predefined.showWordList, this.predefined.showCaptions, this.predefined.enabledCaptionTracks, this.predefined.showAnnotations, this.predefined.enabledAnnotations, this.predefined.visibility, this.predefined.saveButton],
		audio: [this.predefined.aspectRatio, this.predefined.showCaptions, this.predefined.allowDefinitions, this.predefined.targetLanguages, this.predefined.showAnnotations, this.predefined.showTranscripts, this.predefined.enabledCaptionTracks, this.predefined.enabledAnnotations, this.predefined.visibility, this.predefined.saveButton],
		image: [this.predefined.aspectRatio, this.predefined.showCaptions, this.predefined.allowDefinitions, this.predefined.targetLanguages, this.predefined.showAnnotations, this.predefined.showTranscripts, this.predefined.enabledCaptionTracks, this.predefined.enabledAnnotations, this.predefined.visibility, this.predefined.saveButton],
		text: [this.predefined.aspectRatio, this.predefined.allowDefinitions, this.predefined.targetLanguages, this.predefined.showAnnotations, this.predefined.enabledAnnotations, this.predefined.visibility, this.predefined.saveButton],
	}

	getResources = async ids => await ids.map(id => ResourceLibrary.load(id))

	getCaptionTracks = resource => this.getResources(resource.relations
		.filter(r => r.type === `transcript_of`)
		.map(r => r.subjectId))

	getAnnotationDocs = resource => this.getResources(resource.relations
		.filter(r => r.type === `references`)
		.map(r => r.subjectId))

	createControls = (config, context, content) => ({
		type: config.type,
		name: config.name,
		label: config.label,
		none: config.none,
		classes: config.classes,
		setting: config.setting(context, content),
		items: config.items(context, content),
	})

	/* args: courseId, owner, userId, content, resource, holder, action */
	ContentSettings = async args => {

		// Determine what content type we are dealing with
		const context = {
			courseId: args.courseId || 0,
			owner: args.owner || false,
			userId: args.userId || 0,
		}

		args.content.enableableCaptionTracks = await this.getCaptionTracks(args.resource)
		args.content.enableableAnnotationDocuments = await this.getAnnotationDocs(args.resource)

		// Create the form
		const controlsSettings = this.settings[args.content.contentType],
			controls = controlsSettings.map(config => this.createControls(config, context, args.content)),
			ractive = new Ractive({
				el: args.holder,
				template: settingsTemplate,
				data: {
					controls,
					content: args.content,
					context,
					controlsSettings,
				},
			})

		ractive.on(`click`, (evt, which) => {
			evt.original.preventDefault()
			if (which !== `save`) return

			// submit form data via ajax
			const xhr = new XMLHttpRequest(),
				fd = new FormData()

			fd.append(`contentType`, this.content.contentType)

			ractive.get(`controls`).forEach((control, index) => {
				if (control.type === `button`) return

				const setting = ractive.get(`controls[${index}].setting`),
					name = control.name;

				(
					setting instanceof Array ?
						setting
						:
						[setting]
				).forEach(value => fd.append(name, `${value}`))
			})

			xhr.addEventListener(`load`, () => document.location.reload(true))

			xhr.addEventListener(`error`, () => alert(`Something broke.`))

			xhr.open(`POST`, args.action)
			xhr.send(fd)
		})
	}
}

export default ContentSettings
