package org.caleydo.core.io.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * File dialog for opening raw text data files.
 * 
 * @author Marc Streit
 */
public class LoadDataDialog
	extends Dialog {
	private FileLoadDataAction fileLoadDataAction;

	/**
	 * Constructor.
	 */
	public LoadDataDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Open Text Data File");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		fileLoadDataAction = new FileLoadDataAction(parent);
		fileLoadDataAction.run();

		return parent;
	}

	@Override
	protected void okPressed() {

		if (fileLoadDataAction.execute())
			super.okPressed();
	}
}