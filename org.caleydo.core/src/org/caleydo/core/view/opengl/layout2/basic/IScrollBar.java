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
package org.caleydo.core.view.opengl.layout2.basic;

import gleem.linalg.Vec2f;

import java.awt.Point;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;

/**
 * @author Samuel Gratzl
 *
 */
public interface IScrollBar extends IGLRenderer, IPickingListener {
	void renderPick(GLGraphics g, float w, float h, GLElement parent);

	float setBounds(float offset, float view, float total);

	void setCallback(IScrollBarCallback callback);

	public interface IScrollBarCallback {
		void onScrollBarMoved(IScrollBar scrollBar, float offset);

		/**
		 * @param pickedPoint
		 * @return
		 */
		Vec2f toRelative(Point pickedPoint);

		/**
		 * @param scrollBar
		 * @return
		 */
		float getTotal(IScrollBar scrollBar);

		/**
		 *
		 */
		void repaint();
	}
}
