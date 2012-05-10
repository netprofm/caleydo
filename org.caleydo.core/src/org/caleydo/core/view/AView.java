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
package org.caleydo.core.view;

import java.util.Set;
import org.caleydo.core.data.AUniqueObject;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.id.IDType;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.event.view.SelectionCommandEvent;
import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.opengl.util.texture.EIconTextures;
import org.eclipse.swt.widgets.Composite;

/**
 * Abstract class that is the base of all view representations. It holds the the own view ID, the parent ID
 * and the attributes that needs to be processed.
 * 
 * @author Marc Streit
 */
public abstract class AView
	extends AUniqueObject
	implements IView {

	/** The plugin name of the view */
	public String viewType;

	public static EIconTextures icon = EIconTextures.NO_ICON_AVAILABLE;

	protected GeneralManager generalManager;

	protected EventPublisher eventPublisher;

	protected Composite parentComposite;

	/**
	 * Constructor.
	 */
	public AView(int viewID, Composite parentComposite) {
		super(viewID);

		generalManager = GeneralManager.get();
		eventPublisher = generalManager.getEventPublisher();
		this.parentComposite = parentComposite;
	}

	/**
	 * Empty implementation of initialize, should be overwritten in views if needed.
	 */
	@Override
	public void initialize() {

	}

	/**
	 * creates and sends a {@link TriggerSelectioCommand} event and distributes it via the related
	 * eventPublisher.
	 * 
	 * @param expression_index
	 *            type of genome this selection command refers to
	 * @param command
	 *            selection-command to distribute
	 */
	@Deprecated
	protected void sendSelectionCommandEvent(IDType genomeType, SelectionCommand command) {
		SelectionCommandEvent event = new SelectionCommandEvent();
		event.setSender(this);
		event.setSelectionCommand(command);
		event.tableIDCategory(genomeType.getIDCategory());
		eventPublisher.triggerEvent(event);
	}

	@Override
	public String getViewType() {
		return viewType;
	}

	public Set<IDataDomain> getDataDomains() {
		return null;
	}

	/**
	 * Determines whether the view displays concrete data of a data set or not.
	 * 
	 * @return
	 */
	public boolean isDataView() {
		return false;
	}

	public Composite getParentComposite() {
		return parentComposite;
	}
}
