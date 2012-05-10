/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *  
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.core.view.opengl.util.draganddrop;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import javax.media.opengl.GL2;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.canvas.PixelGLConverter;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;

public class DragAndDropController {

	private Set<IDraggable> draggables;
	private IDropArea dropArea;
	private boolean isDragging;
	private boolean isDraggingFirstTime;
	private Point startDraggingWindowCoords;
	private AGLView view;
	private String draggingMode;

	public DragAndDropController(AGLView view) {
		draggables = new HashSet<IDraggable>();
		isDragging = false;
		isDraggingFirstTime = false;
		this.view = view;
	}

	public void addDraggable(IDraggable draggable) {
		if (draggable != null)
			draggables.add(draggable);
	}

	public void removeDraggable(IDraggable draggable) {
		draggables.remove(draggable);
	}

//	public void startDragging(String draggingMode) {
//		this.draggingMode = draggingMode;
//		startDragging();
//	}

	public void startDragging() {
		isDragging = true;
		isDraggingFirstTime = true;
		dropArea = null;
	}

	public void setDropArea(IDropArea dropArea) {
		if(dropArea == this.dropArea)
			return;
		if(this.dropArea != null)
			this.dropArea.handleDropAreaReplaced();
		this.dropArea = dropArea;
		
	}

	public void clearDraggables() {
		draggables.clear();
	}

	public void handleDragging(GL2 gl, GLMouseListener glMouseListener) {

		if (isDragging) {
			Point mouseWinCoords = glMouseListener.getPickedPoint();

			float[] fArTargetWorldCoordinates = new float[] { 0, 0 };

			PixelGLConverter pixelGLConverter = view.getPixelGLConverter();
			// GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(gl, mouseWinCoords.x,
			// mouseWinCoords.y);
			fArTargetWorldCoordinates[0] = pixelGLConverter.getGLWidthForPixelWidth(mouseWinCoords.x);
			fArTargetWorldCoordinates[1] =
				pixelGLConverter.getGLHeightForPixelHeight(view.getParentGLCanvas().getHeight()
					- mouseWinCoords.y);

			if (dropArea != null) {
				dropArea.handleDragOver(gl, draggables, fArTargetWorldCoordinates[0],
					fArTargetWorldCoordinates[1]);
			}

			for (IDraggable draggable : draggables) {
				if (isDraggingFirstTime) {

					float[] fArStartDraggingWorldCoordinates = new float[] { 0, 0 };
					// GLCoordinateUtils.convertWindowCoordinatesToWorldCoordinates(gl,
					// startDraggingWindowCoords.x, startDraggingWindowCoords.y);
					fArStartDraggingWorldCoordinates[0] =
						pixelGLConverter.getGLWidthForPixelWidth(startDraggingWindowCoords.x);
					fArStartDraggingWorldCoordinates[1] =
						pixelGLConverter.getGLHeightForPixelHeight(view.getParentGLCanvas().getHeight()
							- startDraggingWindowCoords.y);
					draggable.setDraggingStartPoint(fArStartDraggingWorldCoordinates[0],
						fArStartDraggingWorldCoordinates[1]);
				}
				draggable.handleDragging(gl, fArTargetWorldCoordinates[0], fArTargetWorldCoordinates[1]);
			}

			if (glMouseListener.wasMouseReleased()) {
				isDragging = false;
				for (IDraggable draggable : draggables) {
					draggable.handleDrop(gl, fArTargetWorldCoordinates[0], fArTargetWorldCoordinates[1]);
				}
				if (dropArea != null) {
					dropArea.handleDrop(gl, draggables, fArTargetWorldCoordinates[0],
						fArTargetWorldCoordinates[1], this);

				}
				clearDraggables();
				draggingMode = null;
				view.setDisplayListDirty();
			}

			isDraggingFirstTime = false;
		}
	}

	public boolean isDragging() {
		return isDragging;
	}

	public Set<IDraggable> getDraggables() {
		return draggables;
	}

	public boolean hasDraggables() {
		return !draggables.isEmpty();
	}

	public boolean containsDraggable(IDraggable draggable) {
		return draggables.contains(draggable);
	}

	public void setDraggingStartPosition(Point startPosition) {
		startDraggingWindowCoords = startPosition;
	}

	public String getDraggingMode() {
		return draggingMode;
	}

	public void setDraggingMode(String draggingMode)
	{
		this.draggingMode = draggingMode;
	}
}
