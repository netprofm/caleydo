package org.caleydo.view.bucket.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.rcp.view.toolbar.IToolBarItem;
import org.caleydo.rcp.view.toolbar.ToolBarContainer;
import org.caleydo.view.bucket.SerializedBucketView;
import org.caleydo.view.pathway.toolbar.PathwaySearchBox;
import org.caleydo.view.pathway.toolbar.PathwayToolBarMediator;
import org.caleydo.view.pathway.toolbar.actions.GeneMappingAction;
import org.caleydo.view.pathway.toolbar.actions.TextureAction;

/**
 * Widget based toolbar container to display pathway related toolbar content.
 * 
 * @author Marc Streit
 */
public class PathwayToolBarContainer extends ToolBarContainer {

	/** Mediator to handle actions triggered by the contributed elements */
	PathwayToolBarMediator pathwayToolBarMediator;

	/** serialized remote rendering view to read the configuration from */
	SerializedBucketView targetViewData;

	/**
	 * Creates a the pathway selection box and add the pathway toolbar items.
	 */
	@Override
	public List<IToolBarItem> getToolBarItems() {

		List<IToolBarItem> elements = new ArrayList<IToolBarItem>();

		TextureAction textureAction = new TextureAction(pathwayToolBarMediator);
		textureAction.setTexturesEnabled(targetViewData.isPathwayTexturesEnabled());
		elements.add(textureAction);

		GeneMappingAction geneMappingAction = new GeneMappingAction(
				pathwayToolBarMediator);
		geneMappingAction.setGeneMappingEnabled(targetViewData.isGeneMappingEnabled());
		elements.add(geneMappingAction);

		// TODO: neighborhood currently broken
		// elements.add(new NeighborhoodAction(pathwayToolBarMediator));

		PathwaySearchBox pathwaySearchBox = new PathwaySearchBox("");
		pathwaySearchBox.setPathwayToolBarMediator(pathwayToolBarMediator);
		elements.add(pathwaySearchBox);

		return elements;
	}

	public PathwayToolBarMediator getPathwayToolBarMediator() {
		return pathwayToolBarMediator;
	}

	public void setPathwayToolBarMediator(PathwayToolBarMediator pathwayToolBarMediator) {
		this.pathwayToolBarMediator = pathwayToolBarMediator;
	}

	public SerializedBucketView getTargetViewData() {
		return targetViewData;
	}

	public void setTargetViewData(SerializedBucketView targetViewData) {
		this.targetViewData = targetViewData;
	}
}
