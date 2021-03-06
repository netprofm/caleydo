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
package org.caleydo.view.entourage;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.layout2.util.GLElementWindow.GLTitleBar;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.entourage.GLEntourage.PathwayMultiFormInfo;

/**
 * @author Christian
 *
 */
public class PortalHighlightRenderer extends PickableGLElement {

	private final Rect location;
	private final GLPathwayWindow window;
	private final PathwayMultiFormInfo info;

	/**
	 *
	 */
	public PortalHighlightRenderer(PathwayMultiFormInfo info, Rect location, GLPathwayWindow window) {
		this.location = location;
		this.window = window;
		this.info = info;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (info.window.isZoomed())
			return;
		g.incZ(0.5f);
		g.color(PortalRenderStyle.CONTEXT_PORTAL_COLOR)
				.lineWidth(2)
				.drawRoundedRect(location.x() + 1, location.y() + 1, location.width() + 1, location.height() + 1,
						location.height() / 5.0f);
		g.lineWidth(1);
		g.incZ(-0.5f);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (info.window.isZoomed())
			return;
		g.incZ(0.5f);
		g.fillRect(location.x() + 1, location.y() + 1, location.width() + 1, location.height() + 1);
		g.lineWidth(1);
		g.incZ(-0.5f);
	}

	@Override
	protected void onMouseOver(Pick pick) {
		// b6f2b3#
		window.setTitleBarColor(new Color("d7a8c0"));
		window.setBackgroundColor(new Color("ffe5f3"));
		// window.titleBar.setHighlight(true);
		// System.out.println("highight over");
	}

	// There seems to be a bug with picking that causes mouse out events not to arrive correctly.
	@Override
	protected void onMouseOut(Pick pick) {
		// window.setBackgroundColor(new Color(1, 0, 1, 1f));
		// System.out.println("highight out");
		window.setTitleBarColor(GLTitleBar.DEFAULT_COLOR);
		window.setBackgroundColor(GLPathwayBackground.DEFAULT_COLOR);
		window.getTitleBar().setHighlight(false);
	}

	// @Override
	// public void onPathwayTextureSelected(PathwayGraph pathway) {
	// // window.setBackgroundColor(GLPathwayBackground.DEFAULT_COLOR);
	// }

}
