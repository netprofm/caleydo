package org.caleydo.rcp.action.view.storagebased;

import org.caleydo.core.command.view.rcp.EExternalActionType;
import org.caleydo.rcp.action.view.AToolBarAction;
import org.eclipse.jface.resource.ImageDescriptor;

public class ClearSelectionsAction
	extends AToolBarAction
{

	public static final String TEXT = "Clear all selections";
	public static final String ICON = "resources/icons/view/storagebased/clear_selections.png";
	
	public ClearSelectionsAction(int iViewID)
	{
		super(iViewID);

		setText(TEXT);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor.createFromURL(this.getClass().getClassLoader()
				.getResource(ICON)));
	}

	@Override
	public void run()
	{
		super.run();

		triggerCmdExternalAction(EExternalActionType.STORAGEBASED_CLEAR_SELECTIONS);
	};
}
