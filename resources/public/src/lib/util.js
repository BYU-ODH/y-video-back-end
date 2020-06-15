import { diff } from 'deep-object-diff'

export const getInitials = fullName => {
	let initials = fullName.match(/\b\w/g) || []
	initials = ((initials.shift() || ``) + (initials.pop() || ``)).toUpperCase()
	return initials
}

export const objectIsEmpty = o => {
	return Object.entries(o).length === 0 && o.constructor === Object
}

export const componentDidChange = async (
	toConsole = false,
	component = ``,
	method = ``,
	prevProps = {},
	nextProps = {},
	propsProperties = [],
	prevState = {},
	nextState = {},
	stateProperties = []
) => {

	const label = `[DEBUG] ${component}.${method}()`

	if (toConsole) logger.group(label)

	const propsDiff = diff(prevProps, nextProps)
	const stateDiff = diff(prevState, nextState)

	if (toConsole) logger.log(`props changes:`, propsDiff)
	if (toConsole) logger.log(`state changes:`, stateDiff)

	const propsChanged = propsProperties.reduce((acc, property) => ({
		...acc,
		[`props.${property} Changed`]: {
			value: propsDiff.hasOwnProperty(property),
		},
	}), {})

	const stateChanged = stateProperties.reduce((acc, property) => ({
		...acc,
		[`state.${property} Changed`]: {
			value: stateDiff.hasOwnProperty(property),
		},
	}), {})

	const propsKeys = Object.keys(propsChanged)
	const stateKeys = Object.keys(stateChanged)

	if (toConsole && propsKeys.length > 0) console.table(propsChanged)
	if (toConsole && stateKeys.length > 0) console.table(stateChanged)

	const changed =
		(
			propsKeys.length > 0 ?
				propsKeys.some(key => propsChanged[key].value)
				:
				false
		) || (
			stateKeys.length > 0 ?
				stateKeys.some(key => stateChanged[key].value)
				:
				false
		)

	if (toConsole) logger.logc(`${changed ? `RENDER` : `NO RENDER`}`, `background: ${changed ? `Maroon` : `Teal`}`)

	if (toConsole) logger.groupEnd(label)

	return changed
}

const logStyle = `background: transparent; color: white; font-weight: bold; padding: 2px 4px; border-radius: 2px;`
const infoStyle = `background: #61dafb; color: #282c34; font-weight: bold; padding: 2px 4px; border-radius: 2px;`
const warnStyle = `background: #ffbb17; color: #332b00; font-weight: bold; padding: 2px 4px; border-radius: 2px;`
const errorStyle = `background: #dc2727; color: #290000; font-weight: bold; padding: 2px 4px; border-radius: 2px;`

export const logger = {
	log: (message, variable) => message ? console.log(`%c${message}`, logStyle, variable ? variable : ``) : null,
	info: (message, variable) => message ? console.log(`%c${message}`, infoStyle, variable ? variable : ``) : null,
	warn: (message, variable) => message ? console.log(`%c${message}`, warnStyle, variable ? variable : ``) : null,
	error: (message, variable) => message ? console.log(`%c${message}`, errorStyle, variable ? variable : ``) : null,
	logc: (message, css, variable) => message ? console.log(`%c${message}`, `${logStyle} ${css}`, variable ? variable : ``) : null,
	group: (message) => message ? console.groupCollapsed(`%c${message}`, infoStyle) : null,
	groupEnd: (message) => message ? console.groupEnd(`%c${message}`, infoStyle) : null,
}

