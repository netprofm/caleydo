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
package org.caleydo.view.entourage.event;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.AVertexRepBasedEventFactory;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.entourage.EEmbeddingID;

/**
 * @author Christian
 *
 */
public class AddPathwayEventFactory extends AVertexRepBasedEventFactory {

	/**
	 * @param eventClass
	 * @param eventSpace
	 */
	public AddPathwayEventFactory(String eventSpace) {
		super(eventSpace);
	}

	@Override
	public AEvent create(PathwayVertexRep vertexRep) {
		if (vertexRep != null && vertexRep.getType() == EPathwayVertexType.map) {
			PathwayGraph pathway = vertexRep.getPathway();
			if (pathway != null) {
				AddPathwayEvent event = new AddPathwayEvent(pathway, EEmbeddingID.PATHWAY_LEVEL2);
				event.setEventSpace(eventSpace);
				return event;
			}
		}
		return null;
	}

}
