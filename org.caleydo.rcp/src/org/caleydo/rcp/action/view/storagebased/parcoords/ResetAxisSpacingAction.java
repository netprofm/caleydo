package org.caleydo.rcp.action.view.storagebased.parcoords;

import org.caleydo.core.command.view.rcp.EExternalActionType;
import org.caleydo.data.loader.ResourceLoader;
import org.caleydo.rcp.action.view.AToolBarAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;

public class ResetAxisSpacingAction
	extends AToolBarAction
{
	public static final String TEXT = "Reset Axis Spacing";
	public static final String ICON = "resources/icons/view/storagebased/parcoords/axis_copy.png";

	/**
	 * Constructor.
	 */
	public ResetAxisSpacingAction(int iViewID)
	{
		super(iViewID);

		setText(TEXT);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor.createFromImage(new ResourceLoader().getImage(
				PlatformUI.getWorkbench().getDisplay(), ICON)));
	}

	@Override
	public void run()
	{
		super.run();

		triggerCmdExternalAction(EExternalActionType.PARCOORDS_RESET_AXIS_SPACING);
	};
}
