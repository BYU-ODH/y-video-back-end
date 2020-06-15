import React from 'react'

import Style from './styles'

const LazyImage =
	({
		src,
		alt = ``,
		height = `10rem`,
		width = `17.8rem`,
		heightSm = `50.5vw`,
		widthSm = `90vw`,
	}) => {
		return (
			<Style
				height={height}
				width={width}
				heightSm={heightSm}
				widthSm={widthSm}
			>
				{src &&
					<img
						src={src}
						alt={alt}
					/>
				}
			</Style>
		)
	}

export default LazyImage
