/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
/**
 *
 */
package org.caleydo.view.enroute.path.node.mode;

import gleem.linalg.Vec3f;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.view.enroute.path.APathwayPathRenderer;
import org.caleydo.view.enroute.path.node.ALinearizableNode;
import org.caleydo.view.enroute.path.node.ANodeAttributeRenderer;
import org.caleydo.view.enroute.path.node.ComplexNode;
import org.caleydo.view.enroute.path.node.RemoveNodeButtonAttributeRenderer;

/**
 * The linearized mode for {@link ComplexNode}s.
 *
 * @author Christian
 *
 */
public class ComplexNodeLinearizedMode extends ALinearizeableNodeMode implements IComplexNodeMode {

	/**
	 * @param view
	 */
	public ComplexNodeLinearizedMode(AGLView view, APathwayPathRenderer pathwayPathRenderer) {
		super(view, pathwayPathRenderer);
	}

	@Override
	public void apply(ALinearizableNode node) {
		this.node = node;
		destroy();
		init();
		attributeRenderers.clear();
		RemoveNodeButtonAttributeRenderer attributeRenderer = new RemoveNodeButtonAttributeRenderer(view, node,
				pathwayPathRenderer);

		ComplexNode complexNode = (ComplexNode) node;

		if (complexNode.getNodes() != null) {
			List<Integer> nodeIds = new ArrayList<Integer>();

			for (ALinearizableNode n : complexNode.getNodes()) {
				nodeIds.add(n.hashCode());
			}
			attributeRenderer.setNodeIds(nodeIds);
			attributeRenderer.registerPickingListeners();
		}
		addAttributeRenderer(attributeRenderer);

		// Column baseColumn = new Column("baseColumn");
		// Row baseRow = new Row("baseRow");
		// ColorRenderer colorRenderer = new ColorRenderer(new float[] { 1, 1,
		// 1, 1 });
		// colorRenderer.setView(view);
		// colorRenderer.setBorderColor(new float[] { 0, 0, 0, 1 });
		// colorRenderer
		// .addPickingID(PickingType.LINEARIZABLE_NODE.name(),
		// node.getNodeId());
		// baseColumn.addBackgroundRenderer(colorRenderer);
		//
		// ElementLayout labelLayout = new ElementLayout("label");
		// LabelRenderer labelRenderer = new LabelRenderer(view, this);
		// labelRenderer.setAlignment(LabelRenderer.ALIGN_CENTER);
		//
		// labelLayout.setRenderer(labelRenderer);
		// labelLayout.setPixelSizeY(16);
		//
		// ElementLayout horizontalSpacing = new ElementLayout();
		// horizontalSpacing.setPixelSizeX(2);
		//
		// // baseRow.append(horizontalSpacing);
		// baseRow.append(labelLayout);
		// // baseRow.append(horizontalSpacing);
		//
		// ElementLayout verticalSpacing = new ElementLayout();
		// verticalSpacing.setPixelSizeY(2);
		//
		// baseColumn.append(verticalSpacing);
		// baseColumn.append(baseRow);
		// baseColumn.append(verticalSpacing);
		//
		// layoutManager.setBaseElementLayout(baseColumn);
	}

	@Override
	public int getMinHeightPixels() {
		ComplexNode complexNode = (ComplexNode) node;
		int heightPixels = 0;
		for (ALinearizableNode node : complexNode.getNodes()) {
			heightPixels += node.getHeightPixels();
		}

		return heightPixels;
	}

	@Override
	public int getMinWidthPixels() {

		ComplexNode complexNode = (ComplexNode) node;
		int maxWidthPixels = Integer.MIN_VALUE;
		for (ALinearizableNode node : complexNode.getNodes()) {
			if (maxWidthPixels < node.getWidthPixels()) {
				maxWidthPixels = node.getWidthPixels();
			}
		}

		return maxWidthPixels;
	}

	@Override
	protected void init() {

	}

	@Override
	public void destroy() {
		super.destroy();
	}

	// @Override
	// public String getLabel() {
	// return node.getCaption();
	// }

	@Override
	public void render(GL2 gl, GLU glu) {
		ComplexNode complexNode = (ComplexNode) node;

		for (ALinearizableNode node : complexNode.getNodes()) {
			node.render(gl, glu);
		}
	}

	@Override
	public void renderHighlight(GL2 gl, GLU glu) {
		ComplexNode complexNode = (ComplexNode) node;
		for (ALinearizableNode node : complexNode.getNodes()) {
			node.renderHighlight(gl, glu);
		}
		for (ANodeAttributeRenderer attributeRenderer : attributeRenderers) {
			attributeRenderer.render(gl);
		}
	}

	@Override
	public void updateSubNodePositions() {
		ComplexNode complexNode = (ComplexNode) node;
		Vec3f position = complexNode.getPosition();
		float currentPositionY = position.y() + complexNode.getHeight() / 2.0f;

		for (ALinearizableNode node : complexNode.getNodes()) {
			float nodeHeight = node.getHeight();
			currentPositionY -= nodeHeight / 2.0f;
			node.setPosition(new Vec3f(position.x(), currentPositionY, position.z()));
			currentPositionY -= nodeHeight / 2.0f;
		}
	}

	@Override
	protected boolean determineHighlightColor() {
		return false;
	}


	// @Override
	// public boolean isLabelDefault() {
	// return false;
	// }

}
