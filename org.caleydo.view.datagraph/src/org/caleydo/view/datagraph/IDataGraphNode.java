package org.caleydo.view.datagraph;

import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;
import org.caleydo.view.visbricks.brick.data.IDimensionGroupData;

public interface IDataGraphNode extends IDraggable {

	public List<IDimensionGroupData> getDimensionGroups();
	
	public void render(GL2 gl);
	
	public int getHeightPixels();
	
	public int getWidthPixels();
	
}