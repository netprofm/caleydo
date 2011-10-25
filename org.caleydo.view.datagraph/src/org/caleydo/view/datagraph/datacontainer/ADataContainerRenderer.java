package org.caleydo.view.datagraph.datacontainer;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.view.datagraph.node.IDataGraphNode;

public abstract class ADataContainerRenderer extends LayoutRenderer {

	public final static String DIMENSION_GROUP_PICKING_TYPE = "org.caleydo.view.datagraph.dimensiongroup";

	protected IDataGraphNode node;
	protected AGLView view;
	protected DragAndDropController dragAndDropController;
	protected Map<Integer, Pair<Point2D, Point2D>> bottomDimensionGroupPositions;
	protected Map<Integer, Pair<Point2D, Point2D>> topDimensionGroupPositions;
	protected boolean isUpsideDown = false;

	public ADataContainerRenderer(IDataGraphNode node, AGLView view,
			DragAndDropController dragAndDropController) {
		this.node = node;
		this.view = view;
		this.dragAndDropController = dragAndDropController;
		bottomDimensionGroupPositions = new HashMap<Integer, Pair<Point2D, Point2D>>();
		topDimensionGroupPositions = new HashMap<Integer, Pair<Point2D, Point2D>>();
	}

	public abstract void setDataContainers(
			List<DataContainer> dataContainers);

	public Pair<Point2D, Point2D> getBottomAnchorPointsOfDataContainer(
			DataContainer dataContainer) {
		return bottomDimensionGroupPositions.get(dataContainer.getID());
	}

	public Pair<Point2D, Point2D> getTopAnchorPointsOfDataContainer(
			DataContainer dataContainer) {
		return topDimensionGroupPositions.get(dataContainer.getID());
	}

	public abstract void destroy();

	public abstract void setUpsideDown(boolean isUpsideDown);

	public boolean isUpsideDown() {
		return isUpsideDown;
	}

}
