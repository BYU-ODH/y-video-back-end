import React, { PureComponent } from 'react'
import { Wrapper } from './styles'

class Overlay extends PureComponent {

	state = {
		visible: false,
	}

	closeModal = e => {
		e.preventDefault()

		this.setState({
			visible: false,
		})

		setTimeout(() => {
			this.props.toggleOverlay()
		}, 250)

	}

	render() {
		return (
			<Wrapper visible={this.state.visible} out={!this.state.visible}>
				<div>
					<div>
						<h3>What is YVIDEO?</h3>
						<p>Y-Video is a web-based system for streaming video and audio content especially as it applies to learning a foreign language. It houses a collection of media all with the express purpose of helping you learn languages.</p>
					</div>
					<div>
						<h3>Text Helps</h3>
						<p>Additional aids are available to be used in conjunction with the media, such as captions and annotations. A key feature of the site enables learners to directly interact with subtitle tracks to quickly access translations for foreign words and phrases and view relevant annotations.</p>
					</div>
					<div>
						<h3>For School</h3>
						<p>Teachers or course designers are able to aggregate media into a single location and set up specific conditions in order to optimize the learning experience. Statistics and analytics are available to improve the learning experience.</p>
					</div>
					<button onClick={this.closeModal}>close</button>
				</div>
			</Wrapper>
		)
	}

	componentDidMount = () => {
		this.setState({
			visible: true,
		})
	}
}

export default Overlay
