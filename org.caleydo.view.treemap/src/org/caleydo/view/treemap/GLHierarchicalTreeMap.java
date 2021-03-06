/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.treemap;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.graph.tree.ClusterNode;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.camera.CameraProjectionMode;
import org.caleydo.core.view.opengl.camera.ViewFrustum;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.ATableBasedView;
import org.caleydo.core.view.opengl.canvas.EDetailLevel;
import org.caleydo.core.view.opengl.canvas.GLMouseAdapter;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.canvas.remote.IGLRemoteRenderingView;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.treemap.layout.TreeMapRenderer;
import org.caleydo.view.treemap.listener.ZoomInEvent;
import org.caleydo.view.treemap.listener.ZoomInListener;
import org.caleydo.view.treemap.listener.ZoomOutEvent;
import org.caleydo.view.treemap.listener.ZoomOutListener;
import org.caleydo.view.treemap.renderstyle.TreeMapRenderStyle;
import org.eclipse.swt.widgets.Composite;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

/**
 * Extended Treemap for displaying multiple treemaps and zoom function.
 *
 * @author Alexander Lex
 * @author Michael Lafer
 */

public class GLHierarchicalTreeMap extends ATableBasedView implements IGLRemoteRenderingView {
	public static String VIEW_TYPE = "org.caleydo.view.treemap.hierarchical";

	public static String VIEW_NAME = "Hierarchical Treemap";

	private TreeMapRenderStyle renderStyle;

	TreeMapRenderer painter;

	// private EIDType eFieldDataType = EIDType.EXPRESSION_INDEX;
	// private EIDType eDimensionDataType = EIDType.EXPERIMENT_INDEX;

	// toggleable feature flags

	boolean bUseDetailLevel = true;

	private ATableBasedDataDomain dataDomain;

	private boolean bDisplayData = false;

	GLTreeMap mainTreeMapView;

	Vector<GLTreeMap> thumbnailTreemapViews = new Vector<GLTreeMap>(4);

	int thumbnailDisplayList;

	AnimationControle animationControle = new AnimationControle();

	private ZoomInListener zoomInListener;
	private ZoomOutListener zoomOutListener;

	float xMargin = 0.05f;
	float yMargin = 0.01f;

	Queue<GLTreeMap> uninitializedTreeMap = new LinkedList<GLTreeMap>();

	/**
	 * Constructor.
	 *
	 * @param glCanvas
	 * @param label
	 * @param viewFrustum
	 */
	public GLHierarchicalTreeMap(IGLCanvas glCanvas, ViewFrustum viewFrustum) {

		super(glCanvas, viewFrustum, VIEW_TYPE, VIEW_NAME);

		parentGLCanvas.addMouseListener(new GLMouseAdapter() {
			@Override
			public void mouseWheelMoved(IMouseEvent e) {
				mainTreeMapView.processMouseWheeleEvent(e);
			}
		});
	}

	@Override
	public void init(GL2 gl) {
		if (isDisplayListDirty) {
			displayListIndex = gl.glGenLists(1);
			isDisplayListDirty = false;
		}
		// renderStyle = new GeneralRenderStyle(viewFrustum);
		renderStyle = new TreeMapRenderStyle(viewFrustum);

		detailLevel = EDetailLevel.HIGH;

		// mainTreeMapView = createEmbeddedTreeMap();
		setMainTreeMapView(createEmbeddedTreeMap());
		mainTreeMapView.setRemotePickingManager(pickingManager, getID());

		mainTreeMapView.initRemote(gl, this, glMouseListener);
		mainTreeMapView.setDrawLabel(true);

		thumbnailDisplayList = gl.glGenLists(1);
	}

	@Override
	public void initLocal(GL2 gl) {

		init(gl);
	}

	@Override
	public void initData() {
		bDisplayData = tablePerspective.getRecordPerspective().getTree() != null;
		for (GLTreeMap view : thumbnailTreemapViews)
			view.initData();
	}