export const departments = [
	{
		code: `A HTG`,
		name: `American Heritage`,
	},
	{
		code: `ACC`,
		name: `Accounting`,
	},
	{
		code: `AEROS`,
		name: `Aerospace Studies`,
	},
	{
		code: `AFRIK`,
		name: `Afrikaans`,
	},
	{
		code: `AM ST`,
		name: `American Studies`,
	},
	{
		code: `ANES`,
		name: `Ancient Near Eastern Studies`,
	},
	{
		code: `ANTHR`,
		name: `Antrhopology`,
	},
	{
		code: `ARAB`,
		name: `Arabic`,
	},
	{
		code: `ARMEN`,
		name: `Armenian`,
	},
	{
		code: `ART`,
		name: `Art`,
	},
	{
		code: `ARTED`,
		name: `Art Education`,
	},
	{
		code: `ARTHC`,
		name: `Art History and Curatorial Studies`,
	},
	{
		code: `ASIAN`,
		name: `Asian Studies`,
	},
	{
		code: `ASL`,
		name: `American Sign Language`,
	},
	{
		code: `BIO`,
		name: `Biology`,
	},
	{
		code: `BULGN`,
		name: `Bulgarian`,
	},
	{
		code: `C S`,
		name: `Computer Science`,
	},
	{
		code: `CAMBO`,
		name: `Cambodian`,
	},
	{
		code: `CANT`,
		name: `Cantonese`,
	},
	{
		code: `CE EN`,
		name: `Civil and Environmental Engineering`,
	},
	{
		code: `CEBU`,
		name: `Cebuano`,
	},
	{
		code: `CFM`,
		name: `Construction and Facilities Management`,
	},
	{
		code: `CH EN`,
		name: `Chemical Engineering`,
	},
	{
		code: `CHEM`,
		name: `Chemistry and Biochemistry`,
	},
	{
		code: `CHIN`,
		name: `Chinese - Mandarin`,
	},
	{
		code: `CL CV`,
		name: `Classical Civilization`,
	},
	{
		code: `CLSCS`,
		name: `Classics`,
	},
	{
		code: `CMLIT`,
		name: `Comparative Literature`,
	},
	{
		code: `CMPST`,
		name: `Comparative Studies`,
	},
	{
		code: `COMD`,
		name: `Communication Disorders`,
	},
	{
		code: `COMMS`,
		name: `Communications`,
	},
	{
		code: `CPSE`,
		name: `Counseling Psychology and Special Education`,
	},
	{
		code: `CREOL`,
		name: `Hatian Creole`,
	},
	{
		code: `CSANM`,
		name: `Computer Science Animation`,
	},
	{
		code: `DANCE`,
		name: `Dance`,
	},
	{
		code: `DANSH`,
		name: `Danish`,
	},
	{
		code: `DES`,
		name: `Design`,
	},
	{
		code: `DESAN`,
		name: `Design - Animation`,
	},
	{
		code: `DESGD`,
		name: `Design - Graphic Design`,
	},
	{
		code: `DESIL`,
		name: `Design - Illustration`,
	},
	{
		code: `DESPH`,
		name: `Design - Photography`,
	},
	{
		code: `DIGHT`,
		name: `Digital Humanities and Technology`,
	},
	{
		code: `DUTCH`,
		name: `Dutch`,
	},
	{
		code: `EC EN`,
		name: `Electrical and Computer Engineering`,
	},
	{
		code: `ECE`,
		name: `Early Childhood Education`,
	},
	{
		code: `ECON`,
		name: `Economics`,
	},
	{
		code: `EDLF`,
		name: `Educational Leadership and Foundations`,
	},
	{
		code: `EIME`,
		name: `Educational Inquiry, Measurement, and Evaluation`,
	},
	{
		code: `EL ED`,
		name: `Elementary Education`,
	},
	{
		code: `ELANG`,
		name: `English Language`,
	},
	{
		code: `EMBA`,
		name: `Executive Master of Business Administration`,
	},
	{
		code: `ENG T`,
		name: `Engineering Technology`,
	},
	{
		code: `ENGL`,
		name: `English`,
	},
	{
		code: `ENT`,
		name: `Entrepreneurial Management`,
	},
	{
		code: `ESL`,
		name: `English as a Second Language`,
	},
	{
		code: `ESTON`,
		name: `Estonian`,
	},
	{
		code: `EUROP`,
		name: `European Studies`,
	},
	{
		code: `EXDM`,
		name: `Experience Design and Management`,
	},
	{
		code: `EXSC`,
		name: `Exercise Sciences`,
	},
	{
		code: `FIN`,
		name: `Finance`,
	},
	{
		code: `FINN`,
		name: `Finnish`,
	},
	{
		code: `FLANG`,
		name: `Foreign Language Courses`,
	},
	{
		code: `FNART`,
		name: `Fine Arts`,
	},
	{
		code: `FREN`,
		name: `French`,
	},
	{
		code: `GEOG`,
		name: `Geography`,
	},
	{
		code: `GEOL`,
		name: `Geological Sciences`,
	},
	{
		code: `GERM`,
		name: `German`,
	},
	{
		code: `GREEK`,
		name: `Greek (Classical)`,
	},
	{
		code: `GSCM`,
		name: `Global Supply Chain Management`,
	},
	{
		code: `GWS`,
		name: `Global Women's Studies`,
	},
	{
		code: `HAWAI`,
		name: `Hawaiian`,
	},
	{
		code: `HCOLL`,
		name: `Humanities College`,
	},
	{
		code: `HEB`,
		name: `Hebrew`,
	},
	{
		code: `HINDI`,
		name: `Hindi`,
	},
	{
		code: `HIST`,
		name: `History`,
	},
	{
		code: `HLTH`,
		name: `Public Health`,
	},
	{
		code: `HMONG`,
		name: `Hmong`,
	},
	{
		code: `HONRS`,
		name: `Honors Program`,
	},
	{
		code: `HRM`,
		name: `Human Resource Management`,
	},
	{
		code: `HUNG`,
		name: `Hungarian`,
	},
	{
		code: `IAS`,
		name: `International and Area Studies`,
	},
	{
		code: `ICLND`,
		name: `Icelandic`,
	},
	{
		code: `ICS`,
		name: `International Cinema Studies`,
	},
	{
		code: `IHUM`,
		name: `Interdisciplinary Humanities`,
	},
	{
		code: `INDES`,
		name: `Industrial Design`,
	},
	{
		code: `INDON`,
		name: `Indonesian`,
	},
	{
		code: `IP&amp;T`,
		name: `Instructional Psychology and Technology`,
	},
	{
		code: `IS`,
		name: `Information Systems`,
	},
	{
		code: `IT&amp;C`,
		name: `Information Technology and Cybersecurity`,
	},
	{
		code: `IT`,
		name: `Information Technology`,
	},
	{
		code: `ITAL`,
		name: `Italian`,
	},
	{
		code: `JAPAN`,
		name: `Japanese`,
	},
	{
		code: `KICHE`,
		name: `K'iche`,
	},
	{
		code: `KIRIB`,
		name: `Kiribati`,
	},
	{
		code: `KOREA`,
		name: `Korean`,
	},
	{
		code: `LATIN`,
		name: `Latin (Classical)`,
	},
	{
		code: `LATVI`,
		name: `Latvian`,
	},
	{
		code: `LAW`,
		name: `Law`,
	},
	{
		code: `LFSCI`,
		name: `Life Sciences`,
	},
	{
		code: `LING`,
		name: `Linguistics`,
	},
	{
		code: `LITHU`,
		name: `Lithuanian`,
	},
	{
		code: `LT AM`,
		name: `Latin American Studies`,
	},
	{
		code: `M COM`,
		name: `Management Communication`,
	},
	{
		code: `MALAG`,
		name: `Malagasy`,
	},
	{
		code: `MALAY`,
		name: `Malay`,
	},
	{
		code: `MATH`,
		name: `Mathematics`,
	},
	{
		code: `MBA`,
		name: `Business Administration`,
	},
	{
		code: `ME EN`,
		name: `Mechanical Engineering`,
	},
	{
		code: `MESA`,
		name: `Middle East Studies/Arabic`,
	},
	{
		code: `MFGEN`,
		name: `Manufacturing Engineering`,
	},
	{
		code: `MFG`,
		name: `Manufacturing`,
	},
	{
		code: `MFHD`,
		name: `Marriage, Family, and Human Development`,
	},
	{
		code: `MFT`,
		name: `Marriage and Family Therapy`,
	},
	{
		code: `MIL S`,
		name: `Military Science`,
	},
	{
		code: `MKTG`,
		name: `Marketing`,
	},
	{
		code: `MMBIO`,
		name: `Microbiology and Molecular Biology`,
	},
	{
		code: `MPA`,
		name: `Public Management`,
	},
	{
		code: `MSB`,
		name: `Marriott School of Business`,
	},
	{
		code: `MTHED`,
		name: `Mathematics Education`,
	},
	{
		code: `MUSIC`,
		name: `Music`,
	},
	{
		code: `NAVAJ`,
		name: `Navajo`,
	},
	{
		code: `NDFS`,
		name: `Nutrition, Dietetics, and Food Science`,
	},
	{
		code: `NES`,
		name: `Near Eastern Studies`,
	},
	{
		code: `NEURO`,
		name: `Neuroscience`,
	},
	{
		code: `NORWE`,
		name: `Norwegian`,
	},
	{
		code: `NURS`,
		name: `Nursing`,
	},
	{
		code: `PDBIO`,
		name: `Physiology and Developmental Biology`,
	},
	{
		code: `PERSI`,
		name: `Persian`,
	},
	{
		code: `PETE`,
		name: `Physical Education Teacher Education`,
	},
	{
		code: `PHIL`,
		name: `Philosophy`,
	},
	{
		code: `PHSCS`,
		name: `Physics and Astronomy`,
	},
	{
		code: `PHY S`,
		name: `Physical Science`,
	},
	{
		code: `PLANG`,
		name: `Professional Language`,
	},
	{
		code: `POLI`,
		name: `Political Science`,
	},
	{
		code: `POLSH`,
		name: `Polish`,
	},
	{
		code: `PORT`,
		name: `Portuguese`,
	},
	{
		code: `PSYCH`,
		name: `Psychology`,
	},
	{
		code: `PWS`,
		name: `Plant and Wildlife Sciences`,
	},
	{
		code: `QUECH`,
		name: `Quechua`,
	},
	{
		code: `REL A`,
		name: `Rel A - Ancient Scripture`,
	},
	{
		code: `REL C`,
		name: `Rel C - Church History and Doctrine`,
	},
	{
		code: `REL E`,
		name: `Rel E - Religious Education`,
	},
	{
		code: `ROM`,
		name: `Romanian`,
	},
	{
		code: `RUSS`,
		name: `Russian`,
	},
	{
		code: `SAMOA`,
		name: `Samoan`,
	},
	{
		code: `SC ED`,
		name: `Secondary Education`,
	},
	{
		code: `SCAND`,
		name: `Scandinavian Studies`,
	},
	{
		code: `SFL`,
		name: `School of Family Life`,
	},
	{
		code: `SLAT`,
		name: `Second Language Teaching`,
	},
	{
		code: `SLOVK`,
		name: `Slovak`,
	},
	{
		code: `SOC`,
		name: `Sociology`,
	},
	{
		code: `SOC W`,
		name: `Social Work`,
	},
	{
		code: `SPAN`,
		name: `Spanish`,
	},
	{
		code: `STAC`,
		name: `Student Activities`,
	},
	{
		code: `STAT`,
		name: `Statistics`,
	},
	{
		code: `STDEV`,
		name: `Student Development`,
	},
	{
		code: `STRAT`,
		name: `Strategic Management`,
	},
	{
		code: `SWAHI`,
		name: `Swahili`,
	},
	{
		code: `SWED`,
		name: `Swedish`,
	},
	{
		code: `SWELL`,
		name: `Student Wellness`,
	},
	{
		code: `T ED`,
		name: `Teacher Education`,
	},
	{
		code: `TAGAL`,
		name: `Tagalog`,
	},
	{
		code: `TAHTN`,
		name: `Tahitian`,
	},
	{
		code: `TECH`,
		name: `Technology`,
	},
	{
		code: `TEE`,
		name: `Technology and Engineering Education`,
	},
	{
		code: `TELL`,
		name: `Teaching English Language Learners`,
	},
	{
		code: `TES`,
		name: `Technology and Engineering Studies`,
	},
	{
		code: `TEST`,
		name: `Test`,
	},
	{
		code: `THAI`,
		name: `Thai`,
	},
	{
		code: `TMA`,
		name: `Theatre and Media Arts`,
	},
	{
		code: `TONGA`,
		name: `Tongan`,
	},
	{
		code: `TRM`,
		name: `Therapeutic Recreation and Management`,
	},
	{
		code: `TURK`,
		name: `Turkish`,
	},
	{
		code: `UNIV`,
		name: `University Requirements`,
	},
	{
		code: `VIET`,
		name: `Vietnamese`,
	},
	{
		code: `WELSH`,
		name: `Welsh`,
	},
	{
		code: `WRTG`,
		name: `Writing`,
	},
]