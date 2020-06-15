import React, { PureComponent } from 'react'

import { SwitchToggle, AspectRadio, Tag, Spinner } from 'components/bits'

import Style, { InnerContainer, Column, Setting, RatioList } from './styles'

export class ContentSettings extends PureComponent {

	render() {

		const {
			showing,
			content,
			tag,
			loading,
		} = this.props.viewstate

		const {
			allowDefinitions,
			showCaptions,
			showAnnotations,
			aspectRatio,
			showWordList,
		} = content.settings

		const {
			keywords,
			description,
		} = content.resource

		const {
			handleToggle,
			handleDescription,
			handleRatio,
			addTag,
			removeTag,
			changeTag,
		} = this.props.handlers

		if (loading) return <Spinner/>

		return (
			<Style active={showing}>
				<InnerContainer>
					<Column>
						<h4>General</h4>
						<Setting>
							<p>Allow automatic definitions</p>
							<SwitchToggle on={allowDefinitions} setToggle={handleToggle} data_key='allowDefinitions' />
						</Setting>
						<Setting>
							<p>Show Word List</p>
							<SwitchToggle on={showWordList} setToggle={handleToggle} data_key='showWordList' />
						</Setting>
					</Column>

					<Column>
						<h4>Tags</h4>
						<div className='tags'>
							{keywords.map((item, index) => item === `` ? null : <Tag key={index} onClick={removeTag}>{item}</Tag>)}
						</div>
						<form onSubmit={addTag}>
							<input type='text' placeholder='Add tags...' onChange={changeTag} value={tag} className='tag-input' />
						</form>
					</Column>

					<Column>
						<h4>Description</h4>
						<textarea rows={4} onChange={handleDescription} value={description} />
					</Column>

					<Column>
						<h4>
							Notes
							<SwitchToggle on={showAnnotations} setToggle={handleToggle} data_key='showAnnotations' />
						</h4>
					</Column>

					<Column>
						<h4>
							Captions
							<SwitchToggle on={showCaptions} setToggle={handleToggle} data_key='showCaptions' />
						</h4>
					</Column>

					<Column>
						<h4>Aspect Ratio</h4>
						<RatioList>
							<div>
								<AspectRadio id={0} ratio='1.33' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.33`}>Standard</AspectRadio>
								<AspectRadio id={1} ratio='2.39' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `2.39`}>Widescreen</AspectRadio>
								<AspectRadio id={2} ratio='1.66' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.66`}>European Widescreen</AspectRadio>
								<AspectRadio id={3} ratio='1.85' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.85`}>US Widescreen</AspectRadio>
								<AspectRadio id={4} ratio='1.4142' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.4142`}>Lichtenberg</AspectRadio>
							</div>
							<div>
								<AspectRadio id={5} ratio='1.5' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.5`}>Classic Film</AspectRadio>
								<AspectRadio id={6} ratio='1.6' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.6`}>Credit Card</AspectRadio>
								<AspectRadio id={7} ratio='1.77' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.77`}>HD Video</AspectRadio>
								<AspectRadio id={8} ratio='1.618' onChange={handleRatio} contentId={content.id} selected={aspectRatio === `1.618`}>Golden</AspectRadio>
							</div>
						</RatioList>
					</Column>
				</InnerContainer>
			</Style>
		)
	}
}

export default ContentSettings
