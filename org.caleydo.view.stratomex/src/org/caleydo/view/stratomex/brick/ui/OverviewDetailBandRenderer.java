/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/

package org.caleydo.view.stratomex.brick.ui;


import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.util.spline.ConnectionBandRenderer;
import org.caleydo.view.stratomex.brick.GLBrick;
import org.caleydo.view.stratomex.column.BrickColumn;

/**
 * Renderer for the band between a normal brick (overview) and the detailed brick.
 *
 * @author Partl
 *
 */
public class OverviewDetailBandRenderer extends ALayoutRenderer {

	private GLBrick leftBrick;
	private GLBrick rightBrick;
	private ConnectionBandRenderer bandRenderer;
	private boolean isOverviewLeft;

	public OverviewDetailBandRenderer(GLBrick leftBrick, GLBrick rightBrick, boolean isOverviewLeft) {
		this.isOverviewLeft = isOverviewLeft;
		this.leftBrick = leftBrick;
		this.rightBrick = rightBrick;
		bandRenderer = new ConnectionBandRenderer();
	}

	@Override
	public void renderContent(GL2 gl) {

		ElementLayout leftLayout = leftBrick.getLayout();
		ElementLayout rightLayout = rightBrick.getLayout();
		BrickColumn dimensionGroup = leftBrick.getBrickColumn();
		Column groupColumn = dimensionGroup.getGroupColumn();

		float leftX = leftLayout.getTranslateX() + leftLayout.getSizeScaledX();
		float leftTopY = leftLayout.getTranslateY() + leftLayout.getSizeScaledY();
		float leftBottomY = leftLayout.getTranslateY();

		float[] leftTopPos = new float[] { leftX, leftTopY };
		float[] leftBottomPos = new float[] { leftX, leftBottomY };

		float rightX = rightLayout.getTranslateX();
		float rightTopY = rightLayout.getTranslateY() + rightLayout.getSizeScaledY();
		float rightBottomY = rightLayout.getTranslateY();

		float[] rightTopPos = new float[] { rightX, rightTopY };
		float[] rightBottomPos = new float[] { rightX, rightBottomY };

		float offsetX = x * 0.4f;

		float middleAnchorX = 0;
		float[] middleAnchorTopPos = null;
		float[] middleAnchorBottomPos = null;

		gl.glTranslatef(-elementLayout.getTranslateX(), -elementLayout.getTranslateY(), 0);

		float[] bandColor = new float[] { 0, 0, 0, 0.15f };// Color.GREEN.getRGBA();

		bandRenderer.init(gl);

		if (isOverviewLeft) {
			middleAnchorX = groupColumn.getTranslateX() + groupColumn.getSizeScaledX();
			middleAnchorTopPos = new float[] { middleAnchorX, leftTopY };
			middleAnchorBottomPos = new float[] { middleAnchorX, leftBottomY };

			bandRenderer.renderSingleBand(gl, middleAnchorTopPos, middleAnchorBottomPos, rightTopPos, rightBottomPos,
					false, offsetX, 0, bandColor);

			bandRenderer.renderStraightBand(gl, leftTopPos, leftBottomPos, middleAnchorTopPos, middleAnchorBottomPos,
					false, 0, bandColor, 1f);
		} else {
			middleAnchorX = groupColumn.getTranslateX();
			if (middleAnchorX > rightX - offsetX) {
				middleAnchorX = rightX - offsetX;
			}

			middleAnchorTopPos = new float[] { middleAnchorX, rightTopY };
			middleAnchorBottomPos = new float[] { middleAnchorX, rightBottomY };

			bandRenderer.renderSingleBand(gl, leftTopPos, leftBottomPos, middleAnchorTopPos, middleAnchorBottomPos,
					false, offsetX, 0, bandColor);

			bandRenderer.renderStraightBand(gl, middleAnchorTopPos, middleAnchorBottomPos, rightTopPos, rightBottomPos,
					false, 0, bandColor, bandColor[3]);
		}

		// gl.glBegin(GL2.GL_QUADS);
		//
		// gl.glVertex3f(leftX, leftBottomY, 2);
		// gl.glVertex3f(rightX, rightBottomY, 2);
		// gl.glVertex3f(rightX, rightTopY, 2);
		// gl.glVertex3f(leftX, leftTopY, 2);
		//
		// gl.glEnd();
		gl.glTranslatef(elementLayout.getTranslateX(), elementLayout.getTranslateY(), 0);

	}

	@Override
	protected boolean permitsWrappingDisplayLists() {
		return false;
	}
}
