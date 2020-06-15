import React from 'react'

const withLogs = Component => {
	return class extends React.PureComponent {
		render() {
			return <Component {...this.props} showLogs={true} />
		}
	}
}

export default withLogs