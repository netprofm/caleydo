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
package org.caleydo.view.tourguide.internal.stratomex.state;

import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.core.view.opengl.layout.AForwardingRenderer;
import org.caleydo.core.view.opengl.layout.ALayoutRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.core.view.opengl.util.text.TextUtils;

/**
 * 
 * @author Samuel Gratzl
 * 
 */
class PreviewRenderer extends AForwardingRenderer {
	private final AGLView view;
	private final String label;

	public PreviewRenderer(ALayoutRenderer renderer, AGLView view, String label) {
		super(renderer);
		this.view = view;
		this.label = label;
	}
	@Override
	public void setLimits(float x, float y) {
		super.setLimits(x, y);
		currentRenderer.setLimits(x * 0.9f, y * .25f);
	}

	@Override
	protected void renderContent(GL2 gl) {
		float w = x;
		float h = y;
		float ph = h * 0.25f;
		float pw = w * 0.9f;
		gl.glPushMatrix();
		gl.glTranslatef(w * 0.05f, h * 0.5f, 0);
		float th = view.getPixelGLConverter().getGLHeightForPixelHeight(16);
		float offset = view.getPixelGLConverter().getGLHeightForPixelHeight(2);

		final CaleydoTextRenderer text = view.getTextRenderer();
		{
			float twi = text.getTextWidth("Preview", th);
			text.renderTextInBounds(gl, "Preview", pw * 0.5f - twi * 0.5f, ph * 1.05f, 1.f, pw, th);
		}

		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(-offset, -offset, 0);
		gl.glVertex3f(pw + offset, -offset, 0);
		gl.glVertex3f(pw + offset, ph + offset, 0);
		gl.glVertex3f(-offset, ph + offset, 0);
		gl.glEnd();

		super.renderContent(gl);

		List<String> texts = TextUtils.wrap(text, label, pw, th);
		{
			float yl = -texts.size() * th;
			for (String line : texts) {
				float twi = text.getTextWidth(line, th);
				text.renderTextInBounds(gl, line, pw * 0.5f - twi * 0.5f, yl, 1.f, pw, th);
				yl -= th;
			}
		}

		gl.glPopMatrix();
	}
}