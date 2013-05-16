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
package org.caleydo.view.tourguide.spi.algorithm;

import java.util.Collection;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDType;

/**
 * @author Samuel Gratzl
 *
 */
public interface IComputeElement extends Iterable<Integer> {
	String getPersistentID();

	IDType getIdType();

	Collection<Group> getGroups();

	int getGroupSize();

	/**
	 * @param group
	 *            may be null for all
	 * @return
	 */
	Collection<Integer> of(Group group);

	/**
	 * @return
	 */
	IDataDomain getDataDomain();

	String getLabel();

	int size();

}
