/**
 * 
 */
package org.caleydo.view.linearizedpathway.node;

import gleem.linalg.Vec3f;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL2;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.ATimedMouseOutPickingListener;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.caleydo.view.linearizedpathway.GLLinearizedPathway;
import org.caleydo.view.linearizedpathway.PickingType;
import org.caleydo.view.linearizedpathway.event.RemoveLinearizedNodeEvent;

/**
 * Renderer for a button to remove a node from the linearized pathway.
 * 
 * @author Christian
 * 
 */
public class RemoveNodeButtonAttributeRenderer extends ANodeAttributeRenderer {

	protected static final int REMOVE_BUTTON_WIDTH_PIXELS = 16;

	/**
	 * Determines whether the button to remove the associated node from the
	 * linearized pathway shall be shown.
	 */
	protected boolean showRemoveButton = false;

	/**
	 * Picking listener that triggers the node removal.
	 */
	private IPickingListener buttonPickingListener;

	/**
	 * Picking listener that shows and hides the node when hovering over a node.
	 */
	private IPickingListener showButtonPickingListener;

	/**
	 * List of nodeIds that shall be used for picking.
	 */
	private List<Integer> nodeIds = new ArrayList<Integer>();

	/**
	 * @param view
	 * @param node
	 */
	public RemoveNodeButtonAttributeRenderer(GLLinearizedPathway view, ANode node) {
		super(view, node);
	}

	@Override
	public void render(GL2 gl) {
		if (showRemoveButton) {

			// gl.glPushAttrib(GL2.GL_COLOR);
			for (Integer nodeId : nodeIds) {
				gl.glPushName(pickingManager.getPickingID(view.getID(),
						PickingType.REMOVABLE_NODE.name(), nodeId));
				gl.glPushName(pickingManager.getPickingID(view.getID(),
						PickingType.REMOVE_NODE_BUTTON.name(), nodeId));
			}
			// ALinearizableNode currentNode =
			Vec3f position = node.getPosition();
			float nodeHeight = pixelGLConverter.getGLHeightForPixelHeight(node
					.getHeightPixels());
			float nodeWidth = pixelGLConverter.getGLWidthForPixelWidth(node
					.getWidthPixels());
			float buttonWidth = pixelGLConverter
					.getGLWidthForPixelWidth(REMOVE_BUTTON_WIDTH_PIXELS);

			Vec3f lowerLeftCorner = new Vec3f(position.x() + nodeWidth / 2.0f,
					position.y() + nodeHeight / 2.0f, position.z());
			Vec3f lowerRightCorner = new Vec3f(lowerLeftCorner.x() + buttonWidth,
					lowerLeftCorner.y(), position.z());
			Vec3f upperRightCorner = new Vec3f(lowerRightCorner.x(), lowerRightCorner.y()
					+ buttonWidth, position.z());
			Vec3f upperLeftCorner = new Vec3f(lowerLeftCorner.x(), upperRightCorner.y(),
					position.z());

			textureManager.renderTexture(gl, EIconTextures.REMOVE, lowerLeftCorner,
					lowerRightCorner, upperRightCorner, upperLeftCorner, 1, 1, 1, 1);

			for (int i = 0; i < nodeIds.size(); i++) {
				gl.glPopName();
				gl.glPopName();
			}

			// gl.glPopAttrib();

		}

	}

	@Override
	public void registerPickingListeners() {
		
		for (Integer nodeId : nodeIds) {
			showButtonPickingListener = new ATimedMouseOutPickingListener() {

				@Override
				protected void timedMouseOut(Pick pick) {
					showRemoveButton = false;
					view.setDisplayListDirty();
				}

				@Override
				public void mouseOver(Pick pick) {

					List<ALinearizableNode> linearizedNodes = view.getLinearizedNodes();
					int index = linearizedNodes.indexOf(node);

					if ((index == 0) || (index == linearizedNodes.size() - 1)) {
						super.mouseOver(pick);
						showRemoveButton = true;
						view.setDisplayListDirty();
					}
				}

				@Override
				public void mouseOut(Pick pick) {
					if (showRemoveButton) {
						super.mouseOut(pick);
					}
				}
			};

			view.addIDPickingListener(showButtonPickingListener,
					PickingType.LINEARIZABLE_NODE.name(), nodeId);
			view.addIDPickingListener(showButtonPickingListener,
					PickingType.REMOVABLE_NODE.name(), nodeId);

			buttonPickingListener = new APickingListener() {

				@Override
				public void clicked(Pick pick) {
					RemoveLinearizedNodeEvent event = new RemoveLinearizedNodeEvent((ALinearizableNode)node);
					event.setSender(this);
					GeneralManager.get().getEventPublisher().triggerEvent(event);
//					view.removeLinearizedNode((ALinearizableNode)node);
				}

			};

			view.addIDPickingListener(buttonPickingListener,
					PickingType.REMOVE_NODE_BUTTON.name(), nodeId);
		}
	}

	@Override
	public void unregisterPickingListeners() {
		for (Integer nodeId : nodeIds) {
			view.removeIDPickingListener(showButtonPickingListener,
					PickingType.LINEARIZABLE_NODE.name(), nodeId);
			view.removeIDPickingListener(showButtonPickingListener,
					PickingType.REMOVABLE_NODE.name(), nodeId);
			view.removeIDPickingListener(buttonPickingListener,
					PickingType.REMOVE_NODE_BUTTON.name(), nodeId);
		}
	}

	/**
	 * @param nodeIds
	 *            setter, see {@link #nodeIds}
	 */
	public void setNodeIds(List<Integer> nodeIds) {
		this.nodeIds = nodeIds;
	}

	/**
	 * Adds a node id to {@link #nodeIds}.
	 * 
	 * @param nodeId
	 */
	public void addNodeId(int nodeId) {
		nodeIds.add(nodeId);
	}
}
