package org.caleydo.core.manager.picking;

import org.caleydo.core.manager.id.EManagedObjectType;

public enum EPickingType
{
	// bucket
	BUCKET_MOVE_IN_ICON_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	BUCKET_MOVE_OUT_ICON_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	BUCKET_MOVE_LEFT_ICON_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	BUCKET_MOVE_RIGHT_ICON_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	BUCKET_LOCK_ICON_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	// BUCKET_REMOVE_ICON_SELECTION,
	// BUCKET_SWITCH_ICON_SELECTION,
	// BUCKET_SEARCH_PATHWAY,
	VIEW_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),
	MEMO_PAD_SELECTION(EManagedObjectType.GL_REMOTE_RENDERING),

	// parallel coordinates
	POLYLINE_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	X_AXIS_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	Y_AXIS_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	LOWER_GATE_TIP_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	LOWER_GATE_BODY_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	LOWER_GATE_BOTTOM_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	// UPPER_GATE_SELECTION,
	PC_ICON_SELECTION(EManagedObjectType.GL_PARALLEL_COORDINATES),
	MOVE_AXIS_LEFT(EManagedObjectType.GL_PARALLEL_COORDINATES),
	MOVE_AXIS_RIGHT(EManagedObjectType.GL_PARALLEL_COORDINATES),
	REMOVE_AXIS(EManagedObjectType.GL_PARALLEL_COORDINATES),
	DUPLICATE_AXIS(EManagedObjectType.GL_PARALLEL_COORDINATES),
	ANGULAR_UPPER(EManagedObjectType.GL_PARALLEL_COORDINATES),
	ANGULAR_LOWER(EManagedObjectType.GL_PARALLEL_COORDINATES),

	// pathway manager
	PATHWAY_ELEMENT_SELECTION(EManagedObjectType.GL_PATHWAY),
	PATHWAY_TEXTURE_SELECTION(EManagedObjectType.GL_PATHWAY),

	// heat map
	HEAT_MAP_FIELD_SELECTION(EManagedObjectType.GL_HEAT_MAP),

	// hierarchical heat map
	HIER_HEAT_MAP_FIELD_SELECTION(EManagedObjectType.GL_HEAT_MAP),
	HIER_HEAT_MAP_TEXTURE_SELECTION(EManagedObjectType.GL_HEAT_MAP),
	
	// glyph
	GLYPH_FIELD_SELECTION(EManagedObjectType.GL_GLYPH),
	// TODO: works only for glyph sliders now, new solution?
	SLIDER_SELECTION(EManagedObjectType.GL_GLYPH_SLIDER);

	private EManagedObjectType viewType;

	private EPickingType(EManagedObjectType viewType)
	{
		this.viewType = viewType;
	}

	/**
	 * Returns the view type associated with the Picking Type
	 * 
	 * @return
	 */
	public EManagedObjectType getViewType()
	{
		return viewType;
	}
}
