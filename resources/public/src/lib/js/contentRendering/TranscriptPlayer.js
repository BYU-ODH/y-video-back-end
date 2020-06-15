import { Ayamel } from 'yvideojs'

// TODO: resizing

/* args: captionTracks, holder, sync, annotator */
const TranscriptPlayer = args => {

	let currentTime = 0
	const element = args.holder
	const tracks = args.captionTracks.filter(track => {
		return track.kind === `captions` ||
			track.kind === `subtitles` ||
			track.kind === `descriptions`
	})

	// replacing ractive variables
	let sync = args.sync || false
	let activeIndex = 0

	// Transcript DOM
	// const transcriptSelect = document.createElement(`div`),
	// 	syncButton = document.createElement(`button`),
	// 	transcriptContentHolder = document.createElement(`div`),
	// 	transcriptSelector = document.createElement(`select`),
	// 	iconAnchorElement = document.createElement(`i`)

	// const transcriptDisplay = document.createElement(`div`)

	// const initTranscriptPlayer = () => {
	// 	transcriptDisplay.classList.add(`transcriptDisplay`)
	// 	transcriptSelect.classList.add(`form-inline`)
	// 	transcriptSelect.classList.add(`transcriptSelect`)
	// 	transcriptContentHolder.classList.add(`transcriptContentHolder`)
	// 	iconAnchorElement.classList.add(`icon-anchor`)
	// 	syncButton.title = `Anchor transcript to media location`

	// 	syncButton.appendChild(iconAnchorElement)
	// 	syncButton.addEventListener(`click`, (e) => {
	// 		sync = !sync
	// 		if (sync)
	// 			syncButton.classList.add(`active`)
	// 		else
	// 			syncButton.classList.remove(`active`)

	// 	})
	// 	syncButton.setAttribute(`type`, `button`)
	// 	syncButton.classList.add(`btn`)
	// 	if (sync)
	// 		syncButton.classList.add(`active`)

	// 	transcriptSelect.appendChild(transcriptSelector)
	// 	transcriptSelect.appendChild(syncButton)
	// 	transcriptSelector.addEventListener(`change`, (e) => {
	// 		activeIndex = e.target.selectedIndex
	// 		transcriptContentHolder.querySelectorAll(`.transcriptContent`).forEach((t, i) => {
	// 			t.style.display = i === activeIndex ? `block` : `none`
	// 		})
	// 	})

	// 	transcriptDisplay.appendChild(transcriptSelect)
	// 	transcriptDisplay.appendChild(transcriptContentHolder)
	// 	element.appendChild(transcriptDisplay)
	// }

	const addTrack = ti => {
		if (tracks.length < ti + 1) return false
		const transcriptOption = document.createElement(`option`),
			transcriptContent = document.createElement(`div`),
			transcript = tracks[ti]

		transcriptOption.innerHTML = transcript.label
		// transcriptSelector.appendChild(transcriptOption)
		transcriptContent.style.display = activeIndex === ti ? `block` : `none`
		transcriptContent.setAttribute(`data-trackindex`, ti)
		transcriptContent.classList.add(`transcriptContent`)

		transcript.cues.forEach((cue, i) => {
			const q = document.createElement(`div`),
				html_cue = cue.getCueAsHTML()

			q.classList.add(`transcriptCue`)
			q.classList.add(Ayamel.Text.getDirection(html_cue.textContent))
			q.setAttribute(`data-trackindex`, ti)
			q.setAttribute(`data-cueindex`, i)
			q.appendChild(html_cue)
			q.addEventListener(`click`, (e) => {
				element.dispatchEvent(new CustomEvent(`cueclick`, { bubbles: true, detail: { track: transcript, cue } }))
			})
			transcriptContent.appendChild(q)
		})

		// transcriptContentHolder.appendChild(transcriptContent)
		return true
	}

	/*
	 * Define the module interface.
	 */
	Object.defineProperties(this, {
		sync: {
			set(value) {
				sync = !!value
				return sync
			},
			get() {
				return sync
			},
		},
		activeTranscript: {
			set(value) {
				activeIndex = value
			},
			get() {
				return activeIndex
			},
		},
		addEventListener: {
			value(event, callback, capture) {
				element.addEventListener(event, callback, capture || false)
			},
		},
		addTrack: {
			value(track) {
				if (track.kind !== `captions` &&
					track.kind !== `subtitles` &&
					track.kind !== `descriptions`
				) return
				if (~tracks.indexOf(track)) return
				tracks.push(track)
				return addTrack(tracks.indexOf(track))
			},
		},
		updateTrack: {
			value(track) {
				const i = tracks.indexOf(track)
				if (~i) console.log(`updateTrack not implemented. Report this if the Transcripts are not loading correctly.`)
			},
		},
		currentTime: {
			get() {
				return currentTime
			},
			set(value) {
				let top = 1 / 0
				let bottom = -1 / 0
				const track = tracks[activeIndex]

				currentTime = +value;
				[].forEach.call(document.querySelectorAll(`.transcriptContent[data-trackindex="${activeIndex}"] > .transcriptCue`),
					(node) => {
						const cue = track.cues[node.dataset.cueindex]
						node.classList[currentTime >= cue.startTime && currentTime <= cue.endTime ? `add` : `remove`](`active`)
					})
				// Possibly scroll
				if (!sync) return
				const activeCues = document.querySelectorAll(`.transcriptContent[data-trackindex="${activeIndex}"] > .active`)
				if (activeCues.length === 0) return;
				[].forEach.call(activeCues, (activeCue) => {
					top = Math.min(top, activeCue.offsetTop)
					bottom = Math.max(bottom, activeCue.offsetTop + activeCue.offsetHeight)
				})

				const parent = document.querySelector(`.transcriptContentHolder`)
				parent.scrollTop = (top - parent.offsetHeight + bottom) / 2 - parent.offsetTop
			},
		},
		update: {
			value() {
				console.log(`Transcript Player Update all tracks not implemented. Report this if the transcripts are not loading correctly.`)
			},
		},
	})
}

export default TranscriptPlayer