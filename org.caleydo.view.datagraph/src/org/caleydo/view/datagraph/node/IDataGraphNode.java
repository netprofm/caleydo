package org.caleydo.view.datagraph.node;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.media.opengl.GL2;

import org.caleydo.core.data.container.ADimensionGroupData;
import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;

public interface IDataGraphNode extends IDraggable {

	public List<DataContainer> getDataContainers();

	public void render(GL2 gl);

	public int getHeightPixels();

	public int getWidthPixels();

	public float getHeight();

	public float getWidth();

	public Pair<Point2D, Point2D> getBottomDataContainerAnchorPoints(
			DataContainer dataContainer);

	public Pair<Point2D, Point2D> getTopDataContainerAnchorPoints(
			DataContainer dataContainer);

	public Pair<Point2D, Point2D> getTopAnchorPoints();

	public Pair<Point2D, Point2D> getBottomAnchorPoints();

	public Pair<Point2D, Point2D> getLeftAnchorPoints();

	public Pair<Point2D, Point2D> getRightAnchorPoints();

	public Point2D getPosition();

	public int getID();

	public void destroy();

	public void update();

	public Rectangle2D getBoundingBox();

	public void init();

	public void setUpsideDown(boolean isUpsideDown);

	public boolean isUpsideDown();

}
