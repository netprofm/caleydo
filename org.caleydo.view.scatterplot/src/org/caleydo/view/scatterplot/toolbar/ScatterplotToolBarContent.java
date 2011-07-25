package org.caleydo.view.scatterplot.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.gui.toolbar.ActionToolBarContainer;
import org.caleydo.core.gui.toolbar.IToolBarItem;
import org.caleydo.core.gui.toolbar.ToolBarContainer;
import org.caleydo.core.gui.toolbar.content.AToolBarContent;
import org.caleydo.view.scatterplot.GLScatterPlot;
import org.caleydo.view.scatterplot.actions.Toggle2AxisModeAction;
import org.caleydo.view.scatterplot.actions.ToggleColorModeAction;
import org.caleydo.view.scatterplot.actions.ToggleMainViewZoomAction;
import org.caleydo.view.scatterplot.actions.ToggleMatrixViewAction;
import org.caleydo.view.scatterplot.actions.ToggleMatrixZoomAction;
import org.caleydo.view.scatterplot.actions.TogglePointTypeAction;

/**
 * ToolBarContent implementation for scatterplot specific toolbar items.
 * 
 * @author Marc Streit
 * @author Juergen Pillhofer
 */
public class ScatterplotToolBarContent extends AToolBarContent {

	public static final String IMAGE_PATH = "resources/icons/view/tablebased/parcoords/parcoords.png";

	public static final String VIEW_TITLE = "Scatterplot";

	private IToolBarItem pointSizeSlider;

	// private IToolBarItem xAxisSelector;
	// private IToolBarItem yAxisSelector;

	@Override
	public Class<?> getViewClass() {
		return GLScatterPlot.class;
	}

	@Override
	protected List<ToolBarContainer> getToolBarContent() {
		ActionToolBarContainer container = new ActionToolBarContainer();

		container.setImagePath(IMAGE_PATH);
		container.setTitle(VIEW_TITLE);
		List<IToolBarItem> actionList = new ArrayList<IToolBarItem>();
		container.setToolBarItems(actionList);

		IToolBarItem testAction = new TogglePointTypeAction();
		actionList.add(testAction);

		IToolBarItem toggleMatrix = new ToggleMatrixViewAction();
		actionList.add(toggleMatrix);

		IToolBarItem toggleColor = new ToggleColorModeAction();
		actionList.add(toggleColor);

		IToolBarItem toggleMatrixZoom = new ToggleMatrixZoomAction();
		actionList.add(toggleMatrixZoom);

		IToolBarItem toggleMainViewZoom = new ToggleMainViewZoomAction();
		actionList.add(toggleMainViewZoom);

		IToolBarItem toggle2AxisMode = new Toggle2AxisModeAction();
		actionList.add(toggle2AxisMode);

		if (pointSizeSlider == null) {
			pointSizeSlider = new PointSizeSlider("", 0);
		}
		actionList.add(pointSizeSlider);

		// if (xAxisSelector == null) {
		// xAxisSelector = new XAxisSelector("", 0);
		// }
		// actionList.add(xAxisSelector);
		//
		// if (yAxisSelector == null) {
		// yAxisSelector = new YAxisSelector("", 0);
		// }
		// actionList.add(yAxisSelector);

		ArrayList<ToolBarContainer> list = new ArrayList<ToolBarContainer>();
		list.add(container);

		return list;
	}

}
