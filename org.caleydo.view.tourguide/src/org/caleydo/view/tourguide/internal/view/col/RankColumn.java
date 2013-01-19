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
package org.caleydo.view.tourguide.internal.view.col;


import static org.caleydo.core.view.opengl.layout.ElementLayouts.wrap;

import java.util.List;

import org.caleydo.core.util.base.ConstantLabelProvider;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.PickingRenderer;
import org.caleydo.core.view.opengl.layout.util.TextureRenderer;
import org.caleydo.view.tourguide.api.query.ScoreQuery;
import org.caleydo.view.tourguide.api.query.ScoringElement;
import org.caleydo.view.tourguide.internal.TourGuideRenderStyle;
import org.caleydo.view.tourguide.internal.view.ScoreQueryUI;

public class RankColumn extends ATableColumn {
	public RankColumn(AGLView view) {
		super(view);
		this.setPixelSizeX(getTextWidth("99."));
		init();
	}

	@Override
	protected ElementLayout createHeader() {
		Row row = new Row();
		row.setGrabX(true);
		// row.setLeftToRight(false);
		ElementLayout b = wrap(new TextureRenderer(TourGuideRenderStyle.ICON_TABLE_FILTER, view.getTextureManager()),
				16);
		b.setGrabY(true);
		b.addBackgroundRenderer(new PickingRenderer(ScoreQueryUI.EDIT_FILTER, 1, view));
		row.append(b);
		return row;
	}

	@Override
	public void setData(List<ScoringElement> data, ScoreQuery query) {
		this.clearBody();
		if (!query.isSorted()) {
			return;
		}
		for (int i = 0; i < data.size(); ++i)
			this.addTd(createRightLabel(new ConstantLabelProvider(String.format("%d.", i + 1)), -1), i);
	}

	@Override
	protected int getMinWidth() {
		return getPixelSizeX();
	}
}