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
package org.caleydo.view.enroute.mappeddataview;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.id.IDMappingManager;
import org.caleydo.core.id.IDMappingManagerRegistry;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.datadomain.genetic.GeneticDataDomain;

/**
 * @author alexsb
 *
 */
public abstract class ContentRenderer extends SelectableRenderer {

	Integer geneID;
	TablePerspective tablePerspective;
	Perspective experimentPerspective;
	GeneticDataDomain dataDomain;
	Integer davidID;
	float z = 0.05f;
	Group group;
	/** The sample ID Type of the local sample VA */
	IDType sampleIDType;
	APickingListener pickingListener;

	/**
	 * Determines whether the renderer should render in highlight mode.
	 */
	boolean isHighlightMode = false;

	IDMappingManager sampleIDMappingManager;

	public ContentRenderer(Integer geneID, Integer davidID, GeneticDataDomain dataDomain,
			TablePerspective tablePerspective, Perspective experimentPerspective, AGLView parentView, Group group,
			boolean isHighlightMode) {
		super(parentView, new Color(MappedDataRenderer.BAR_COLOR));
		this.davidID = davidID;
		this.geneID = geneID;

		this.dataDomain = dataDomain;
		this.tablePerspective = tablePerspective;
		this.experimentPerspective = experimentPerspective;
		this.group = group;
		this.isHighlightMode = isHighlightMode;
		sampleIDType = experimentPerspective.getIdType();
		sampleIDMappingManager = IDMappingManagerRegistry.get().getIDMappingManager(sampleIDType);
		init();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		unRegisterPickingListener();
	}

	public abstract void init();

	private void unRegisterPickingListener() {
		parentView.removePickingListener(pickingListener);
	}

}
