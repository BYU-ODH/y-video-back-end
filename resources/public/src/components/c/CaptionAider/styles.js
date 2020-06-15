import styled from 'styled-components'

import actions_undo from 'assets/ca_actions_undo.svg'
import actions_redo from 'assets/ca_actions_redo.svg'
import actions_clear_repeat from 'assets/ca_actions_clear_repeat.svg'
import actions_autocue_breakpoint from 'assets/ca_actions_autocue_breakpoint.svg'

import tracks_create from 'assets/ca_tracks_create.svg'
import tracks_edit from 'assets/ca_tracks_edit.svg'
import tracks_load from 'assets/ca_tracks_load.svg'
import tracks_save from 'assets/ca_tracks_save.svg'
import tracks_show from 'assets/ca_tracks_show.svg'

import tools_add_cue from 'assets/ca_tools_add_cue.svg'
import tools_delete from 'assets/ca_tools_delete.svg'
import tools_move from 'assets/ca_tools_move.svg'
import tools_reorder from 'assets/ca_tools_reorder.svg'
import tools_select from 'assets/ca_tools_select.svg'
import tools_set_repeat from 'assets/ca_tools_set_repeat.svg'
import tools_split from 'assets/ca_tools_split.svg'
import tools_time_shift from 'assets/ca_tools_time_shift.svg'

import settings_anchor_to_seeker from 'assets/ca_settings_anchor_to_seeker.svg'
import settings_auto_repeat from 'assets/ca_settings_auto_repeat.svg'
import settings_enable_repeat from 'assets/ca_settings_enable_repeat.svg'
import settings_move_after_add from 'assets/ca_settings_move_after_add.svg'

const Style = styled.div`
	padding-top: 8.4rem;
	padding-bottom: 15rem;
	overflow-y: scroll;
	height: calc(100vh - 23.4rem);

	background-color: #303030;

	& #player {
		padding-bottom: 0 !important;
	}

	& #timeline {
		padding: 0;
		margin: 0 50px;
	}

	& > div {
		& .ayamelPlayer,
		& .videoBox,
		& .mediaPlayer {
			width: 100% !important;
			height: 70vh;
		}
		& .sliderContainer {
			padding-bottom: 0 !important;
		}
	}

	& .tl-btn {
		height: 2rem;
		width: 2rem;

		padding: 0;
		margin: 0;

		border: none;
		background: transparent;
		cursor: pointer;
		outline: none;

		background-size: contain;

		text-indent: -9999px;
		overflow: hidden;
	}

	& .tl-btn > * {
		display: none;
	}

	& .tl-btn[title="Undo"] {
		background: url(${actions_undo}) center no-repeat;
	}

	& .tl-btn[title="Redo"] {
		background: url(${actions_redo}) center no-repeat;
	}

	& .tl-btn[title="Clear Repeat"] {
		background: url(${actions_clear_repeat}) center no-repeat;
	}

	& .tl-btn[title="AutoCue Breakpoint"] {
		background: url(${actions_autocue_breakpoint}) center no-repeat;
	}

	& .tl-btn[title="Create a new track"] {
		background: url(${tracks_create}) center no-repeat;
	}

	& .tl-btn[title="Edit track metadata"] {
		background: url(${tracks_edit}) center no-repeat;
	}

	& .tl-btn[title="Save tracks"] {
		background: url(${tracks_save}) center no-repeat;
	}

	& .tl-btn[title="Load track"] {
		background: url(${tracks_load}) center no-repeat;
	}

	& .tl-btn[title="Show track"] {
		background: url(${tracks_show}) center no-repeat;
	}

	& .tl-btn[title="Select Tool (S)"] {
		background: url(${tools_select}) center no-repeat;
	}

	& .tl-btn[title="Add Cue Tool (A)"] {
		background: url(${tools_add_cue}) center no-repeat;
	}

	& .tl-btn[title="Move Tool (M)"] {
		background: url(${tools_move}) center no-repeat;
	}

	& .tl-btn[title="Time Shift Tool (F)"] {
		background: url(${tools_time_shift}) center no-repeat;
	}

	& .tl-btn[title="Split Tool (Q)"] {
		background: url(${tools_split}) center no-repeat;
	}

	& .tl-btn[title="Delete Tool (D)"] {
		background: url(${tools_delete}) center no-repeat;
	}

	& .tl-btn[title="Set Repeat Tool (R)"] {
		background: url(${tools_set_repeat}) center no-repeat;
	}

	& .tl-btn[title="Reorder Tool (O)"] {
		background: url(${tools_reorder}) center no-repeat;
	}

	& .tl-btn[title="Enable Repeat"] {
		background: url(${settings_enable_repeat}) center no-repeat;
	}

	& .tl-btn[title="Anchor View to Seeker"] {
		background: url(${settings_anchor_to_seeker}) center no-repeat;
	}

	& .tl-btn[title="Auto Repeat"] {
		background: url(${settings_auto_repeat}) center no-repeat;
	}

	& .tl-btn[title="Move After Add"] {
		background: url(${settings_move_after_add}) center no-repeat;
	}

	& .tl-toolbar-holder {
		display: flex;
		justify-content: space-evenly;
		margin: 2rem 0;
	}

	& .tl-toolbar {
		display: flex;
		justify-content: space-between;
		align-items: center;
		color: white;
	}

	& #timeline > div > div.tl-toolbar-holder > div:nth-child(5) {
		display: none;
	}

	& .tl-timestamp {
		color: white;
		display: flex;
		align-content: center;
		min-width: 8rem;
	}

	& .tl-btn-group {
		display: flex;
		justify-content: space-evenly;
	}
`

export default Style
