import React from 'react'

class YLex {
	getReprRenderer = name =>
	<>
		{name === `WAV` ?
			value => <audio controls><source src={value} type='audio/wav' /></audio>
			:
			value
		}
	</>

	processRepr = (name, values) => {
		const renderer = this.getReprRenderer(name)
		return values.map(renderer).join(`, `)
	}

	renderSense = sense =>
		<>
			{sense.definition}
			{sense.examples && sense.examples.length &&
				<>
					<br />
					<i>Examples:</i>
					{sense.examples.map(s =>
						<>
							<br />
							{s}
						</>
					)}
				</>
			}
			{sense.notes && sense.notes.length &&
				<>
					<br />
					<i>Notes:</i>
					{sense.notes.map(s =>
						<>
							<br />
							{s}
						</>
					)}
				</>
			}
		</>

	renderLemma = lemma => {

		const prefRep = lemma.representations[0],
			lemmaForm = lemma.forms[lemma.lemmaForm],
			repList = Object.keys(lemmaForm),
			formList = Object.keys(lemma.forms)

		return (
			<>
				<b>{this.processRepr(prefRep, lemmaForm[prefRep])}</b>
				{repList.length > 1 &&
					<dl>
						{repList.map(repr =>
							repr === prefRep &&
								<>
									<dt>{repr}</dt>
									<dd>{this.processRepr(repr, lemmaForm[repr])}</dd>
								</>
						)}
					</dl>
				}
				{lemma.pos && <div>{lemma.poss}</div>}
				{formList.length > 1 &&
					<>
					<div>
						<i>Other Forms:</i><dl>
							{formList.map(fname => {
								const form = lemma.forms[fname]
								return fname === lemma.lemmaForm &&
								<>
									<dt>{fname}</dt>
									<dd>
										{repList.length > 1 ?
											<dl>
												{Object.keys(form).map(repr =>
													<>
														<dt>{repr}</dt>
														<dd>{this.processRepr(repr, form[repr])}</dd>
													</>
												)}
											</dl>
											:
											<>
												{this.processRepr(repList[0], form[repList[0]])}
											</>
										}
									</dd>
								</>
							})}
						</dl>
					</div>
					</>
				}
				{lemma.senses && lemma.senses.length &&
					<div>
						<ol>
							{lemma.senses.map(sense => <li>{sense}</li>)}
						</ol>
					</div>
				}
				{lemma.sources.map(source =>
					<>
						<div class='source'>{source.attribution}</div>
						<br />
					</>
				)}
			</>
		)
	}

	renderWord = (text, word) =>
		<>
			<b>{text.substring(word.start, word.end)}</b>
			<ol>
				{word.lemmas.map(lemma => <li>{lemma}</li>)}
			</ol>
		</>

	renderResult = result =>
		<>
			<div class='sourceText'><b>Original Text:</b> {result.text}</div>
			{result.translations &&
				<div class='translationResult'>
					<b>Free Translations:</b>
					<div class='translations'>
						{result.translations.map(trans =>
							<>
								"{trans.text}"
								<div class='source'>
									{trans.source.attribution}
								</div>
							</>
						)}
					</div>
				</div>
			}
			{result.words &&
				<div class='translationResult'>
					<b>Definitions:</b>
					<div class='translations'>
						{result.words.map(word => this.renderWord(result.text, word))}
					</div>
				</div>
			}
		</>

}

export default YLex
