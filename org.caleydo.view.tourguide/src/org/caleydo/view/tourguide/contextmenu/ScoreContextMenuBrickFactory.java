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
package org.caleydo.view.tourguide.contextmenu;

import java.util.Arrays;
import java.util.Collections;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.gui.util.DisplayUtils;
import org.caleydo.core.util.execution.SafeCallable;
import org.caleydo.core.view.contextmenu.AContextMenuItem;
import org.caleydo.view.stratomex.brick.IContextMenuBrickFactory;
import org.caleydo.view.stratomex.column.BrickColumn;
import org.caleydo.view.tourguide.vendingmachine.VendingMachine;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Samuel Gratzl
 *
 */
public class ScoreContextMenuBrickFactory implements IContextMenuBrickFactory {

	@Override
	public Iterable<AContextMenuItem> createGroupEntries(BrickColumn referenceColumn, TablePerspective groupTable) {
		if (!isTourGuideVisible()) // show context menu only if the tour guide view is visible
			return Collections.emptyList();
		return Arrays.asList(new ScoreGroupItem(referenceColumn, groupTable), new MutualExclusiveScoreGroupItem(
				referenceColumn, groupTable));
	}

	private static boolean isTourGuideVisible() {
		final IWorkbench wb = PlatformUI.getWorkbench();
		return DisplayUtils.syncExec(wb.getDisplay(), new SafeCallable<Boolean>() {
			@Override
			public Boolean call() {
				IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
				IWorkbenchPage page = win.getActivePage();
				return page.findViewReference(VendingMachine.VIEW_TYPE) != null;
			}
		}).booleanValue();
	}

	@Override
	public Iterable<AContextMenuItem> createStratification(BrickColumn referenceColumn) {
		if (!isTourGuideVisible())
			return Collections.emptyList();
		return Arrays.asList(new ScoreColumnItem(referenceColumn), new ScoreAllGroupsInColumnItem(referenceColumn));
	}

}
