package org.caleydo.core.manager.id;

/**
 * Types of managed objects
 * 
 * @author Marc Streit
 * @author Alexander Lex
 */
public enum EManagedObjectType
{
	STORAGE(10),
	STORAGE_NUMERICAL(11),
	STORAGE_NOMINAL(12),

	VIRTUAL_ARRAY(13),
	SELECTION(14),

	SET(15),

	VIEW(20),
	VIEW_GL_CANVAS(21),
	VIEW_SWT_PATHWAY(22),
	VIEW_SWT_GLYPH_DATAEXPORT(24),
	VIEW_SWT_GLYPH_MAPPINGCONFIGURATION(25),
	VIEW_SWT_BROWSER_GENERAL(26),
	VIEW_SWT_BROWSER_GENOME(36),
	VIEW_SWT_DATA_ENTITY_SEARCHER(27),
	VIEW_SWT_TABULAR_DATA_VIEWER(99),
	VIEW_SWT_DATA_EXCHANGER(99),
	VIEW_SWT_STORAGE_TABLE(99),
	VIEW_SWT_SELECTION_TABLE(99),
	VIEW_SWT_DATA_SET_EDITOR(99),
	VIEW_SWT_MIXER(99),
	VIEW_SWT_IMAGE(99),
	VIEW_SWT_UNDO_REDO(99),
	VIEW_SWT_JOGL_CONTAINER(99),

	GL_CANVAS(28),
	GL_EVENT_LISTENER(29),
	GL_PATHWAY(30),
	GL_PARALLEL_COORDINATES(31),
	GL_HEAT_MAP(32),
	GL_GLYPH(33),
	GL_REMOTE_RENDERING(34),
	GL_GLYPH_SLIDER(35),
	GL_HIER_HEAT_MAP(36),
	GL_CELL_LOCALIZATION(37),
	GL_SELECTION_PANEL(36),

	GUI_SWT_WINDOW(39),
	GUI_SWT_NATIVE_WIDGET(40),
	GUI_SWT_EMBEDDED_JOGL_WIDGET(41),
	GUI_SWT_EMBEDDED_JGRAPH_WIDGET(42),

	CMD_QUEUE(43),
	CMD_QUEUE_RUN(44),
	COMMAND(45),

	MEMENTO(46),

	PATHWAY(50),
	PATHWAY_ELEMENT(51),
	PATHWAY_VERTEX(52),
	PATHWAY_VERTEX_REP(53),
	PATHWAY_EDGE(54),
	PATHWAY_EDGE_REP(55),

	EVENT_PUBLISHER(60),
	EVENT_MEDIATOR(61),

	REMOTE_LEVEL_ELEMENT(64),

	GRAPH(65),
	GRAPH_ITEM(66),
	/**
	 * Type for ids used to connect different elements that have different ids.
	 */
	CONNECTION(70);

	private int iIdPrefix;

	/**
	 * Constructor.
	 * 
	 */
	private EManagedObjectType(final int iIdPrefix)
	{
		this.iIdPrefix = iIdPrefix;
	}

	public int getIdPrefix()
	{
		return iIdPrefix;
	}
}
