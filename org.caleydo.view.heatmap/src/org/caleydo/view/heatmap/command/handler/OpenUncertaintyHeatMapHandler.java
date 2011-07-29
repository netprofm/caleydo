package org.caleydo.view.heatmap.command.handler;

import org.caleydo.view.heatmap.uncertainty.GLUncertaintyHeatMap;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenUncertaintyHeatMapHandler extends AbstractHandler implements
		IHandler {

	/**
	 * Counter variable for determination of the secondary view ID.
	 * Needed for multiple instances of the same view type.
	 */
	private static int SECONDARY_ID = 0;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {

			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage()
					.showView(GLUncertaintyHeatMap.VIEW_TYPE, Integer.toString(SECONDARY_ID), IWorkbenchPage.VIEW_ACTIVATE);
			
			SECONDARY_ID++;
			
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return null;
	}

}