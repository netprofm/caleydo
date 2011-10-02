package org.caleydo.view.datagraph;

import java.awt.geom.Point2D;

import javax.media.opengl.GL2;

import org.caleydo.core.data.container.ADimensionGroupData;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;

public class DimensionGroupRenderer extends ARenderer implements IDraggable {

	private ADimensionGroupData dimensionGroupData;

	private AGLView view;
	private IDataGraphNode node;

	private float prevDraggingMouseX;
	private float prevDraggingMouseY;
	private Point2D draggingPosition;
	private SelectionType selectionType;
	private boolean renderDimensionGroupLabel;

	public DimensionGroupRenderer(ADimensionGroupData dimensionGroupData,
			AGLView view, DragAndDropController dragAndDropController,
			IDataGraphNode node) {
		this.setDimensionGroupData(dimensionGroupData);
		this.view = view;
		this.node = node;
		renderDimensionGroupLabel = true;
	}

	@Override
	public void render(GL2 gl) {
		CaleydoTextRenderer textRenderer = view.getTextRenderer();

		// FIXME: Use color from data domain

		float[] color = new float[] { 0.5f, 0.5f, 0.5f };
		if (dimensionGroupData.getDataDomain() != null) {
			color = dimensionGroupData.getDataDomain().getColor().getRGB();
		}

		gl.glColor4f(color[0], color[1], color[2], 1f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(0, 0, 0.1f);
		gl.glVertex3f(x, 0, 0.1f);
		gl.glVertex3f(x, y, 0.1f);
		gl.glVertex3f(0, y, 0.1f);
		gl.glEnd();

		if (selectionType != null && selectionType != SelectionType.NORMAL) {
			gl.glColor4fv(selectionType.getColor(), 0);
			gl.glPushAttrib(GL2.GL_LINE_BIT);
			gl.glLineWidth(3);
			gl.glBegin(GL2.GL_LINE_LOOP);
			gl.glVertex3f(0, 0, 0.1f);
			gl.glVertex3f(x, 0, 0.1f);
			gl.glVertex3f(x, y, 0.1f);
			gl.glVertex3f(0, y, 0.1f);
			gl.glEnd();
			gl.glPopAttrib();
		}

		gl.glPushAttrib(GL2.GL_LINE_BIT);
		gl.glLineWidth(2);

		// gl.glColor3f(0.3f, 0.3f, 0.3f);
		gl.glColor3f(color[0] - 0.2f, color[1] - 0.2f, color[2] - 0.2f);
		gl.glBegin(GL2.GL_LINES);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(0, y, 0);

		// gl.glColor3f(0.7f, 0.7f, 0.7f);
		gl.glColor3f(color[0] - 0.2f, color[1] - 0.2f, color[2] - 0.2f);
		gl.glVertex3f(0, y, 0);
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glVertex3f(x, y, 0);

		gl.glEnd();

		gl.glPopAttrib();

		if (renderDimensionGroupLabel) {
			gl.glPushMatrix();
			gl.glTranslatef(0, y, 0.1f);
			gl.glRotatef(-90, 0, 0, 1);

			textRenderer.renderTextInBounds(gl, dimensionGroupData.getLabel(),
					0, 0, 0, y, x);
			gl.glPopMatrix();
		}

	}

	public void setDimensionGroupData(ADimensionGroupData dimensionGroupData) {
		this.dimensionGroupData = dimensionGroupData;
	}

	public ADimensionGroupData getDimensionGroupData() {
		return dimensionGroupData;
	}

	@Override
	public void setDraggingStartPoint(float mouseCoordinateX,
			float mouseCoordinateY) {
		prevDraggingMouseX = mouseCoordinateX;
		prevDraggingMouseY = mouseCoordinateY;
		draggingPosition = node.getBottomDimensionGroupAnchorPoints(
				dimensionGroupData).getFirst();

	}

	@Override
	public void handleDragging(GL2 gl, float mouseCoordinateX,
			float mouseCoordinateY) {
		gl.glColor4f(0.6f, 0.6f, 0.6f, 0.5f);
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f((float) draggingPosition.getX(),
				(float) draggingPosition.getY(), 0);
		gl.glVertex3f((float) draggingPosition.getX() + x,
				(float) draggingPosition.getY(), 0);
		gl.glVertex3f((float) draggingPosition.getX() + x,
				(float) draggingPosition.getY() + y, 0);
		gl.glVertex3f((float) draggingPosition.getX(),
				(float) draggingPosition.getY() + y, 0);
		gl.glEnd();

		if ((prevDraggingMouseX >= mouseCoordinateX - 0.01 && prevDraggingMouseX <= mouseCoordinateX + 0.01)
				&& (prevDraggingMouseY >= mouseCoordinateY - 0.01 && prevDraggingMouseY <= mouseCoordinateY + 0.01))
			return;

		float mouseDeltaX = prevDraggingMouseX - mouseCoordinateX;
		float mouseDeltaY = prevDraggingMouseY - mouseCoordinateY;

		draggingPosition.setLocation(draggingPosition.getX() - mouseDeltaX,
				draggingPosition.getY() - mouseDeltaY);

		prevDraggingMouseX = mouseCoordinateX;
		prevDraggingMouseY = mouseCoordinateY;

		view.setDisplayListDirty();

	}

	@Override
	public void handleDrop(GL2 gl, float mouseCoordinateX,
			float mouseCoordinateY) {
		draggingPosition.setLocation(0, 0);
	}

	public void setSelectionType(SelectionType selectionType) {
		this.selectionType = selectionType;
	}

	public SelectionType getSelectionType() {
		return selectionType;
	}

	public void setRenderDimensionGroupLabel(boolean renderDimensionGroupLabel) {
		this.renderDimensionGroupLabel = renderDimensionGroupLabel;
	}

	public boolean isRenderDimensionGroupLabel() {
		return renderDimensionGroupLabel;
	}

}