package org.caleydo.view.subgraph;

import gleem.linalg.Vec2f;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.perspective.variable.PerspectiveInitializationData;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.IMultiTablePerspectiveBasedView;
import org.caleydo.core.view.ViewManager;
import org.caleydo.core.view.listener.AddTablePerspectivesEvent;
import org.caleydo.core.view.listener.AddTablePerspectivesListener;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.layout.LayoutManager;
import org.caleydo.core.view.opengl.layout.util.multiform.IMultiFormChangeListener;
import org.caleydo.core.view.opengl.layout.util.multiform.MultiFormRenderer;
import org.caleydo.core.view.opengl.layout2.AGLElementGLView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementAdapter;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.util.GLElementViewSwitchingBar;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.pathway.IPathwayRepresentation;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.VertexRepBasedContextMenuItem;
import org.caleydo.datadomain.pathway.data.PathwayTablePerspective;
import org.caleydo.datadomain.pathway.graph.PathwayPath;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.listener.PathwayPathSelectionEvent;
import org.caleydo.datadomain.pathway.listener.ShowPortalNodesEvent;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.subgraph.event.ShowNodeInfoEvent;
import org.eclipse.swt.widgets.Composite;

public class GLSubGraph extends AGLElementGLView implements IMultiTablePerspectiveBasedView, IGLRemoteRenderingView,
		IMultiFormChangeListener {

	public static String VIEW_TYPE = "org.caleydo.view.subgraph";

	public static String VIEW_NAME = "SubGraph";

	private LayoutManager layoutManager;

	private List<TablePerspective> tablePerspectives = new ArrayList<>();

	private Set<String> remoteRenderedPathwayMultiformViewIDs;

	private String pathEventSpace;

	private GLElementContainer baseContainer = new GLElementContainer(GLLayouts.flowHorizontal(10));
	private GLElementContainer root = new GLElementContainer(GLLayouts.LAYERS);
	private GLElementContainer pathwayColumn = new GLElementContainer(GLLayouts.flowVertical(10));
	private GLSubGraphAugmentation augmentation = new GLSubGraphAugmentation();
	private GLElementContainer nodeInfoContainer = new GLElementContainer(GLLayouts.flowHorizontal(10));

	private GLPathwayBackground currentActiveBackground = null;

	// private List<IPathwayRepresentation> pathwayRepresentations = new ArrayList<>();

	private PathEventSpaceHandler pathEventSpaceHandler = new PathEventSpaceHandler();

	/**
	 * All segments of the currently selected path.
	 */
	private List<PathwayPath> pathSegments = new ArrayList<>();

	/**
	 * Maps from the embeddingID to all associated {@link MultiFormRenderer}s and their wrapping {@link GLElement}.
	 */
	private Map<String, List<Pair<MultiFormRenderer, GLElement>>> multiFormRenderers = new HashMap<>();

	/**
	 * Constructor.
	 *
	 * @param glCanvas
	 * @param viewLabel
	 * @param viewFrustum
	 */
	public GLSubGraph(IGLCanvas glCanvas, Composite parentComposite, ViewFrustum viewFrustum) {

		super(glCanvas, parentComposite, viewFrustum, VIEW_TYPE, VIEW_NAME);
		pathEventSpace = GeneralManager.get().getEventPublisher().createUniqueEventSpace();
		GLElementContainer column = new GLElementContainer(GLLayouts.flowVertical(10));
		column.add(baseContainer);
		nodeInfoContainer.setSize(Float.NaN, 0);
		column.add(nodeInfoContainer);
		baseContainer.add(pathwayColumn, 0.4f);

		root.add(column);
		root.add(augmentation);

	}

	@Override
	public void init(GL2 gl) {
		super.init(gl);

	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedSubGraphView serializedForm = new SerializedSubGraphView();
		serializedForm.setViewID(this.getID());
		return serializedForm;
	}

	@Override
	public String toString() {
		return "Subgraph view";
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();
		eventListeners.register(AddTablePerspectivesEvent.class, new AddTablePerspectivesListener().setHandler(this));
		eventListeners.register(pathEventSpaceHandler, pathEventSpace);
	}

	@Override
	protected void destroyViewSpecificContent(GL2 gl) {
		gl.glDeleteLists(displayListIndex, 1);
		layoutManager.destroy(gl);
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.tableBased;
	}

	@Override
	public void addTablePerspective(TablePerspective newTablePerspective) {
		if (newTablePerspective != null) {
			tablePerspectives.add(newTablePerspective);
		}
		createRemoteRenderedViews();
	}

	@Override
	public void addTablePerspectives(List<TablePerspective> newTablePerspectives) {
		if (newTablePerspectives != null) {
			for (TablePerspective tablePerspective : newTablePerspectives) {
				if (tablePerspective != null) {
					tablePerspectives.add(tablePerspective);
				}
			}
		}
		createRemoteRenderedViews();
	}

	private void createRemoteRenderedViews() {
		if (remoteRenderedPathwayMultiformViewIDs == null) {

			PathwayDataDomain pathwayDataDomain = (PathwayDataDomain) DataDomainManager.get().getDataDomainByType(
					PathwayDataDomain.DATA_DOMAIN_TYPE);

			TablePerspective tablePerspective = tablePerspectives.get(0);

			Perspective oldRecordPerspective = tablePerspective.getRecordPerspective();
			Perspective newRecordPerspective = new Perspective(tablePerspective.getDataDomain(),
					oldRecordPerspective.getIdType());

			PerspectiveInitializationData data = new PerspectiveInitializationData();
			data.setData(oldRecordPerspective.getVirtualArray());

			newRecordPerspective.init(data);

			Perspective oldDimensionPerspective = tablePerspective.getDimensionPerspective();
			Perspective newDimensionPerspective = new Perspective(tablePerspective.getDataDomain(),
					oldDimensionPerspective.getIdType());
			data = new PerspectiveInitializationData();
			data.setData(oldDimensionPerspective.getVirtualArray());

			newDimensionPerspective.init(data);
			PathwayTablePerspective pathwayTablePerspective = new PathwayTablePerspective(
					tablePerspective.getDataDomain(), pathwayDataDomain, newRecordPerspective, newDimensionPerspective,
					PathwayManager.get().getPathwayByTitle("Glioma", EPathwayDatabaseType.KEGG));
			pathwayDataDomain.addTablePerspective(pathwayTablePerspective);

			List<TablePerspective> pathwayTablePerspectives = new ArrayList<>(1);
			pathwayTablePerspectives.add(pathwayTablePerspective);

			addMultiformRenderer(pathwayTablePerspectives, EEmbeddingID.PATHWAY_MULTIFORM.id(), pathwayColumn,
					Float.NaN);

			pathwayTablePerspective = new PathwayTablePerspective(tablePerspective.getDataDomain(), pathwayDataDomain,
					newRecordPerspective, newDimensionPerspective, PathwayManager.get().getPathwayByTitle(
							"Pathways in cancer", EPathwayDatabaseType.KEGG));
			pathwayDataDomain.addTablePerspective(pathwayTablePerspective);

			pathwayTablePerspectives.clear();
			pathwayTablePerspectives.add(pathwayTablePerspective);
			addMultiformRenderer(pathwayTablePerspectives, EEmbeddingID.PATHWAY_MULTIFORM.id(), pathwayColumn,
					Float.NaN);

			addMultiformRenderer(tablePerspectives, EEmbeddingID.PATH.id(), baseContainer, 0.6f);

		}
	}

	private void addMultiformRenderer(List<TablePerspective> tablePerspectives, String embeddingID,
			GLElementContainer parent, Object layoutData) {

		GLElementContainer backgroundContainer = new GLElementContainer(GLLayouts.LAYERS);
		backgroundContainer.setLayoutData(layoutData);
		GLElementContainer container = new GLElementContainer(GLLayouts.flowVertical(6));
		// GLPathwayBackground bg = new GLPathwayBackground();

		final GLPathwayBackground bg = new GLPathwayBackground();
		backgroundContainer.add(bg);
		backgroundContainer.add(container);

		bg.onPick(new APickingListener() {
			@Override
			protected void mouseOver(Pick pick) {
				if (currentActiveBackground != null && currentActiveBackground != bg) {
					currentActiveBackground.hovered = false;
					currentActiveBackground.repaint();
				}
				currentActiveBackground = bg;
				bg.hovered = true;
				bg.repaint();
			}
		});

		remoteRenderedPathwayMultiformViewIDs = ViewManager.get().getRemotePlugInViewIDs(VIEW_TYPE, embeddingID);

		// Different renderers should receive path updates from the beginning on, therefore no lazy creation.
		MultiFormRenderer renderer = new MultiFormRenderer(this, false);
		renderer.addChangeListener(this);

		for (String viewID : remoteRenderedPathwayMultiformViewIDs) {
			int id = renderer.addPluginVisualization(viewID, getViewType(), embeddingID, tablePerspectives,
					pathEventSpace);
			IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(renderer, id);
			if (pathwayRepresentation != null) {
				pathwayRepresentation.addVertexRepBasedContextMenuItem(new VertexRepBasedContextMenuItem("Show Info",
						ShowNodeInfoEvent.class, pathEventSpace));
			}
		}

		GLElementAdapter multiFormRendererAdapter = new GLElementAdapter(this, renderer);
		// multiFormRendererAdapter.onPick(pl);
		container.add(multiFormRendererAdapter);

		GLElementViewSwitchingBar viewSwitchingBar = new GLElementViewSwitchingBar(renderer);
		container.add(viewSwitchingBar);

		List<Pair<MultiFormRenderer, GLElement>> embeddingSpecificRenderers = multiFormRenderers.get(embeddingID);

		if (embeddingSpecificRenderers == null) {
			embeddingSpecificRenderers = new ArrayList<>();
			multiFormRenderers.put(embeddingID, embeddingSpecificRenderers);
		}
		embeddingSpecificRenderers.add(new Pair<MultiFormRenderer, GLElement>(renderer, multiFormRendererAdapter));

		parent.add(backgroundContainer);

	}

	@Override
	public List<TablePerspective> getTablePerspectives() {
		return new ArrayList<>(tablePerspectives);
	}

	@Override
	public void removeTablePerspective(int tablePerspectiveID) {
		// TODO: implement
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		// TODO: implement
		return null;
	}

	@Override
	public boolean isDataView() {
		return true;
	}

	@Override
	protected GLElement createRoot() {

		return root;
	}

	/**
	 * Gets the renderer of the specified {@link MultiFormRenderer} as {@link IPathwayRepresentation}.
	 *
	 * @param renderer
	 * @param id
	 *            id of the visualization in the renderer.
	 * @return The pathway representation or null, if the active renderer is no pathway representation.
	 */
	protected IPathwayRepresentation getPathwayRepresentation(MultiFormRenderer renderer, int id) {
		// int id = renderer.getActiveRendererID();
		IPathwayRepresentation pathwayRepresentation = null;

		if (renderer.isView(id)) {
			AGLView view = renderer.getView(id);
			if (view instanceof IPathwayRepresentation) {
				pathwayRepresentation = (IPathwayRepresentation) view;

			}
		} else {
			ALayoutRenderer layoutRenderer = renderer.getLayoutRenderer(id);
			if (layoutRenderer instanceof IPathwayRepresentation) {
				pathwayRepresentation = (IPathwayRepresentation) layoutRenderer;
			}
		}

		return pathwayRepresentation;
	}

	protected Rectangle2D getAbsoluteVertexLocation(IPathwayRepresentation pathwayRepresentation,
			PathwayVertexRep vertexRep, GLElement element) {

		Vec2f elementPosition = element.getAbsoluteLocation();
		Rectangle2D location = pathwayRepresentation.getVertexRepBounds(vertexRep);
		if (location != null) {
			return new Rectangle2D.Float((float) (location.getX() + elementPosition.x()),
					(float) (location.getY() + elementPosition.y()), (float) location.getWidth(),
					(float) location.getHeight());
			// return new Rectangle2D.Float((elementPosition.x()), (elementPosition.y()), (float) location.getWidth(),
			// (float) location.getHeight());

		}
		return null;
	}

	protected void updatePathLinks() {

		augmentation.clearRenderers();
		PathwayVertexRep start = null;
		PathwayVertexRep end = null;
		List<Pair<MultiFormRenderer, GLElement>> rendererList = multiFormRenderers.get(EEmbeddingID.PATHWAY_MULTIFORM
				.id());

		for (PathwayPath path : pathSegments) {
			if (start == null) {
				start = path.getPath().getEndVertex();
			} else {
				end = path.getPath().getStartVertex();
				// draw link
				//
				PathwayVertexRep referenceVertexRep = start;
				Rectangle2D referenceRectangle = null;
				for (Pair<MultiFormRenderer, GLElement> rendererPair : rendererList) {
					MultiFormRenderer renderer = rendererPair.getFirst();
					IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(renderer,
							renderer.getActiveRendererID());
					if (pathwayRepresentation != null
							&& pathwayRepresentation.getPathway() == referenceVertexRep.getPathway()) {
						referenceRectangle = getAbsoluteVertexLocation(pathwayRepresentation, referenceVertexRep,
								rendererPair.getSecond());
						break;
					}
				}
				if (referenceRectangle == null)
					return;

				for (Pair<MultiFormRenderer, GLElement> rendererPair : rendererList) {
					MultiFormRenderer renderer = rendererPair.getFirst();

					IPathwayRepresentation pathwayRepresentation = getPathwayRepresentation(renderer,
							renderer.getActiveRendererID());

					if (pathwayRepresentation != null) {

						// for (PathwayVertexRep vertexRep : vertexReps) {
						Rectangle2D rect = getAbsoluteVertexLocation(pathwayRepresentation, end,
								rendererPair.getSecond());
						if (rect != null) {
							augmentation.addRenderer(new GLSubGraphAugmentation.ConnectionRenderer(referenceRectangle,
									rect));
						}
						// }
					}
				}
				//
				start = path.getPath().getEndVertex();
				end = null;
			}
		}
	}

	@Override
	public void display(GL2 gl) {
		boolean updateAugmentation = false;
		if (isLayoutDirty)
			updateAugmentation = true;
		super.display(gl);
		// The augmentation has to be updated after the layout was updated in super; updating on relayout would be too
		// early, as the layout is not adapted at that time.
		if (updateAugmentation) {
			updatePathLinks();
		}
	}

	private class PathEventSpaceHandler {

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onShowPortalNodes(ShowPortalNodesEvent event) {

		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onPathSelection(PathwayPathSelectionEvent event) {
			pathSegments = event.getPathSegments();
			updatePathLinks();
		}

		@ListenTo(restrictExclusiveToEventSpace = true)
		public void onShowNodeInfo(ShowNodeInfoEvent event) {
			GLNodeInfo nodeInfo = new GLNodeInfo(event.getVertexRep());
			nodeInfo.setSize(80, 80);
			nodeInfoContainer.add(nodeInfo);
			nodeInfoContainer.setSize(Float.NaN, 80);
			// nodeInfoContainer.relayout();
		}

	}

	@Override
	public void activeRendererChanged(MultiFormRenderer multiFormRenderer, int rendererID, int previousRendererID) {
		isLayoutDirty = true;
	}

	@Override
	public void rendererAdded(MultiFormRenderer multiFormRenderer, int rendererID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rendererRemoved(MultiFormRenderer multiFormRenderer, int rendererID) {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroyed(MultiFormRenderer multiFormRenderer) {
		// TODO Auto-generated method stub

	}

}
