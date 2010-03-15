package org.caleydo.core.manager.picking;

/**
 * List of all possible pickable elements. Every type of element which should be pickable must be registered
 * here.
 * 
 * @author Alexander Lex
 */
public enum EPickingType {
	// bucket
	BUCKET_MOVE_IN_ICON_SELECTION,
	BUCKET_MOVE_OUT_ICON_SELECTION,
	BUCKET_MOVE_LEFT_ICON_SELECTION,
	BUCKET_MOVE_RIGHT_ICON_SELECTION,
	BUCKET_LOCK_ICON_SELECTION,
	BUCKET_REMOVE_ICON_SELECTION,
	BUCKET_DRAG_ICON_SELECTION,
	// BUCKET_SEARCH_PATHWAY,
	VIEW_SELECTION,
	/** A remote level element is the place-holder for a view, basically the wall behind a view */
	REMOTE_LEVEL_ELEMENT,
	MEMO_PAD_SELECTION,

	// selection panel
	SELECTION_PANEL_ITEM,

	// parallel coordinates
	POLYLINE_SELECTION,
	X_AXIS_SELECTION,
	Y_AXIS_SELECTION,
	GATE_TIP_SELECTION,
	GATE_BODY_SELECTION,
	GATE_BOTTOM_SELECTION,
	ADD_GATE,
	ADD_MASTER_GATE,
	REMOVE_GATE,
	PC_ICON_SELECTION,
	MOVE_AXIS,
	REMOVE_AXIS,
	DUPLICATE_AXIS,
	ANGULAR_UPPER,
	ANGULAR_LOWER,
	/** Type for selection of views in the parallel coordinates, currently the heat map */
	PCS_VIEW_SELECTION,
	REMOVE_NAN,

	// pathway manager
	PATHWAY_ELEMENT_SELECTION,
	PATHWAY_TEXTURE_SELECTION,

	// heat map
	HEAT_MAP_LINE_SELECTION,
	HEAT_MAP_STORAGE_SELECTION,

	// bookmark
	BOOKMARK_CONTAINER_HEADING,
	BOOKMARK_ELEMENT,

	// hierarchical heat map
	HIER_HEAT_MAP_FIELD_SELECTION,
	HIER_HEAT_MAP_TEXTURE_SELECTION,
	/** Button that triggers whether level 2 is large or small */
	HIER_HEAT_MAP_INFOCUS_SELECTION,
	HIER_HEAT_MAP_ACTIVATE_HORIZONTAL_DENDROGRAM,
	/** Button that triggers wheter storage dendrogram shows the whole tree, or only the tree till the cut-off */
	HIER_HEAT_MAP_ACTIVATE_STORAGE_DENDROGRAM,
	HIER_HEAT_MAP_CURSOR_LEVEL1,
	HIER_HEAT_MAP_BLOCK_CURSOR_LEVEL1,
	HIER_HEAT_MAP_CURSOR_LEVEL2,
	HIER_HEAT_MAP_BLOCK_CURSOR_LEVEL2,
	HIER_HEAT_MAP_EMBEDDED_HEATMAP_SELECTION,
	HIER_HEAT_MAP_GENE_DENDROGRAM_SELECTION,
	HIER_HEAT_MAP_EXPERIMENT_DENDROGRAM_SELECTION,
	HIER_HEAT_MAP_TEXTURE_CURSOR,
	HIER_HEAT_MAP_GENES_GROUP,
	HIER_HEAT_MAP_EXPERIMENTS_GROUP,

	// dendrogram
	DENDROGRAM_GENE_LEAF_SELECTION,
	DENDROGRAM_GENE_NODE_SELECTION,
	DENDROGRAM_EXPERIMENT_LEAF_SELECTION,
	DENDROGRAM_EXPERIMENT_NODE_SELECTION,
	DENDROGRAM_CUT_SELECTION,

	// glyph
	GLYPH_FIELD_SELECTION,
	// TODO: works only for glyph sliders now, new solution?
	SLIDER_SELECTION,

	// radial hierarchy
	RAD_HIERARCHY_PDISC_SELECTION,
	RAD_HIERARCHY_SLIDER_SELECTION,
	RAD_HIERARCHY_SLIDER_BODY_SELECTION,
	RAD_HIERARCHY_SLIDER_BUTTON_SELECTION,
	// tissue viewer
	TISSUE_SELECTION,

	// histogram
	HISTOGRAM_COLOR_LINE,
	HISTOGRAM_LEFT_SPREAD_COLOR_LINE,
	HISTOGRAM_RIGHT_SPREAD_COLOR_LINE,

	// scatterplot

	SCATTER_POINT_SELECTION,
	SCATTER_MATRIX_SELECTION,
	SCATTER_MAIN_ZOOM,

	// grouper
	GROUPER_GROUP_SELECTION,
	GROUPER_VA_ELEMENT_SELECTION,
	GROUPER_BACKGROUND_SELECTION,
	GROUPER_COLLAPSE_BUTTON_SELECTION,

	// Compare
	COMPARE_LEFT_EMBEDDED_VIEW_SELECTION,
	COMPARE_RIGHT_EMBEDDED_VIEW_SELECTION,
	COMPARE_LEFT_GROUP_SELECTION,
	COMPARE_RIGHT_GROUP_SELECTION,
	COMPARE_OVERVIEW_SLIDER_BODY_SELECTION,
	COMPARE_OVERVIEW_SLIDER_ARROW_UP_SELECTION,
	COMPARE_OVERVIEW_SLIDER_ARROW_DOWN_SELECTION,
	COMPARE_SET_BAR_ITEM_SELECTION,
	COMPARE_SET_BAR_SELECTION_WINDOW_SELECTION,

	CONTEXT_MENU_SELECTION,
	CONTEXT_MENU_SCROLL_DOWN,
	CONTEXT_MENU_SCROLL_UP,
	
	// datawindows
	DATAW_NODE;

}
