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
package org.caleydo.vis.rank.ui.mapping;

import static org.caleydo.vis.rank.ui.RenderStyle.HIST_HEIGHT;
import gleem.linalg.Vec2f;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.vis.rank.model.mapping.IMappingFunction;
import org.caleydo.vis.rank.ui.RenderStyle;

/**
 * @author Samuel Gratzl
 *
 */
public class MappingCrossUI<T extends IMappingFunction> extends AMappingFunctionMode<T> {
	private static final float GAP = 10;

	protected final boolean isNormalLeft; // otherwise right

	public MappingCrossUI(T model, boolean isNormalLeft) {
		super(model);
		this.isNormalLeft = isNormalLeft;
	}

	@Override
	public String getIcon() {
		return isNormalLeft ? RenderStyle.ICON_MAPPING_CROSS_LEFT : RenderStyle.ICON_MAPPING_CROSS_RIGHT;
	}

	@Override
	public String getName() {
		return isNormalLeft ? "Function" : "Mirrored Function";
	}

	@Override
	public void reset() {

	}

	@Override
	public void doLayout(IGLLayoutElement raw, IGLLayoutElement norm, IGLLayoutElement canvas, float x, float y,
			float w, float h) {
		final float histHeight = HIST_HEIGHT + RenderStyle.LABEL_HEIGHT;
		Vec2f rawL;
		Vec2f normL;
		Vec2f canvasL;
		if (isNormalLeft) {
			rawL = new Vec2f(x + histHeight + GAP, y + h - histHeight);
			normL = new Vec2f(x, y + GAP + x);
			canvasL = new Vec2f(histHeight + GAP + x, y + GAP);
		} else {
			rawL = new Vec2f(x, y + h - histHeight);
			normL = new Vec2f(w - x - histHeight, y + GAP);
			canvasL = new Vec2f(x, y + GAP);
		}

		raw.setBounds(rawL.x(), rawL.y(), w - histHeight - GAP * 2, histHeight);
		raw.asElement().setLayoutData(Boolean.TRUE);
		norm.setBounds(normL.x(), normL.y(), histHeight, h - histHeight - GAP * 2);
		norm.asElement().setLayoutData(!isNormalLeft);
		float x_canvas = canvasL.x();
		float y_canvas = canvasL.y();
		float w_canvas = w - histHeight - GAP * 2;
		float h_canvas = h - histHeight - GAP * 2;
		canvas.setBounds(x_canvas, y_canvas, w_canvas, h_canvas);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		renderMapping(g, w, h, true, isNormalLeft);

		super.renderImpl(g, w, h);
	}
}

