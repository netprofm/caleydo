package org.caleydo.core.view.opengl.util.overlay.contextmenu;

import gleem.linalg.Vec3f;

import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import javax.media.opengl.GL;

import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.view.opengl.canvas.AGLEventListener;
import org.caleydo.core.view.opengl.canvas.remote.GLRemoteRendering;
import org.caleydo.core.view.opengl.renderstyle.GeneralRenderStyle;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.GLHelperFunctions;
import org.caleydo.core.view.opengl.util.overlay.AOverlayManager;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.core.view.opengl.util.texture.GLIconTextureManager;

import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;

/**
 * Renders a context menu of dynamically specified items. ContextMenu is based on {@link AOverlayManager}
 * where all the location relevant information is set. Since only one context menu can be active at a time it
 * is implemented as a singleton. It needs to be provided with a location and 1-n instances derived from
 * {@link AContextMenuItem}. On a left click it triggers the event specified in AContextMenuItem
 * 
 * @author Alexander Lex
 */
public class ContextMenu
	extends AOverlayManager {

	private class ContextMenuMetaData {
		private float xOrigin = -1;
		private float yOrigin = -1;
		private float width;
		private float height;
		private float maxTextWidth;
	}

	// Coordinates stuff
	private static final float ITEM_HEIGHT = 0.1f;
	private static final float ICON_SIZE = 0.08f;
	private static final float SPACER_SIZE = 0.02f;
	private static final float SPACING = 0.02f;
	private static final float FONT_SCALING = GeneralRenderStyle.SMALL_FONT_SCALING_FACTOR;

	private static final float BASIC_Z = 0f;
	private static final float BUTTON_Z = BASIC_Z + 0.001f;
	private static final float TEXT_Z = BUTTON_Z + 0.001f;

	/** Overhead for width for the context menu which should be added to the maximum text width */
	private static final float WIDHT_OVERHEAD = 3 * SPACING + 2 * ICON_SIZE;
	/** Overhead for height for the context menu which should be added to {@literal NrElements * ITEM_HEIGHT} */
	private static final float HEIGHT_OVERHEAD = SPACING;

	/** The list of items that should be displayed and triggered */
	private ArrayList<AContextMenuItem> contextMenuItems;

	private TextRenderer textRenderer;

	private GLIconTextureManager iconManager;

	private PickingManager pickingManager;

	private AGLEventListener masterGLView;

	private int mouseOverElement = -1;

	private int displayListIndex = -1;

	private boolean isDisplayListDirty = true;

	/** The singleton instance */
	private static ContextMenu instance;

	private int iPickingIDCounter = 0;

	private HashMap<AContextMenuItem, Integer> hashContextMenuItemToUniqueID;
	private HashMap<Integer, AContextMenuItem> hashUniqueIDToContextMenuItem;
	private HashMap<AContextMenuItem, ContextMenuMetaData> hashContextMenuItemToMetaData;

	private ContextMenuMetaData baseMenuMetaData;

	/**
	 * Private constructor since this is a singleton
	 */
	private ContextMenu() {
		super();
		contextMenuItems = new ArrayList<AContextMenuItem>();
		textRenderer = new TextRenderer(new Font("Arial", Font.PLAIN, 18), true, true);
		textRenderer.setSmoothing(true);
		iconManager = new GLIconTextureManager();
		pickingManager = GeneralManager.get().getViewGLCanvasManager().getPickingManager();

		hashContextMenuItemToUniqueID = new HashMap<AContextMenuItem, Integer>();
		hashContextMenuItemToMetaData = new HashMap<AContextMenuItem, ContextMenuMetaData>();
		hashUniqueIDToContextMenuItem = new HashMap<Integer, AContextMenuItem>();
	}

	/**
	 * Static access method to the instance of the singleton. This is not thread-safe.
	 * 
	 * @return The sole instance of the ContextMenu
	 */
	public static ContextMenu get() {
		if (instance == null)
			instance = new ContextMenu();

		return instance;
	}

	/**
	 * Set the GL view which is currently rendering the context menu. Only in this view will the context menu
	 * be rendered. For remote rendering the remote rendering view is required, while the rest of the data
	 * probably needs to be set in the embedded view.
	 * 
	 * @param masterViewID
	 *            the id of the view where the menu should be rendered
	 */
	public void setMasterGLView(AGLEventListener masterGLView) {
		this.masterGLView = masterGLView;
	}

	/**
	 * Adds an instance of AContextMenuItem to the context menu. The order items are supplied will be the same
	 * in which they appear.
	 * 
	 * @param item
	 *            an instance of AContextMenuItem
	 */
	public void addContextMenueItem(AContextMenuItem item) {

		contextMenuItems.add(item);

		// float textWidth = (float) textRenderer.getBounds(item.getText()).getWidth() * FONT_SCALING;
		// if (textWidth > longestTextWidth)
		// longestTextWidth = textWidth;
	}

	/**
	 * Add a item container to the context menu. The items in the context menu are added automatically
	 * 
	 * @param itemContainer
	 *            the container which holds a list of items
	 */
	public void addItemContanier(AItemContainer itemContainer) {

		flush();

		for (AContextMenuItem item : itemContainer) {
			addContextMenueItem(item);
		}
	}

	/**
	 * Renders the context menu if it is set up for the respective caller. It is safe to call this method in
	 * the display method regardless of whether a context menu should be rendered at the time. It first checks
	 * whether the calling view is the master view at the moment and then whether it is enabled. If both
	 * conditions are true the context menu is displayed. A display list is used which is automatically set
	 * dirty when an element is picked.
	 * 
	 * @param gl
	 * @param masterGLView
	 */
	public void render(GL gl, AGLEventListener masterGLView) {
		if (this.masterGLView != masterGLView)
			return;
		if (!isEnabled)
			return;
		if (contextMenuItems.size() == 0)
			return;
		if (isFirstTime) {
			isFirstTime = false;

			if (contextMenuItems.size() == 0)
				return;

			if (displayListIndex == -1) {
				displayListIndex = gl.glGenLists(1);
			}

			float[] fArWorldCoords =
				GLCoordinateUtils
					.convertWindowCoordinatesToWorldCoordinates(gl, pickedPoint.x, pickedPoint.y);

			float[] fArLeftLimitWorldCoords =
				GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(gl, 0, 0);

			fLeftBorder = fArLeftLimitWorldCoords[0];
			// notice that top and bottom are inverse in opengl vs window coordinates
			fTopBorder = fArLeftLimitWorldCoords[1];
			// GLHelperFunctions.drawPointAt(gl, new
			// Vec3f(fArLeftLimitWorldCoords[0],fArLeftLimitWorldCoords[1], fArLeftLimitWorldCoords[2]));
			//			
			//			
			float[] fArRightLimitWorldCoords =
				GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(gl, windowWidth, windowHeight);

			fRightBorder = fArRightLimitWorldCoords[0];
			fBottomBorder = fArRightLimitWorldCoords[1];

			// GLHelperFunctions.drawPointAt(gl, new
			// Vec3f(fArRightLimitWorldCoords[0],fArRightLimitWorldCoords[1], fArRightLimitWorldCoords[2]));

			baseMenuMetaData = new ContextMenuMetaData();

			baseMenuMetaData.xOrigin = fArWorldCoords[0];
			baseMenuMetaData.yOrigin = fArWorldCoords[1];

			// This is necessary because of the problems
			// with the frustum and picking in the Bucket view.
			// FIXME: Find clean solution!!
			if (masterGLView instanceof GLRemoteRendering) {
				baseMenuMetaData.xOrigin *= 2f;
				baseMenuMetaData.yOrigin *= 2f;
				fRightBorder *=2f;
				fLeftBorder *= 2f;
				fTopBorder *= 2f;
				fBottomBorder *=2f;
			}

			initializeSubMenus(contextMenuItems, baseMenuMetaData);
			
			if ((fRightBorder - baseMenuMetaData.xOrigin) < baseMenuMetaData.width)
				baseMenuMetaData.xOrigin -= baseMenuMetaData.width;
			
			if ((fBottomBorder + baseMenuMetaData.yOrigin) < baseMenuMetaData.height)
				baseMenuMetaData.yOrigin += baseMenuMetaData.height;

		}

		GLHelperFunctions.drawPointAt(gl, new Vec3f(fRightBorder, fBottomBorder, 0));
		GLHelperFunctions.drawPointAt(gl, new Vec3f(fLeftBorder, fTopBorder, 0));
		
		if (isDisplayListDirty) {
			gl.glNewList(displayListIndex, GL.GL_COMPILE);
			gl.glDisable(GL.GL_DEPTH_TEST);
			drawMenu(gl, contextMenuItems, baseMenuMetaData);
			gl.glEnable(GL.GL_DEPTH_TEST);
			gl.glEndList();
			isDisplayListDirty = false;
		}

		gl.glCallList(displayListIndex);
	}

	/**
	 * <p>
	 * Initializes a sub menu and recursively initializes the sub menus of the items in contextMenuItems. Sets
	 * unique IDs for every element and creates the ContextMenuMetaData objects for sub menus.
	 * </p>
	 * <p>
	 * Sets the width and height of a sub menu. The origin in X and Y have to be set at another place before
	 * calling this method.
	 * </p>
	 * 
	 * @param contextMenuItems
	 *            the list of context menu items for the current sub menu
	 * @param metaData
	 *            The metaData information for the current sub menu. height and widht are set, origin hast to
	 *            be set before.
	 * @throws IllegalStateException
	 *             if xOrigin and yOrigin in metaData have not been initialized.
	 */
	private void initializeSubMenus(ArrayList<AContextMenuItem> contextMenuItems, ContextMenuMetaData metaData) {

//		if (metaData.xOrigin < 0 || metaData.yOrigin < 0) {
//			throw new IllegalStateException(
//				"xOrigin and yOrigin of metaData have to be initialized before calling this method.");
//		}
		metaData.maxTextWidth = 0;

		for (AContextMenuItem item : contextMenuItems) {
			hashUniqueIDToContextMenuItem.put(iPickingIDCounter, item);
			hashContextMenuItemToUniqueID.put(item, iPickingIDCounter++);

			float textWidth = (float) textRenderer.getBounds(item.getText()).getWidth() * FONT_SCALING;
			if (textWidth > metaData.maxTextWidth)
				metaData.maxTextWidth = textWidth;

			if (item.hasSubItems()) {
				ContextMenuMetaData newMetaData = new ContextMenuMetaData();
				hashContextMenuItemToMetaData.put(item, newMetaData);
				initializeSubMenus(item.getSubItems(), newMetaData);
			}
		}
		metaData.width = metaData.maxTextWidth + WIDHT_OVERHEAD;
		metaData.height = contextMenuItems.size() * ITEM_HEIGHT + HEIGHT_OVERHEAD;

	
	}

	/**
	 * Drawing the actual menu when rebuilding the display list
	 * 
	 * @param gl
	 */
	private void drawMenu(GL gl, ArrayList<AContextMenuItem> contextMenuItems, ContextMenuMetaData metaData) {

		// This is necessary because of the problems
		// with the frustum and picking in the Bucket view.
		// FIXME: Find clean solution!!
		if (!(masterGLView instanceof GLRemoteRendering))
			gl.glTranslatef(0, 0, 2);

		gl.glColor4f(0.6f, 0.6f, 0.6f, 1f);
		gl.glBegin(GL.GL_POLYGON);
		gl.glVertex3f(metaData.xOrigin, metaData.yOrigin, BASIC_Z);
		gl.glVertex3f(metaData.xOrigin, metaData.yOrigin - metaData.height, BASIC_Z);
		gl.glVertex3f(metaData.xOrigin + metaData.width, metaData.yOrigin - metaData.height, BASIC_Z);
		gl.glVertex3f(metaData.xOrigin + metaData.width, metaData.yOrigin, BASIC_Z);
		gl.glEnd();

		float yPosition = metaData.yOrigin;

		for (AContextMenuItem item : contextMenuItems) {

			Integer itemID = hashContextMenuItemToUniqueID.get(item);
			float xPosition = metaData.xOrigin + SPACING;
			yPosition -= ITEM_HEIGHT;

			float alpha = 0f;
			if (itemID == mouseOverElement || isSubElementSelected(item))
				alpha = 0.8f;

			gl.glColor4f(1, 1, 1, alpha);

			int iPickingID =
				pickingManager
					.getPickingID(masterGLView.getID(), EPickingType.CONTEXT_MENU_SELECTION, itemID);
			gl.glPushName(iPickingID);
			gl.glBegin(GL.GL_POLYGON);
			gl.glVertex3f(xPosition, yPosition - SPACING / 2, BUTTON_Z);
			gl.glVertex3f(xPosition, yPosition + ITEM_HEIGHT - SPACING / 2, BUTTON_Z);

			gl.glColor4f(1, 1, 1, 0.0f);
			gl.glVertex3f(xPosition + metaData.width - 2 * SPACING, yPosition + ITEM_HEIGHT - SPACING / 2,
				BUTTON_Z);
			gl.glVertex3f(xPosition + metaData.width - 2 * SPACING, yPosition - SPACING / 2, BUTTON_Z);

			gl.glEnd();
			gl.glPopName();

			EIconTextures iconTexture = item.getIconTexture();

			// it is legal to specify no icons
			if (iconTexture != null) {
				Texture tempTexture = iconManager.getIconTexture(gl, iconTexture);
				tempTexture.enable();
				tempTexture.bind();
				TextureCoords texCoords = tempTexture.getImageTexCoords();

				gl.glColor4f(1, 1, 1, 1);
				gl.glPushName(iPickingID);
				gl.glBegin(GL.GL_POLYGON);
				gl.glTexCoord2f(texCoords.left(), texCoords.top());
				gl.glVertex3f(xPosition, yPosition, TEXT_Z);
				gl.glTexCoord2f(texCoords.right(), texCoords.top());
				gl.glVertex3f(xPosition + ICON_SIZE, yPosition, TEXT_Z);
				gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
				gl.glVertex3f(xPosition + ICON_SIZE, yPosition + ICON_SIZE, TEXT_Z);
				gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
				gl.glVertex3f(xPosition, yPosition + ICON_SIZE, TEXT_Z);
				gl.glEnd();
				tempTexture.disable();
				gl.glPopName();
			}
			xPosition += ICON_SIZE + SPACING;

			textRenderer.begin3DRendering();
			textRenderer.setColor(0, 0, 0, 1);
			gl.glDisable(GL.GL_DEPTH_TEST);

			textRenderer.draw3D(item.getText(), xPosition, yPosition, TEXT_Z, FONT_SCALING);
			// textRenderer.flush();
			textRenderer.end3DRendering();

			xPosition += metaData.maxTextWidth;
			if (item.hasSubItems()) {

				Texture tempTexture = iconManager.getIconTexture(gl, EIconTextures.MENU_MORE);
				tempTexture.enable();
				tempTexture.bind();
				TextureCoords texCoords = tempTexture.getImageTexCoords();

				gl.glColor4f(1, 1, 1, 1);
				gl.glPushName(iPickingID);
				gl.glBegin(GL.GL_POLYGON);
				gl.glTexCoord2f(texCoords.left(), texCoords.top());
				gl.glVertex3f(xPosition, yPosition, TEXT_Z);
				gl.glTexCoord2f(texCoords.right(), texCoords.top());
				gl.glVertex3f(xPosition + ICON_SIZE, yPosition, TEXT_Z);
				gl.glTexCoord2f(texCoords.right(), texCoords.bottom());
				gl.glVertex3f(xPosition + ICON_SIZE, yPosition + ICON_SIZE, TEXT_Z);
				gl.glTexCoord2f(texCoords.left(), texCoords.bottom());
				gl.glVertex3f(xPosition, yPosition + ICON_SIZE, TEXT_Z);
				gl.glEnd();
				tempTexture.disable();
				gl.glPopName();

				if (itemID == mouseOverElement || isSubElementSelected(item)) {
					ContextMenuMetaData subMetaData = hashContextMenuItemToMetaData.get(item);
					subMetaData.xOrigin = metaData.xOrigin + metaData.width;
					subMetaData.yOrigin = yPosition + ITEM_HEIGHT;
					if ((fRightBorder - subMetaData.xOrigin) < subMetaData.width)
						subMetaData.xOrigin -= subMetaData.width + metaData.width;
					
					if ((subMetaData.yOrigin - subMetaData.height) < fBottomBorder)
						subMetaData.yOrigin = fBottomBorder + subMetaData.height;
					
					
					drawMenu(gl, item.getSubItems(), subMetaData);
					
					
				}
			}
		}
		// gl.glEnable(GL.GL_DEPTH_TEST);

		// This is necessary because of the problems
		// with the frustum and picking in the Bucket view.
		// FIXME: Find clean solution!!
		if (!(masterGLView instanceof GLRemoteRendering))
			gl.glTranslatef(0, 0, -2);
	}

	private boolean isSubElementSelected(AContextMenuItem item) {
		if (!item.hasSubItems())
			return false;
		for (AContextMenuItem tempItem : item.getSubItems()) {
			if (hashContextMenuItemToUniqueID.get(tempItem) == mouseOverElement)
				return true;

			if (isSubElementSelected(tempItem))
				return true;
		}
		return false;
	}

	/**
	 * The handling of the picking. This has to be called when an element of the type
	 * {@link EPickingType#CONTEXT_MENU_SELECTION} is picked.
	 * 
	 * @param ePickingMode
	 *            the mode of the picking, eg. mouse-over or clicked. Only mouse-over and clicked are handled.
	 * @param iExternalID
	 *            the id which has to match one of the ids specified in {@link #display}
	 */
	public void handleEvents(EPickingMode ePickingMode, int iExternalID) {
		switch (ePickingMode) {
			case MOUSE_OVER:
				mouseOverElement = iExternalID;
				isDisplayListDirty = true;
				break;
			case CLICKED:
				hashUniqueIDToContextMenuItem.get(iExternalID).triggerEvent();
				isDisplayListDirty = true;
				flush();
				break;
		}
	}

	/**
	 * Overrides the flush method of {@link AOverlayManager}. Disables the context menu and clears the list of
	 * items supplied as well as the masterViewID.
	 */
	@Override
	public void flush() {
		super.flush();
		contextMenuItems.clear();
		mouseOverElement = -1;
		isDisplayListDirty = true;
		masterGLView = null;
	}
}
