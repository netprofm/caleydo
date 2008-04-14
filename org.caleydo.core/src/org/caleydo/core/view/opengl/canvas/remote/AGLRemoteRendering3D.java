package org.caleydo.core.view.opengl.canvas.remote;

import gleem.linalg.Rotf;
import gleem.linalg.Vec3f;
import gleem.linalg.Vec4f;
import gleem.linalg.open.Transform;

import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLEventListener;

import org.caleydo.core.command.CommandQueueSaxType;
import org.caleydo.core.command.data.CmdDataCreateSelectionSetMakro;
import org.caleydo.core.command.event.CmdEventCreateMediator;
import org.caleydo.core.command.view.opengl.CmdGlObjectPathway3D;
import org.caleydo.core.data.AUniqueManagedObject;
import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.set.selection.ISetSelection;
import org.caleydo.core.data.graph.core.PathwayGraph;
import org.caleydo.core.data.graph.item.vertex.PathwayVertexGraphItem;
import org.caleydo.core.data.mapping.EGenomeMappingType;
import org.caleydo.core.data.view.camera.IViewFrustum;
import org.caleydo.core.data.view.camera.ViewFrustumBase.ProjectionMode;
import org.caleydo.core.manager.IGeneralManager;
import org.caleydo.core.manager.IEventPublisher.MediatorType;
import org.caleydo.core.manager.ILoggerManager.LoggerType;
import org.caleydo.core.manager.event.mediator.IMediatorReceiver;
import org.caleydo.core.manager.event.mediator.IMediatorSender;
import org.caleydo.core.manager.event.mediator.MediatorUpdateType;
import org.caleydo.core.manager.type.ManagerObjectType;
import org.caleydo.core.manager.view.EPickingMode;
import org.caleydo.core.manager.view.EPickingType;
import org.caleydo.core.manager.view.Pick;
import org.caleydo.core.util.exception.CaleydoRuntimeException;
import org.caleydo.core.util.slerp.SlerpAction;
import org.caleydo.core.util.slerp.SlerpMod;
import org.caleydo.core.util.system.StringConversionTool;
import org.caleydo.core.util.system.SystemTime;
import org.caleydo.core.util.system.Time;
import org.caleydo.core.view.jogl.mouse.PickingJoglMouseListener;
import org.caleydo.core.view.opengl.canvas.AGLCanvasUser;
import org.caleydo.core.view.opengl.util.EIconTextures;
import org.caleydo.core.view.opengl.util.GLIconTextureManager;
import org.caleydo.core.view.opengl.util.JukeboxHierarchyLayer;
import org.caleydo.core.view.opengl.util.drag.GLDragAndDrop;
import org.caleydo.util.graph.EGraphItemHierarchy;
import org.caleydo.util.graph.EGraphItemProperty;
import org.caleydo.util.graph.IGraphItem;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;

/**
 * Abstract class that is able to remotely rendering views.
 * Subclasses implement the positioning of the views (bucket, jukebox, etc.).
 * 
 * @author Marc Streit
 * 
 */
