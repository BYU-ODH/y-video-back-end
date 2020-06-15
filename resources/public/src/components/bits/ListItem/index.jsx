import React, { PureComponent } from 'react'
import { Link } from 'react-router-dom'

import { LazyImage } from 'components/bits'

import Style, { Icon } from './styles'

class ListItem extends PureComponent {
	render() {

		const { id, name, thumbnail, translation, captions, annotations } = this.props.data

		return (
			<Style>
				<Link to={`/player/${id}`}>
					<LazyImage src={thumbnail} height='3.5rem' width='5.5rem' heightSm='3.5rem' widthSm='5.5rem' />
					<div className='name'>
						<h4>{name}</h4>
						<ul>
							<Icon className='translation' checked={translation} />
							<Icon className='captions' checked={captions} />
							<Icon className='annotations' checked={annotations} />
						</ul>
					</div>
				</Link>
			</Style>
		)
	}
}

export default ListItem