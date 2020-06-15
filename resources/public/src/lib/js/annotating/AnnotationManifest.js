/**
 * An annotation manifest is basically a container for annotations.
 */
class AnnotationManifest {
	annotate(content, renderer) {

		this.annotations.forEach(annotation => {
			annotation.annotate(content, renderer)
		})

	}
}

export default AnnotationManifest