public abstract class AGLRemoteRendering3D 
extends AGLCanvasUser
implements IMediatorReceiver, IMediatorSender 
{
	private static final int MAX_LOADED_VIEWS = 10;

	private static final int SLERP_RANGE = 1000;
	private static final int SLERP_SPEED = 1200;

	protected int iMouseOverViewID = -1;

	protected JukeboxHierarchyLayer underInteractionLayer;
	protected JukeboxHierarchyLayer stackLayer;
	protected JukeboxHierarchyLayer poolLayer;
	protected JukeboxHierarchyLayer transitionLayer;
	protected JukeboxHierarchyLayer spawnLayer;
	protected JukeboxHierarchyLayer memoLayer;

	private ArrayList<SlerpAction> arSlerpActions;

	private Time time;

	/**
	 * Slerp factor: 0 = source; 1 = destination
	 */
	private int iSlerpFactor = 0;

	protected AGLConnectionLineRenderer glConnectionLineRenderer;

	private int iNavigationMouseOverViewID_left = -1;
	private int iNavigationMouseOverViewID_right = -1;
	private int iNavigationMouseOverViewID_out = -1;
	private int iNavigationMouseOverViewID_in = -1;
	private int iNavigationMouseOverViewID_lock = -1;

	private boolean bEnableNavigationOverlay = false;

	// FIXME: should be a singleton
	private GLIconTextureManager glIconTextureManager;

	private ArrayList<Integer> iAlUninitializedPathwayIDs;

	private int iBucketEventMediatorID = -1;

	// Memo pad variables
	// TODO: move to own class
	protected static String TRASH_BIN_PATH = "resources/icons/trashcan_empty.png";

	private static final int MEMO_PAD_PICKING_ID = 1;
	protected static final int MEMO_PAD_TRASH_CAN_PICKING_ID = 2;

	protected TextRenderer textRenderer;

	protected Texture trashCanTexture;

	private GLDragAndDrop dragAndDrop;

	/**
	 * Constructor.
	 * 
	 */
	public AGLRemoteRendering3D(final IGeneralManager generalManager,
			final int iViewId,
			final int iGLCanvasID,
			final String sLabel,
			final IViewFrustum viewFrustum) {

		super(generalManager, iViewId, iGLCanvasID, sLabel, viewFrustum);

		pickingTriggerMouseAdapter.addGLCanvas(this);

		underInteractionLayer = new JukeboxHierarchyLayer(generalManager, 1);
		stackLayer = new JukeboxHierarchyLayer(generalManager, 4);
		poolLayer = new JukeboxHierarchyLayer(generalManager, MAX_LOADED_VIEWS);
		transitionLayer = new JukeboxHierarchyLayer(generalManager, 1);
		spawnLayer = new JukeboxHierarchyLayer(generalManager, 1);
		memoLayer = new JukeboxHierarchyLayer(generalManager, 5);

		underInteractionLayer.setParentLayer(stackLayer);
		stackLayer.setChildLayer(underInteractionLayer);
		stackLayer.setParentLayer(poolLayer);
		poolLayer.setChildLayer(stackLayer);

		arSlerpActions = new ArrayList<SlerpAction>();

		iAlUninitializedPathwayIDs = new ArrayList<Integer>();

		createEventMediator();

		dragAndDrop = new GLDragAndDrop();
		
		textRenderer = new TextRenderer(new Font("Arial",
				Font.BOLD, 24), false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#initLocal(javax.media.opengl.GL)
	 */
	public void initLocal(final GL gl) {

		init(gl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#initRemote(javax.media.opengl.GL,
	 *      int, org.caleydo.core.view.opengl.util.JukeboxHierarchyLayer,
	 *      org.caleydo.core.view.jogl.mouse.PickingJoglMouseListener)
	 */
	public void initRemote(final GL gl, final int iRemoteViewID,
			final JukeboxHierarchyLayer layer,
			final PickingJoglMouseListener pickingTriggerMouseAdapter) {

		this.pickingTriggerMouseAdapter = pickingTriggerMouseAdapter;

		init(gl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#init(javax.media.opengl.GL)
	 */
	public void init(final GL gl) {

		glIconTextureManager = new GLIconTextureManager(gl);

		time = new SystemTime();
		((SystemTime) time).rebase();

		retrieveContainedViews(gl);

		buildStackLayer(gl);
		buildMemoLayer(gl);
		updatePoolLayer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#displayLocal(javax.media.opengl.GL)
	 */
	public void displayLocal(final GL gl) {

		if (pickingTriggerMouseAdapter.wasRightMouseButtonPressed())
		{
			bEnableNavigationOverlay = !bEnableNavigationOverlay;

			glConnectionLineRenderer.enableRendering(!bEnableNavigationOverlay);
		}

		pickingManager.handlePicking(iUniqueId, gl, true);

		display(gl);

		if (pickingTriggerMouseAdapter.getPickedPoint() != null)
			dragAndDrop.setCurrentMousePos(gl, pickingTriggerMouseAdapter
					.getPickedPoint());

		if (pickingTriggerMouseAdapter.wasMouseReleased())
			dragAndDrop.stopDragAction();

		checkForHits(gl);

		pickingTriggerMouseAdapter.resetEvents();
		// gl.glCallList(iGLDisplayListIndexLocal);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#displayRemote(javax.media.opengl.GL)
	 */
	public void displayRemote(final GL gl) {

		display(gl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#display(javax.media.opengl.GL)
	 */
	public void display(final GL gl) {

		time.update();

		updatePoolLayer();
		doSlerpActions(gl);

//		// If user zooms to the bucket bottom all but the under
//		// interaction layer is _not_ rendered.
//		if (!bucketMouseWheelListener.isBucketBottomReached())
//		{

			glConnectionLineRenderer.render(gl);
		
			renderLayer(gl, transitionLayer);
			renderLayer(gl, stackLayer);
			renderLayer(gl, spawnLayer);
			renderPoolLayerBackground(gl);
			renderLayer(gl, underInteractionLayer);
			renderLayer(gl, poolLayer);

			renderMemoPad(gl);

			gl.glPushName(generalManager.getSingelton()
					.getViewGLCanvasManager().getPickingManager().getPickingID(
							iUniqueId, EPickingType.MEMO_PAD_SELECTION,
							MEMO_PAD_PICKING_ID));
			renderLayer(gl, memoLayer);
			gl.glPopName();
//		}
			
		
	}

	private void retrieveContainedViews(final GL gl) {

		Iterator<GLEventListener> iterGLEventListener = generalManager
				.getSingelton().getViewGLCanvasManager()
				.getAllGLEventListeners().iterator();

		while (iterGLEventListener.hasNext())
		{
			AGLCanvasUser tmpGLEventListener = (AGLCanvasUser) iterGLEventListener
					.next();

			if (tmpGLEventListener == this)
				continue;

			int iViewID = ((AGLCanvasUser) tmpGLEventListener).getId();

			if (underInteractionLayer.containsElement(-1))
			{
				underInteractionLayer.addElement(iViewID);
				underInteractionLayer.setElementVisibilityById(true, iViewID);

				tmpGLEventListener.initRemote(gl, iUniqueId,
						underInteractionLayer, pickingTriggerMouseAdapter);

			} else if (stackLayer.containsElement(-1))
			{
				stackLayer.addElement(iViewID);
				stackLayer.setElementVisibilityById(true, iViewID);

				tmpGLEventListener.initRemote(gl, iUniqueId, stackLayer,
						pickingTriggerMouseAdapter);
			} else if (poolLayer.containsElement(-1))
			{
				poolLayer.addElement(iViewID);
				poolLayer.setElementVisibilityById(true, iViewID);

				tmpGLEventListener.initRemote(gl, iUniqueId, poolLayer,
						pickingTriggerMouseAdapter);
			}

			// pickingTriggerMouseAdapter.addGLCanvas(tmpGLEventListener);
			pickingManager.getPickingID(iUniqueId, EPickingType.VIEW_SELECTION,
					iViewID);

			// Register new view to mediator
			// generalManager.getSingelton().getEventPublisher()
			// .registerSenderToMediator(iBucketEventMediatorID, iViewID);
			// generalManager.getSingelton().getEventPublisher()
			// .registerSenderToMediator(iBucketEventMediatorID, iViewID);

			ArrayList<Integer> arMediatorIDs = new ArrayList<Integer>();
			arMediatorIDs.add(iViewID);
			generalManager.getSingelton().getEventPublisher()
					.addSendersAndReceiversToMediator(
							generalManager.getSingelton().getEventPublisher()
									.getItemMediator(iBucketEventMediatorID),
							arMediatorIDs, arMediatorIDs,
							MediatorType.SELECTION_MEDIATOR,
							MediatorUpdateType.MEDIATOR_DEFAULT);
		}
	}
	protected abstract void buildStackLayer(final GL gl);
	protected abstract void buildMemoLayer(final GL gl);
	// FIXME: not necessary to be abstract
	protected abstract void updatePoolLayer();

	private void renderBucketWall(final GL gl) {

		gl.glColor4f(0.4f, 0.4f, 0.4f, 0.8f);
		gl.glLineWidth(4);

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();

		gl.glColor4f(0.9f, 0.9f, 0.9f, 0.4f);

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0, 0, -0.01f);
		gl.glVertex3f(0, 8, -0.01f);
		gl.glVertex3f(8, 8, -0.01f);
		gl.glVertex3f(8, 0, -0.01f);
		gl.glEnd();
	}

	private void renderLayer(final GL gl, final JukeboxHierarchyLayer layer) {

		Iterator<Integer> iterElementList = layer.getElementList().iterator();
		int iViewId = 0;
		int iLayerPositionIndex = 0;

		while (iterElementList.hasNext())
		{
			iViewId = iterElementList.next();

			renderEmptyBucketWall(gl, layer, iLayerPositionIndex);

			// Check if spot in layer is currently empty
			if (iViewId != -1)
			{
				gl.glPushName(pickingManager.getPickingID(iUniqueId,
						EPickingType.VIEW_SELECTION, iViewId));
				renderViewByID(gl, iViewId, layer);
				gl.glPopName();
			}

			iLayerPositionIndex++;
		}
	}

	private void renderViewByID(final GL gl, final int iViewID,
			final JukeboxHierarchyLayer layer) {

		// Init newly created pathways
		// FIXME: this specialization to pathways in the bucket is not good!
		if (!iAlUninitializedPathwayIDs.isEmpty() && arSlerpActions.isEmpty())
		{

			// Iterator<Integer> iterUninitializedViewIDs =
			// iAlUninitializedPathwayIDs.iterator();
			// while (iterUninitializedViewIDs.hasNext())
			// {
			// int iTmpPathwayID = iterUninitializedViewIDs.next();

			int iTmpPathwayID = iAlUninitializedPathwayIDs.get(0);

			// Check if pathway is already loaded in bucket
			if (!generalManager.getSingelton().getPathwayManager()
					.isPathwayVisible(iTmpPathwayID))
			{
				ArrayList<Integer> iArSetIDs = new ArrayList<Integer>();

				// FIXME: think of other way instead of hard coded set IDs
				iArSetIDs.add(85101);
				iArSetIDs.add(87101);
				iArSetIDs.add(86101);
				iArSetIDs.add(88101);

				// Create new selection set
				int iSelectionSetID = generalManager.getSingelton()
						.getSetManager().createId(ManagerObjectType.SET);
				CmdDataCreateSelectionSetMakro selectedSetCmd = (CmdDataCreateSelectionSetMakro) generalManager
						.getSingelton().getCommandManager()
						.createCommandByType(
								CommandQueueSaxType.CREATE_SET_SELECTION_MAKRO);
				selectedSetCmd.setAttributes(iSelectionSetID);
				selectedSetCmd.doCommand();

				iArSetIDs.add(iSelectionSetID);

				int iGeneratedViewID = generalManager.getSingelton()
						.getViewGLCanvasManager().createId(
								ManagerObjectType.VIEW);

				// Create Pathway3D view
				CmdGlObjectPathway3D cmdPathway = (CmdGlObjectPathway3D) generalManager
						.getSingelton().getCommandManager()
						.createCommandByType(
								CommandQueueSaxType.CREATE_GL_PATHWAY_3D);

				cmdPathway.setAttributes(iGeneratedViewID, iTmpPathwayID,
						iArSetIDs, ProjectionMode.ORTHOGRAPHIC, -4, 4, 4, -4,
						-20, 20);

				cmdPathway.doCommand();

				// FIXME: Do this in initRemote of the view
				// Register new view to mediator
				// generalManager.getSingelton().getEventPublisher()
				// .registerSenderToMediator(iBucketEventMediatorID,
				// iGeneratedViewID);
				// generalManager.getSingelton().getEventPublisher()
				// .registerSenderToMediator(iBucketEventMediatorID,
				// iGeneratedViewID);
				ArrayList<Integer> arMediatorIDs = new ArrayList<Integer>();
				arMediatorIDs.add(iGeneratedViewID);
				generalManager.getSingelton().getEventPublisher()
						.addSendersAndReceiversToMediator(
								generalManager.getSingelton()
										.getEventPublisher().getItemMediator(
												iBucketEventMediatorID),
								arMediatorIDs, arMediatorIDs,
								MediatorType.SELECTION_MEDIATOR,
								MediatorUpdateType.MEDIATOR_DEFAULT);

				if (underInteractionLayer.containsElement(-1))
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iGeneratedViewID, spawnLayer, underInteractionLayer);
					arSlerpActions.add(slerpActionTransition);

					((AGLCanvasUser) generalManager.getSingelton()
							.getViewGLCanvasManager().getItem(iGeneratedViewID))
							.initRemote(gl, iUniqueId, underInteractionLayer,
									pickingTriggerMouseAdapter);
				} else if (stackLayer.containsElement(-1))
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iGeneratedViewID, spawnLayer, stackLayer);
					arSlerpActions.add(slerpActionTransition);

					((AGLCanvasUser) generalManager.getSingelton()
							.getViewGLCanvasManager().getItem(iGeneratedViewID))
							.initRemote(gl, iUniqueId, stackLayer,
									pickingTriggerMouseAdapter);
				} else if (poolLayer.containsElement(-1))
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iGeneratedViewID, spawnLayer, poolLayer);
					arSlerpActions.add(slerpActionTransition);

					((AGLCanvasUser) generalManager.getSingelton()
							.getViewGLCanvasManager().getItem(iGeneratedViewID))
							.initRemote(gl, iUniqueId, poolLayer,
									pickingTriggerMouseAdapter);
				} else
				{
					generalManager.getSingelton().logMsg(
							this.getClass().getSimpleName()
									+ ": renderViewByID(): BUCKET IS FULL!!",
							LoggerType.VERBOSE);

					iAlUninitializedPathwayIDs.remove(0);
					return;
				}

				spawnLayer.addElement(iGeneratedViewID);
			}

			iAlUninitializedPathwayIDs.remove(0);
			
			generalManager.getSingelton().getViewGLCanvasManager().getSelectionManager().clear();
			
			// Trigger mouse over update if an entity is currently selected
			alSetSelection.get(0).updateSelectionSet(iUniqueId);
		}

		// Check if view is visible
		if (!layer.getElementVisibilityById(iViewID))
			return;

		AGLCanvasUser tmpCanvasUser = ((AGLCanvasUser) generalManager
				.getSingelton().getViewGLCanvasManager().getItem(iViewID));

		if (tmpCanvasUser == null)
			throw new CaleydoRuntimeException(
					"Cannot render canvas object which is null!");

		gl.glPushMatrix();

		Transform transform = layer.getTransformByElementId(iViewID);
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);

		gl.glTranslatef(translation.x(), translation.y(), translation.z());
		gl.glScalef(scale.x(), scale.y(), scale.z());
		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(),
				axis.z());

		if (layer.equals(underInteractionLayer) || layer.equals(stackLayer))
		{
			renderBucketWall(gl);
		}

		// Render transparent plane for picking views without texture (e.g. PC)
		if (layer.equals(poolLayer))
		{
			gl.glColor4f(1, 1, 1, 0);

			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex3f(0, 0, -0.01f);
			gl.glVertex3f(0, 8, -0.01f);
			gl.glVertex3f(8, 8, -0.01f);
			gl.glVertex3f(8, 0, -0.01f);
			gl.glEnd();
			
			String sRenderText = tmpCanvasUser.getInfo().get(1);
			
//			// Limit pathway name in length
//			if(sRenderText.length()> 35)
//				sRenderText = sRenderText.subSequence(0, 35) + "...";
//			
			textRenderer.begin3DRendering();	
			textRenderer.setColor(1,0,0,1);
			textRenderer.draw3D(sRenderText,
					0, 0, 0,
					1f);  // scale factor
			textRenderer.end3DRendering();
		}

		tmpCanvasUser.displayRemote(gl);

		if (layer.equals(stackLayer))
		{
			renderNavigationOverlay(gl, iViewID);
		}

		gl.glPopMatrix();
	}

	public void renderEmptyBucketWall(final GL gl,
			final JukeboxHierarchyLayer layer, final int iLayerPositionIndex) {

		gl.glPushMatrix();

		Transform transform = layer
				.getTransformByPositionIndex(iLayerPositionIndex);
		Vec3f translation = transform.getTranslation();
		Rotf rot = transform.getRotation();
		Vec3f scale = transform.getScale();
		Vec3f axis = new Vec3f();
		float fAngle = rot.get(axis);

		gl.glTranslatef(translation.x(), translation.y(), translation.z());
		gl.glScalef(scale.x(), scale.y(), scale.z());
		gl.glRotatef(Vec3f.convertRadiant2Grad(fAngle), axis.x(), axis.y(),
				axis.z());

		if (!layer.equals(transitionLayer) && !layer.equals(spawnLayer))
		{
			renderBucketWall(gl);
		}

		gl.glPopMatrix();
	}

	protected abstract void renderPoolLayerBackground(final GL gl);

	private void renderNavigationOverlay(final GL gl, final int iViewID) {

		if (!bEnableNavigationOverlay)
			return;

		glConnectionLineRenderer.enableRendering(false);

		EPickingType leftWallPickingType = null;
		EPickingType rightWallPickingType = null;
		EPickingType topWallPickingType = null;
		EPickingType bottomWallPickingType = null;

		Vec4f tmpColor_out = new Vec4f(0.9f, 0.9f, 0.9f, 0.9f);
		Vec4f tmpColor_in = new Vec4f(0.9f, 0.9f, 0.9f, 0.9f);
		Vec4f tmpColor_left = new Vec4f(0.9f, 0.9f, 0.9f, 0.9f);
		Vec4f tmpColor_right = new Vec4f(0.9f, 0.9f, 0.9f, 0.9f);
		Vec4f tmpColor_lock = new Vec4f(0.9f, 0.9f, 0.9f, 0.9f);

		Texture textureLock = glIconTextureManager
				.getIconTexture(EIconTextures.LOCK);
		Texture textureMoveLeft = null;
		Texture textureMoveRight = null;
		Texture textureMoveOut = null;
		Texture textureMoveIn = null;

		TextureCoords texCoords = textureLock.getImageTexCoords();

		if (iNavigationMouseOverViewID_lock == iViewID)
			tmpColor_lock.set(1, 0.3f, 0.3f, 0.9f);

		if (stackLayer.getPositionIndexByElementId(iViewID) == 0) // top
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;

			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);

			textureMoveIn = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
		} else if (stackLayer.getPositionIndexByElementId(iViewID) == 2) // bottom
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;

			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);

			textureMoveIn = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
		} else if (stackLayer.getPositionIndexByElementId(iViewID) == 1) // left
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;

			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);

			textureMoveIn = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
		} else if (stackLayer.getPositionIndexByElementId(iViewID) == 3) // right
		{
			topWallPickingType = EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION;
			bottomWallPickingType = EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
			leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
			rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;

			if (iNavigationMouseOverViewID_out == iViewID)
				tmpColor_right.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_in == iViewID)
				tmpColor_left.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_left == iViewID)
				tmpColor_out.set(1, 0.3f, 0.3f, 0.9f);
			else if (iNavigationMouseOverViewID_right == iViewID)
				tmpColor_in.set(1, 0.3f, 0.3f, 0.9f);

			textureMoveIn = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
			textureMoveOut = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveLeft = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_DOWN);
			textureMoveRight = glIconTextureManager
					.getIconTexture(EIconTextures.ARROW_LEFT);
		}
		// else if (underInteractionLayer.containsElement(iViewID))
		// {
		// topWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
		// bottomWallPickingType =
		// EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION;
		// leftWallPickingType = EPickingType.BUCKET_MOVE_IN_ICON_SELECTION;
		// rightWallPickingType = EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION;
		// }

		gl.glLineWidth(4);

		// CENTER - NAVIGATION: LOCK
		gl.glPushName(pickingManager.getPickingID(iUniqueId,
				EPickingType.BUCKET_LOCK_ICON_SELECTION, iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glEnd();

		textureLock.enable();
		textureLock.bind();

		gl.glColor4f(tmpColor_lock.x(), tmpColor_lock.y(), tmpColor_lock.z(),
				tmpColor_lock.w());
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glEnd();

		textureLock.disable();

		gl.glPopName();

		// BOTTOM - NAVIGATION: MOVE IN
		gl.glPushName(pickingManager.getPickingID(iUniqueId,
				bottomWallPickingType, iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(8, 0, 0.02f);
		gl.glEnd();

		gl.glColor4f(tmpColor_in.x(), tmpColor_in.y(), tmpColor_in.z(),
				tmpColor_in.w());

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 0.05f, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(7.95f, 0.02f, 0.02f);
		gl.glEnd();

		textureMoveIn.enable();
		textureMoveIn.bind();
		// texCoords = textureMoveIn.getImageTexCoords();
		// gl.glColor4f(1,0.3f,0.3f,0.9f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 0.05f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(2.66f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(5.33f, 0.05f, 0.03f);
		gl.glEnd();

		textureMoveIn.disable();

		gl.glPopName();

		// RIGHT - NAVIGATION: MOVE RIGHT
		gl.glPushName(pickingManager.getPickingID(iUniqueId,
				rightWallPickingType, iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(8, 0, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(8, 8, 0.02f);
		gl.glEnd();

		gl.glColor4f(tmpColor_right.x(), tmpColor_right.y(),
				tmpColor_right.z(), tmpColor_right.w());

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(7.95f, 0.05f, 0.02f);
		gl.glVertex3f(5.33f, 2.66f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(7.95f, 7.95f, 0.02f);
		gl.glEnd();

		textureMoveRight.enable();
		textureMoveRight.bind();

		// gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(7.95f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(5.33f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(7.95f, 5.33f, 0.03f);
		gl.glEnd();

		textureMoveRight.disable();

		gl.glPopName();

		// LEFT - NAVIGATION: MOVE LEFT
		gl.glPushName(pickingManager.getPickingID(iUniqueId,
				leftWallPickingType, iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 0, 0.02f);
		gl.glVertex3f(0, 8, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glEnd();

		gl.glColor4f(tmpColor_left.x(), tmpColor_left.y(), tmpColor_left.z(),
				tmpColor_left.w());

		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 0.05f, 0.02f);
		gl.glVertex3f(0.05f, 7.95f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 2.66f, 0.02f);
		gl.glEnd();

		textureMoveLeft.enable();
		textureMoveLeft.bind();

		// gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(0.05f, 2.66f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(0.05f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(2.66f, 2.66f, 0.03f);
		gl.glEnd();

		textureMoveLeft.disable();

		gl.glPopName();

		// TOP - NAVIGATION: MOVE OUT
		gl.glPushName(pickingManager.getPickingID(iUniqueId,
				topWallPickingType, iViewID));

		gl.glColor4f(0.5f, 0.5f, 0.5f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(0, 8, 0.02f);
		gl.glVertex3f(8, 8, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glEnd();

		gl.glColor4f(tmpColor_out.x(), tmpColor_out.y(), tmpColor_out.z(),
				tmpColor_out.w());
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(0.05f, 7.95f, 0.02f);
		gl.glVertex3f(7.95f, 7.95f, 0.02f);
		gl.glVertex3f(5.33f, 5.33f, 0.02f);
		gl.glVertex3f(2.66f, 5.33f, 0.02f);
		gl.glEnd();

		textureMoveOut.enable();
		textureMoveOut.bind();

		// gl.glColor4f(0,1,0,1);
		gl.glBegin(GL.GL_POLYGON);
		gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
		gl.glVertex3f(2.66f, 7.95f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
		gl.glVertex3f(5.33f, 7.95f, 0.03f);
		gl.glTexCoord2f(texCoords.right(), texCoords.top());
		gl.glVertex3f(5.33f, 5.33f, 0.03f);
		gl.glTexCoord2f(texCoords.left(), texCoords.top());
		gl.glVertex3f(2.66f, 5.33f, 0.03f);
		gl.glEnd();

		textureMoveOut.disable();

		gl.glPopName();
	}

	private void doSlerpActions(final GL gl) {

		if (arSlerpActions.isEmpty())
			return;

		SlerpAction tmpSlerpAction = arSlerpActions.get(0);

		if (iSlerpFactor == 0)
		{
			tmpSlerpAction.start();

			if (tmpSlerpAction.getDestinationHierarchyLayer()
					.equals(stackLayer)
					|| tmpSlerpAction.getDestinationHierarchyLayer().equals(
							underInteractionLayer))
			{
				glConnectionLineRenderer.enableRendering(false);
			}

			// tmpSlerpAction.getOriginHierarchyLayer().setElementVisibilityById(false,
			// tmpSlerpAction.getOriginHierarchyLayer().getElementIdByPositionIndex(
			// tmpSlerpAction.getOriginPosIndex()));

			// Update layer in toolbox renderer
			((AGLCanvasUser) generalManager.getSingelton()
					.getViewGLCanvasManager().getItem(
							tmpSlerpAction.getElementId()))
					.getToolboxRenderer().updateLayer(
							tmpSlerpAction.getDestinationHierarchyLayer());
		}

		if (iSlerpFactor < SLERP_RANGE)
		{
			// Makes animation rendering speed independent
			iSlerpFactor += SLERP_SPEED * time.deltaT();

			if (iSlerpFactor > SLERP_RANGE)
				iSlerpFactor = SLERP_RANGE;
		}

		slerpView(gl, tmpSlerpAction);
	}

	private void slerpView(final GL gl, SlerpAction slerpAction) {

		int iViewId = slerpAction.getElementId();
		SlerpMod slerpMod = new SlerpMod();

		if ((iSlerpFactor == 0))
		{
			slerpMod.playSlerpSound();
		}

		Transform transform = slerpMod.interpolate(slerpAction
				.getOriginHierarchyLayer().getTransformByPositionIndex(
						slerpAction.getOriginPosIndex()), slerpAction
				.getDestinationHierarchyLayer().getTransformByPositionIndex(
						slerpAction.getDestinationPosIndex()),
				(float) iSlerpFactor / SLERP_RANGE);

		gl.glPushMatrix();

		slerpMod.applySlerp(gl, transform);

		((AGLCanvasUser) generalManager.getSingelton().getViewGLCanvasManager()
				.getItem(iViewId)).displayRemote(gl);

		gl.glPopMatrix();

		if (iSlerpFactor >= SLERP_RANGE)
		{
			arSlerpActions.remove(slerpAction);

			// if
			// (!slerpAction.getOriginHierarchyLayer().equals(slerpAction.getDestinationHierarchyLayer()))
			// {
			// Remove view from origin layer after slerping
			// slerpAction.getOriginHierarchyLayer().removeElement(iViewId);
			// }

			slerpAction.getDestinationHierarchyLayer()
					.setElementVisibilityById(true, iViewId);

			iSlerpFactor = 0;
		}

		// After last slerp action is done the line connections are turned on
		// again
		if (arSlerpActions.isEmpty())
		{
			glConnectionLineRenderer.enableRendering(true);

			generalManager.getSingelton().getViewGLCanvasManager()
					.getInfoAreaManager().enable(!bEnableNavigationOverlay);
		}
	}

	private void loadViewToUnderInteractionLayer(final int iViewID) {

		generalManager.getSingelton().logMsg(
						this.getClass().getSimpleName()
								+ ": loadPathwayToUnderInteractionPosition(): View with ID "
								+ iViewID + " is under interaction.",
						LoggerType.VERBOSE);

		// // Check if pathway is already under interaction
		// if (underInteractionLayer.containsElement(iViewID))
		// return;

		// Check if other slerp action is currently running
		if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
			return;

		arSlerpActions.clear();

		// // Check if under interaction layer is free
		// if (underInteractionLayer.getElementList().isEmpty())
		// {
		// // Slerp directly from pool to under interaction layer
		// SlerpAction slerpAction = new SlerpAction(iViewId, stackLayer,
		// false);
		// arSlerpActions.add(slerpAction);
		// }
		// else
		// {

		// // Check if stack layer has a free spot to switch out view under
		// interaction
		// if (stackLayer.containsElement(-1))
		// {
		// // Slerp current view back to layered view
		// if (!underInteractionLayer.getElementList().isEmpty())
		// {
		// // Slerp selected view to under interaction transition position
		// SlerpAction slerpActionTransition = new SlerpAction(
		// iViewID, poolLayer, transitionLayer);
		// arSlerpActions.add(slerpActionTransition);
		//				
		// // Slerp under interaction view to free spot in stack
		// SlerpAction reverseSlerpAction = new SlerpAction(
		// underInteractionLayer.getElementIdByPositionIndex(0),
		// underInteractionLayer, stackLayer);
		// arSlerpActions.add(reverseSlerpAction);
		//				
		// // Slerp selected view from transition position to under interaction
		// position
		// SlerpAction slerpAction = new SlerpAction(
		// iViewID, transitionLayer, underInteractionLayer);
		// arSlerpActions.add(slerpAction);
		// }
		// }
		// else
		// {
		// Check if view is already loaded in the stack layer
		if (stackLayer.containsElement(iViewID))
		{
			// Slerp selected view to under interaction transition position
			SlerpAction slerpActionTransition = new SlerpAction(iViewID,
					stackLayer, transitionLayer);
			arSlerpActions.add(slerpActionTransition);

			// Slerp under interaction view to free spot in stack
			SlerpAction reverseSlerpAction = new SlerpAction(
					underInteractionLayer.getElementIdByPositionIndex(0),
					underInteractionLayer, stackLayer);
			arSlerpActions.add(reverseSlerpAction);

			// Slerp selected view from transition position to under interaction
			// position
			SlerpAction slerpAction = new SlerpAction(iViewID, transitionLayer,
					underInteractionLayer);
			arSlerpActions.add(slerpAction);
		} else
		{
			// Slerp selected view to under interaction transition position
			SlerpAction slerpActionTransition = new SlerpAction(iViewID,
					poolLayer, transitionLayer);
			arSlerpActions.add(slerpActionTransition);

			if (!stackLayer.containsElement(-1))
			{
				// Slerp view from stack to pool
				SlerpAction reverseSlerpAction = new SlerpAction(stackLayer
						.getElementIdByPositionIndex(stackLayer
								.getNextPositionIndex()), stackLayer, true);
				arSlerpActions.add(reverseSlerpAction);
			}

			// Slerp under interaction view to free spot in stack
			SlerpAction reverseSlerpAction2 = new SlerpAction(
					underInteractionLayer.getElementIdByPositionIndex(0),
					underInteractionLayer, true);
			arSlerpActions.add(reverseSlerpAction2);

			// Slerp selected view from transition position to under interaction
			// position
			SlerpAction slerpAction = new SlerpAction(iViewID, transitionLayer,
					underInteractionLayer);
			arSlerpActions.add(slerpAction);
		}
		// }
		//		
		iSlerpFactor = 0;

		// // Slerp current pathway back to layered view
		// if (!underInteractionLayer.getElementList().isEmpty())
		// {
		// SlerpAction reverseSlerpAction = new SlerpAction(
		// underInteractionLayer.getElementIdByPositionIndex(0),
		// underInteractionLayer, true);
		//
		// arSlerpActions.add(reverseSlerpAction);
		// }
		//
		// SlerpAction slerpAction;
		//
		// // Prevent slerp action if pathway is already in layered view
		// if (!stackLayer.containsElement(iViewId))
		// {
		// // Slerp to layered pathway view
		// slerpAction = new SlerpAction(iViewId, poolLayer, false);
		//
		// arSlerpActions.add(slerpAction);
		// }
		//
		// // Slerp from layered to under interaction position
		// slerpAction = new SlerpAction(iViewId, stackLayer, false);
		//
		// arSlerpActions.add(slerpAction);
		// iSlerpFactor = 0;
		//
		// // bRebuildVisiblePathwayDisplayLists = true;
		// //selectedVertex = null;
	}

	@Override
	public void updateReceiver(Object eventTrigger) {

		// TODO Auto-generated method stub

	}

	@Override
	public void updateReceiver(Object eventTrigger, ISet updatedSet) 
	{
		generalManager.getSingelton().logMsg(
				this.getClass().getSimpleName()
						+ " ("+iUniqueId+"): updateReceiver(Object eventTrigger, ISet updatedSet): Update called by "
						+ eventTrigger.getClass().getSimpleName()+" ("+((AUniqueManagedObject)eventTrigger).getId()+")",
				LoggerType.VERBOSE);
		
		ISetSelection refSetSelection = (ISetSelection) updatedSet;

		refSetSelection.getReadToken();	
								
		ArrayList<Integer> iAlSelection = refSetSelection.getSelectionIdArray();
		ArrayList<Integer> iAlSelectionGroup = refSetSelection.getGroupArray();	
		
		ArrayList<Integer> iAlTmpSelectionId = new ArrayList<Integer>(2);
		ArrayList<Integer> iAlTmpGroupId = new ArrayList<Integer>(2);
		
		if (iAlSelection != null && iAlSelectionGroup != null)
		{			
			ArrayList<IGraphItem> alPathwayVertexGraphItem = new ArrayList<IGraphItem>();
			
			for (int iSelectionIndex = 0; iSelectionIndex < iAlSelection.size(); iSelectionIndex++)
			{			
				int iAccessionID = iAlSelection.get(iSelectionIndex);
				
				if (iAlSelectionGroup.get(iSelectionIndex) == -1)
				{
					generalManager.getSingelton().getViewGLCanvasManager().getSelectionManager().clear();
					continue;
				}
				else if (iAlSelectionGroup.get(iSelectionIndex) != 2)
					continue;
				
				String sAccessionCode = generalManager.getSingelton().getGenomeIdManager()
					.getIdStringFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_ACCESSION_CODE);
			
				System.out.println("Accession Code: " +sAccessionCode);
									
				int iNCBIGeneID = generalManager.getSingelton().getGenomeIdManager()
					.getIdIntFromIntByMapping(iAccessionID, EGenomeMappingType.ACCESSION_2_NCBI_GENEID);
	
				String sNCBIGeneIDCode = generalManager.getSingelton().getGenomeIdManager()
					.getIdStringFromIntByMapping(iNCBIGeneID, EGenomeMappingType.NCBI_GENEID_2_NCBI_GENEID_CODE);
			
				int iNCBIGeneIDCode = StringConversionTool.convertStringToInt(sNCBIGeneIDCode, -1);
				
				PathwayVertexGraphItem tmpPathwayVertexGraphItem = 
					((PathwayVertexGraphItem)generalManager.getSingelton().getPathwayItemManager().getItem(
						generalManager.getSingelton().getPathwayItemManager().getPathwayVertexGraphItemIdByNCBIGeneId(iNCBIGeneIDCode)));
			
				alPathwayVertexGraphItem.add(tmpPathwayVertexGraphItem);
				
				iAlTmpSelectionId.add(iAccessionID);
				iAlTmpGroupId.add(1); // mouse over
			}
			
			if (!alPathwayVertexGraphItem.isEmpty())
			{
				loadDependentPathways(alPathwayVertexGraphItem);
			}
			
			alSetSelection.get(0).getWriteToken();
			alSetSelection.get(0).mergeSelection(iAlTmpSelectionId, iAlTmpGroupId, null);
			alSetSelection.get(0).returnWriteToken();
		}
		// Check if update set contains a pathway that was searched by the user
		else if (refSetSelection.getOptionalDataArray() != null)
		{	
			int iPathwayIDToLoad = refSetSelection.getOptionalDataArray().get(0);
			iAlUninitializedPathwayIDs.add(iPathwayIDToLoad);
		}

		refSetSelection.returnReadToken();
	}

	public void loadDependentPathways(final List<IGraphItem> alVertex) {

		// Remove pathways from stacked layer view
		// poolLayer.removeAllElements();

		Iterator<IGraphItem> iterPathwayGraphItem = alVertex.iterator();
		Iterator<IGraphItem> iterIdenticalPathwayGraphItemRep = null;

		IGraphItem pathwayGraphItem;
		int iPathwayID = 0;

		while (iterPathwayGraphItem.hasNext())
		{
			pathwayGraphItem = iterPathwayGraphItem.next();

			if (pathwayGraphItem == null)
			{
				generalManager.getSingelton().logMsg(
						this.getClass().getSimpleName() + " (" + iUniqueId
								+ "): pathway graph item is null.  ",
						LoggerType.VERBOSE);
				continue;
			}

			iterIdenticalPathwayGraphItemRep = pathwayGraphItem
					.getAllItemsByProp(EGraphItemProperty.ALIAS_CHILD)
					.iterator();

			while (iterIdenticalPathwayGraphItemRep.hasNext())
			{
				iPathwayID = ((PathwayGraph) iterIdenticalPathwayGraphItemRep
						.next().getAllGraphByType(
								EGraphItemHierarchy.GRAPH_PARENT).toArray()[0])
						.getId();

				iAlUninitializedPathwayIDs.add(iPathwayID);

				// // Slerp to layered pathway view
				// SlerpAction slerpAction = new SlerpAction(iPathwayId,
				// poolLayer, false);
				//		
				// arSlerpActions.add(slerpAction);
			}

			iSlerpFactor = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#handleEvents(org.caleydo.core.manager.view.EPickingType,
	 *      org.caleydo.core.manager.view.EPickingMode, int,
	 *      org.caleydo.core.manager.view.Pick)
	 */
	protected void handleEvents(EPickingType pickingType,
			EPickingMode pickingMode, int iExternalID, Pick pick) {

		switch (pickingType)
		{
		case VIEW_SELECTION:
			switch (pickingMode)
			{
			case MOUSE_OVER:
				iMouseOverViewID = iExternalID;
				generalManager.getSingelton().getViewGLCanvasManager()
						.getInfoAreaManager().setDataAboutView(iExternalID);

				break;

			case CLICKED:

				generalManager.getSingelton().getViewGLCanvasManager()
						.getInfoAreaManager().setDataAboutView(iExternalID);

				if (poolLayer.containsElement(iExternalID))// ||
															// stackLayer.containsElement(iViewID))
				{
					loadViewToUnderInteractionLayer(iExternalID);
				}

				if (!dragAndDrop.isDragActionRunning())
					dragAndDrop.startDragAction(iExternalID);

				break;

			case DRAGGED:

				break;
			}

			pickingManager.flushHits(iUniqueId, EPickingType.VIEW_SELECTION);

			break;

		case BUCKET_LOCK_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:

				break;

			case MOUSE_OVER:

				iNavigationMouseOverViewID_lock = iExternalID;
				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;

				break;
			}

			pickingManager.flushHits(iUniqueId,
					EPickingType.BUCKET_LOCK_ICON_SELECTION);

			break;

		case BUCKET_MOVE_IN_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				loadViewToUnderInteractionLayer(iExternalID);
				bEnableNavigationOverlay = false;
				break;

			case MOUSE_OVER:

				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = iExternalID;
				iNavigationMouseOverViewID_lock = -1;

				break;
			}

			pickingManager.flushHits(iUniqueId,
					EPickingType.BUCKET_MOVE_IN_ICON_SELECTION);

			break;

		case BUCKET_MOVE_OUT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:

				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();

				SlerpAction slerpActionTransition = new SlerpAction(
						iExternalID, stackLayer, poolLayer);
				arSlerpActions.add(slerpActionTransition);

				bEnableNavigationOverlay = false;

				break;

			case MOUSE_OVER:

				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = iExternalID;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;

				break;
			}

			pickingManager.flushHits(iUniqueId,
					EPickingType.BUCKET_MOVE_OUT_ICON_SELECTION);

			break;

		case BUCKET_MOVE_LEFT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();

				int iDestinationPosIndex = stackLayer
						.getPositionIndexByElementId(iExternalID);

				if (iDestinationPosIndex == 3)
					iDestinationPosIndex = 0;
				else
					iDestinationPosIndex++;

				if (stackLayer
						.getElementIdByPositionIndex(iDestinationPosIndex) == -1)
				{
					SlerpAction slerpAction = new SlerpAction(iExternalID,
							stackLayer, stackLayer, iDestinationPosIndex);
					arSlerpActions.add(slerpAction);
				} else
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iExternalID, stackLayer, transitionLayer);
					arSlerpActions.add(slerpActionTransition);

					SlerpAction slerpAction = new SlerpAction(stackLayer
							.getElementIdByPositionIndex(iDestinationPosIndex),
							stackLayer, stackLayer, stackLayer
									.getPositionIndexByElementId(iExternalID));
					arSlerpActions.add(slerpAction);

					SlerpAction slerpActionTransitionReverse = new SlerpAction(
							iExternalID, transitionLayer, stackLayer,
							iDestinationPosIndex);
					arSlerpActions.add(slerpActionTransitionReverse);
				}

				bEnableNavigationOverlay = false;

				break;

			case MOUSE_OVER:

				iNavigationMouseOverViewID_left = iExternalID;
				iNavigationMouseOverViewID_right = -1;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;

				break;
			}

			pickingManager.flushHits(iUniqueId,
					EPickingType.BUCKET_MOVE_LEFT_ICON_SELECTION);

			break;

		case BUCKET_MOVE_RIGHT_ICON_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:
				// Check if other slerp action is currently running
				if (iSlerpFactor > 0 && iSlerpFactor < SLERP_RANGE)
					break;

				arSlerpActions.clear();

				int iDestinationPosIndex = stackLayer
						.getPositionIndexByElementId(iExternalID);

				if (iDestinationPosIndex == 0)
					iDestinationPosIndex = 3;
				else
					iDestinationPosIndex--;

				// Check if spot is free
				if (stackLayer
						.getElementIdByPositionIndex(iDestinationPosIndex) == -1)
				{
					SlerpAction slerpAction = new SlerpAction(iExternalID,
							stackLayer, stackLayer, iDestinationPosIndex);
					arSlerpActions.add(slerpAction);
				} else
				{
					SlerpAction slerpActionTransition = new SlerpAction(
							iExternalID, stackLayer, transitionLayer);
					arSlerpActions.add(slerpActionTransition);

					SlerpAction slerpAction = new SlerpAction(stackLayer
							.getElementIdByPositionIndex(iDestinationPosIndex),
							stackLayer, stackLayer, stackLayer
									.getPositionIndexByElementId(iExternalID));
					arSlerpActions.add(slerpAction);

					SlerpAction slerpActionTransitionReverse = new SlerpAction(
							iExternalID, transitionLayer, stackLayer,
							iDestinationPosIndex);
					arSlerpActions.add(slerpActionTransitionReverse);
				}

				bEnableNavigationOverlay = false;

				break;

			case MOUSE_OVER:

				iNavigationMouseOverViewID_left = -1;
				iNavigationMouseOverViewID_right = iExternalID;
				iNavigationMouseOverViewID_out = -1;
				iNavigationMouseOverViewID_in = -1;
				iNavigationMouseOverViewID_lock = -1;

				break;
			}

			pickingManager.flushHits(iUniqueId,
					EPickingType.BUCKET_MOVE_RIGHT_ICON_SELECTION);

			break;

		case MEMO_PAD_SELECTION:
			switch (pickingMode)
			{
			case CLICKED:

				break;

			case DRAGGED:

				int iDraggedObjectId = dragAndDrop.getDraggedObjectedId();

				if (iExternalID == MEMO_PAD_TRASH_CAN_PICKING_ID)
				{
					if (iDraggedObjectId != -1)
					{
						// if (memoLayer.containsElement(iDraggedObjectId))
						// {
						memoLayer.removeElement(iDraggedObjectId);
						// dragAndDrop.stopDragAction();
						// break;
						// }

						underInteractionLayer.removeElement(iDraggedObjectId);
						stackLayer.removeElement(iDraggedObjectId);
						poolLayer.removeElement(iDraggedObjectId);
					}
				} else if (iExternalID == MEMO_PAD_PICKING_ID)
				{
					if (iDraggedObjectId != -1)
					{
						if (!memoLayer.containsElement(iDraggedObjectId))
						{
							memoLayer.addElement(iDraggedObjectId);
							memoLayer.setElementVisibilityById(true,
									iDraggedObjectId);
						}
					}
				}

				dragAndDrop.stopDragAction();

				break;
			}

			pickingManager.flushHits(iUniqueId, EPickingType.MEMO_PAD_SELECTION);

			break;

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.caleydo.core.view.opengl.canvas.AGLCanvasUser#getInfo()
	 */
	public ArrayList<String> getInfo() {

		ArrayList<String> sAlInfo = new ArrayList<String>();
		sAlInfo.add("No info available!");
		return sAlInfo;
	}

	private void createEventMediator() {

		// Create event mediator that connects all views in the bucket
		iBucketEventMediatorID = generalManager
				.createId(ManagerObjectType.EVENT_MEDIATOR_CREATE);

		CmdEventCreateMediator tmpMediatorCmd = (CmdEventCreateMediator) generalManager
				.getSingelton().getCommandManager().createCommandByType(
						CommandQueueSaxType.CREATE_EVENT_MEDIATOR);

		ArrayList<Integer> iAlSenderIDs = new ArrayList<Integer>();
		ArrayList<Integer> iAlReceiverIDs = new ArrayList<Integer>();
		iAlSenderIDs.add(iUniqueId);
		iAlReceiverIDs.add(iUniqueId);
		tmpMediatorCmd.setAttributes(iBucketEventMediatorID, iAlSenderIDs,
				iAlReceiverIDs, MediatorType.SELECTION_MEDIATOR);
		tmpMediatorCmd.doCommand();
	}

	protected abstract void renderMemoPad(final GL gl);
}
