/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *  
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *  
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.core.gui.toolbar;

import org.caleydo.core.gui.toolbar.action.AToolBarAction;
import org.caleydo.core.util.link.LinkHandler;
import org.caleydo.data.loader.ResourceLoader;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.PlatformUI;

public class OpenOnlineHelpAction extends AToolBarAction implements IToolBarItem {

	public static final String TEXT = "Help";
	public static final String ICON = "resources/icons/general/help_16.png";

	private String url;

	public OpenOnlineHelpAction(String url, boolean useSmallIcon) {
		this.url = url;
		setText(TEXT);
		setToolTipText(TEXT);
		setImageDescriptor(ImageDescriptor
				.createFromImage(useSmallIcon ? new ResourceLoader().getImage(PlatformUI
						.getWorkbench().getDisplay(), ICON) : JFaceResources
						.getImage(Dialog.DLG_IMG_HELP)));
		setChecked(false);
	}

	@Override
	public void run() {
		super.run();
		setChecked(false);
		LinkHandler.openLink(url);
	};

}