	@Override
	public void initRemote(final GL2 gl, final AGLView glParentView, final GLMouseListener glMouseListener) {

		// Register keyboard listener to GL2 canvas
		final Composite composite = glParentView.getParentGLCanvas().asComposite();
		composite.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				composite.addKeyListener(glKeyListener);
			}
		});

		setMouseListener(glMouseListener);

		init(gl);

	}

	@Override
	public void setDetailLevel(EDetailLevel detailLevel) {
		if (bUseDetailLevel) {
			super.setDetailLevel(detailLevel);
		}
	}

	@Override
	public void displayLocal(GL2 gl) {

		while (!uninitializedTreeMap.isEmpty()) {
			uninitializedTreeMap.poll().initRemote(gl, this, glMouseListener);

		}
		if (mainTreeMapView != null)
			mainTreeMapView.processEvents();

		for (GLTreeMap view : thumbnailTreemapViews)
			view.processEvents();

		pickingManager.handlePicking(this, gl);
		display(gl);
		checkForHits(gl);

	}

	@Override
	public void displayRemote(GL2 gl) {

	}

	@Override
	public void display(GL2 gl) {
		if (bDisplayData) {
			if (animationControle.isActive()) {
				animationControle.display(gl);
			} else {
				displayMainTreeMap(gl, thumbnailTreemapViews.size() > 0);
			}
		} else {
			renderSymbol(gl, EIconTextures.RADIAL_SYMBOL.getFileName(), 0.5f);
		}

	}

	public static final int MAX_THUMBNAILS = 3;
	public static final float THUMBNAIL_HEIGHT = 0.18f;

	void displayMainTreeMap(GL2 gl, boolean resized) {
		// if (thumbnailTreemapViews.size() > 0) {
		if (resized) {

			displayThumbnailTreemaps(gl);

			mainTreeMapView.getViewFrustum().setTop((float) (viewFrustum.getTop() - viewFrustum.getHeight() * 0.2));
			mainTreeMapView.getViewFrustum().setBottom(viewFrustum.getBottom());
			mainTreeMapView.getViewFrustum().setLeft(viewFrustum.getLeft());
			mainTreeMapView.getViewFrustum().setRight(viewFrustum.getRight());

			mainTreeMapView.displayRemote(gl);
		} else {

			mainTreeMapView.getViewFrustum().setTop(viewFrustum.getTop());
			mainTreeMapView.getViewFrustum().setBottom(viewFrustum.getBottom());
			mainTreeMapView.getViewFrustum().setLeft(viewFrustum.getLeft());
			mainTreeMapView.getViewFrustum().setRight(viewFrustum.getRight());
			mainTreeMapView.displayRemote(gl);
		}
	}

	void displayThumbnailTreemaps(GL2 gl) {
		int maxThumbNailViews = MAX_THUMBNAILS;

		// double thumbNailWidth = 0.26;
		double thumbNailWidth = (1 - xMargin * (maxThumbNailViews + 1)) / maxThumbNailViews;
		double thumbNailHeight = THUMBNAIL_HEIGHT;
		double xOffset = 0;
		// double yOffset = 0;
		if (thumbnailTreemapViews.size() > 3)
			drawArrow(gl, (float) xOffset, (float) (1.0f - yMargin - thumbNailHeight), (float) (xOffset + xMargin),
					1 - yMargin);
		for (int i = Math.max(0, thumbnailTreemapViews.size() - maxThumbNailViews); i < thumbnailTreemapViews.size(); i++) {
			xOffset += xMargin;

			GLTreeMap treemap = thumbnailTreemapViews.get(i);

			treemap.getViewFrustum().setLeft((float) (viewFrustum.getLeft() + viewFrustum.getWidth() * xOffset));
			treemap.getViewFrustum().setRight(
					(float) (viewFrustum.getLeft() + viewFrustum.getWidth() * (xOffset + thumbNailWidth)));
			treemap.getViewFrustum().setBottom(
					(float) (viewFrustum.getTop() - viewFrustum.getHeight() * (yMargin + thumbNailHeight)));
			treemap.getViewFrustum().setTop(viewFrustum.getTop() - viewFrustum.getHeight() * yMargin);

			// gl.glPushMatrix();
			// gl.glTranslated(viewFrustum.getWidth() * xOffset,
			// viewFrustum.getHeight() * (1.0 - yMargin - thumbNailHeight), 0);
			gl.glPushName(pickingManager.getPickingID(getID(), PickingType.TREEMAP_THUMBNAILVIEW_SELECTED, i));
			treemap.displayRemote(gl);
			gl.glPopName();
			// gl.glPopMatrix();

			xOffset += thumbNailWidth;

			drawArrow(gl, (float) xOffset, (float) (1.0f - yMargin - thumbNailHeight), (float) (xOffset + xMargin),
					1 - yMargin);
		}
	}

	/**
	 * Draws an arrow between the thumbnail treemaps.
	 *
	 * @param gl
	 * @param x
	 * @param y
	 * @param xmax
	 * @param ymax
	 */
	private void drawArrow(GL2 gl, float x, float y, float xmax, float ymax) {
		x = (x + 0.01f) * viewFrustum.getWidth();
		y = y * viewFrustum.getHeight();
		xmax = (xmax - 0.01f) * viewFrustum.getWidth();
		ymax = ymax * viewFrustum.getHeight();
		gl.glBegin(GL2.GL_POLYGON);
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(x, ymax, 0);
		gl.glVertex3f(xmax, y + (ymax - y) / 2, 0);
		gl.glEnd();

	}

	/**
	 * Displays a symbol when no data is available. Code from GLRadialHierarchy.
	 */
	@Override
	protected void renderSymbol(GL2 gl, String texture, float buttonSize) {

		float xButtonOrigin = viewFrustum.getLeft() + viewFrustum.getWidth() / 2 - buttonSize / 2;
		float yButtonOrigin = viewFrustum.getBottom() + viewFrustum.getHeight() / 2 - buttonSize / 2;
		Texture tempTexture = textureManager.getIconTexture(texture);
		tempTexture.enable(gl);
		tempTexture.bind(gl);

		TextureCoords texCoords = tempTexture.getImageTexCoords();

		gl.glPushAttrib(GL2.GL_CURRENT_BIT | GL2.GL_LINE_BIT);
		gl.glColor4f(1f, 1, 1, 1f);
		gl.glBegin(GL2.GL_POLYGON);

		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(xButtonOrigin, yButtonOrigin, 0.01f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(xButtonOrigin, yButtonOrigin + buttonSize, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(xButtonOrigin + buttonSize, yButtonOrigin + buttonSize, 0.01f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(xButtonOrigin + buttonSize, yButtonOrigin, 0.01f);
		gl.glEnd();
		gl.glPopAttrib();
		tempTexture.disable(gl);
	}

	public void zoomIn() {
		// System.out.println("zooming!!!!!");
		Set<Integer> elements = mainTreeMapView.getSelectionManager().getElements(SelectionType.SELECTION);
		if (elements.size() == 1 /* && thumbnailTreemapViews.size() < 3 */) {

			ClusterNode dataRoot = tablePerspective.getRecordPerspective().getTree()
					.getNodeByNumber(elements.iterator().next());

			mainTreeMapView.setRemotePickingManager(null, 0);
			mainTreeMapView.clearAllSelections();
			mainTreeMapView.getSelectionManager().addToType(SelectionType.SELECTION, dataRoot.getID());
			mainTreeMapView.setDrawLabel(false);

			@SuppressWarnings("unchecked")
			Vector<GLTreeMap> beginThumbnails = (Vector<GLTreeMap>) thumbnailTreemapViews.clone();

			mainTreeMapView.setInteractive(false);
			thumbnailTreemapViews.add(mainTreeMapView);

			GLTreeMap beginMainView = mainTreeMapView;

			// mainTreeMapView = createEmbeddedTreeMap();
			GLTreeMap treeMap = createEmbeddedTreeMap();
			setMainTreeMapView(treeMap);
			uninitializedTreeMap.add(treeMap);

			mainTreeMapView.setDrawLabel(true);

			mainTreeMapView.setRootClusterID(dataRoot.getID());
			mainTreeMapView.setZoomActive(true);
			mainTreeMapView.initData();

			animationControle.initAnimation(this, beginMainView, mainTreeMapView, beginThumbnails,
					thumbnailTreemapViews, AnimationControle.ZOOM_IN_ANIMATION);
			animationControle.setActive(true);

			setDisplayListDirty();

		}
	}

	public void zoomOut() {
		zoomOut(thumbnailTreemapViews.size() - 1);
	}

	private void zoomOut(int index) {
		if (thumbnailTreemapViews.size() > 0) {
			// mainTreeMapView = thumbnailTreemapViews.get(index);
			GLTreeMap beginMainView = mainTreeMapView;

			setMainTreeMapView(thumbnailTreemapViews.get(index));
			for (int i = thumbnailTreemapViews.size() - 1; i > index; i--) {
				thumbnailTreemapViews.get(i).unregisterEventListeners();

				thumbnailTreemapViews.remove(i);
			}

			Vector<GLTreeMap> beginThumbnails = (Vector<GLTreeMap>) thumbnailTreemapViews.clone();

			thumbnailTreemapViews.remove(index);

			mainTreeMapView.setDrawLabel(true);
			mainTreeMapView.setRemotePickingManager(pickingManager, getID());

			animationControle.initAnimation(this, beginMainView, mainTreeMapView, beginThumbnails,
					thumbnailTreemapViews, AnimationControle.ZOOM_OUT_ANIMATION);
			animationControle.setActive(true);

			setDisplayListDirty();
			System.out.println("remaining thumbnails: " + thumbnailTreemapViews.size());
		}
	}

	@Override
	public void setDisplayListDirty() {
		super.setDisplayListDirty();
		if (bDisplayData) {
			mainTreeMapView.setDisplayListDirty();
			for (GLTreeMap view : thumbnailTreemapViews)
				view.setDisplayListDirty();
		}
	}

	/**
	 * Invokes the zoom out function when a thumbnail treemap is clicked or delegates events to the embedded treemap.
	 */
	@Override
	protected void handlePickingEvents(PickingType pickingType, PickingMode pickingMode, int externalID, Pick pick) {
		// System.out.println(pickingType + " " + pickingMode + ": " +
		// externalID);
		if (pickingType == PickingType.TREEMAP_THUMBNAILVIEW_SELECTED && pickingMode == PickingMode.DOUBLE_CLICKED) {
			zoomOut(externalID);
		} else
			mainTreeMapView.handleRemotePickingEvents(pickingType, pickingMode, externalID, pick);

	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		SerializedTreeMapView serializedForm = new SerializedTreeMapView(this);
		return serializedForm;
	}

	@Override
	public void registerEventListeners() {
		super.registerEventListeners();

		zoomInListener = new ZoomInListener();
		zoomInListener.setHandler(this);
		eventPublisher.addListener(ZoomInEvent.class, zoomInListener);

		zoomOutListener = new ZoomOutListener();
		zoomOutListener.setHandler(this);
		eventPublisher.addListener(ZoomOutEvent.class, zoomOutListener);
	}

	@Override
	public void unregisterEventListeners() {
		super.unregisterEventListeners();

		if (zoomInListener != null) {
			eventPublisher.removeListener(zoomInListener);
			zoomInListener = null;
		}

		if (zoomOutListener != null) {
			eventPublisher.removeListener(zoomOutListener);
			zoomOutListener = null;
		}

		if (mainTreeMapView != null)
			mainTreeMapView.unregisterEventListeners();

		for (GLTreeMap view : thumbnailTreemapViews) {
			view.unregisterEventListeners();
		}

	}

	@Override
	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
		if (dataDomain != null) {
			if (tablePerspective.getRecordPerspective().getTree() != null) {
				for (GLTreeMap view : thumbnailTreemapViews) {
					view.setDataDomain(dataDomain);
				}
				bDisplayData = true;

			}
		} else
			bDisplayData = false;
	}

	private GLTreeMap createEmbeddedTreeMap() {

		float fHeatMapHeight = viewFrustum.getHeight();
		float fHeatMapWidth = viewFrustum.getWidth();
		ViewFrustum viewFrustum = new ViewFrustum(CameraProjectionMode.ORTHOGRAPHIC, 0, (int) fHeatMapHeight, 0,
				(int) fHeatMapWidth, -20, 20);

		GLTreeMap treemap = new GLTreeMap(parentGLCanvas, viewFrustum);
		treemap.setDataDomain(dataDomain);
		treemap.setRemoteRenderingGLView(this);
		treemap.registerEventListeners();
		treemap.setRemotePickingManager(pickingManager, getID());
		treemap.initData();

		return treemap;
	}

	@Override
	public List<AGLView> getRemoteRenderedViews() {
		ArrayList<AGLView> remoteRenderedViews = new ArrayList<AGLView>();
		remoteRenderedViews.add(mainTreeMapView);
		return remoteRenderedViews;
	}

	private void setMainTreeMapView(GLTreeMap treemap) {
		mainTreeMapView = treemap;
		mainTreeMapView.setInteractive(true);
	}



	@Override
	protected void destroyViewSpecificContent(GL2 gl) {
		// TODO Auto-generated method stub

	}

}
