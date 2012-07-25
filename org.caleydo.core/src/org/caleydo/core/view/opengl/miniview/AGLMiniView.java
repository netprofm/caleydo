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
package org.caleydo.core.view.opengl.miniview;

import java.util.ArrayList;
import javax.media.opengl.GL2;
import org.caleydo.core.data.collection.dimension.AColumn;

/**
 * Abstract class for all kinds of mini views.
 * 
 * @author Marc Streit
 */
public abstract class AGLMiniView
	implements IGLMiniView {

	protected float fHeight;

	protected float fWidth;

	protected ArrayList<AColumn> alDimension;

	public void setData(ArrayList<AColumn> alDimensions) {

	}

	@Override
	public abstract void render(GL2 gl, float fXOrigin, float fYOrigin, float fZOrigin);

	@Override
	public final float getWidth() {

		return fWidth;
	}

	@Override
	public final float getHeight() {

		return fHeight;
	}

	@Override
	public void setWidth(final float fWidth) {

		this.fWidth = fWidth;
	}

	@Override
	public void setHeight(final float fHeight) {

		this.fHeight = fHeight;
	}
}
