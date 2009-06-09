package org.caleydo.core.view.opengl.util.overlay.contextmenu.container;

import org.caleydo.core.view.opengl.util.overlay.contextmenu.AItemContainer;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.item.InterchangeGroupsItem;
import org.caleydo.core.view.opengl.util.overlay.contextmenu.item.MergeClustersItem;

/**
 * Implementation of AItemContainer for groups/clusters. In this context menu all operations related to group
 * handling are included.
 * 
 * @author Bernhard Schlegl
 */
public class GroupContextMenuItemContainer
	extends AItemContainer {
	/**
	 * Constructor
	 * 
	 * @param groupNr
	 */
	public GroupContextMenuItemContainer() {
		super();

//		if (GeneralManager.get().getUseCase().getUseCaseMode() != EUseCaseMode.GENETIC_DATA)
//			throw new IllegalStateException("This context menu container is only valid for genetic data");

	}

	/**
	 * Sets parameter needed for correct initialization of context menu.
	 * 
	 * @param bGeneGroup
	 *            if true gene groups will be handled, if false experiment groups
	 * @param bEnableMerge
	 *            if true merge cluster item will be added
	 * @param bEnableInterchange
	 *            if true interchange cluster item will be added
	 */
	public void setContextMenuFlags(boolean bGeneGroup, boolean bEnableMerge, boolean bEnableInterchange) {

		if (bEnableMerge) {
			MergeClustersItem mergeClusters = new MergeClustersItem();
			mergeClusters.setGeneExperimentFlag(bGeneGroup);
			addContextMenuItem(mergeClusters);
		}

		if (bEnableInterchange) {
			InterchangeGroupsItem interchangeGroups = new InterchangeGroupsItem();
			interchangeGroups.setGeneExperimentFlag(bGeneGroup);
			addContextMenuItem(interchangeGroups);
		}
	}
}
