/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.stratomex.tourguide.internal;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2GL3;

import org.caleydo.core.view.opengl.layout.ALayoutRenderer;

/**
 * @author Samuel Gratzl
 *
 */
public class TemplateHighlightRenderer extends ALayoutRenderer {
	public TemplateHighlightRenderer() {
	}

	@Override
	protected void renderContent(GL2 gl) {
		final float offset = layoutManager.getPixelGLConverter().getGLWidthForPixelWidth(0.75f);
		gl.glColor4f(0.95f, 0.95f, 0.95f, 1);
		gl.glBegin(GL2GL3.GL_QUADS);
		gl.glVertex3f(0, 0, 0);
		gl.glVertex3f(x, 0, 0);
		gl.glVertex3f(x, y, 0);
		gl.glVertex3f(0, y, 0);
		gl.glEnd();
		gl.glColor4f(0.2f, 0.2f, 0.2f, 1);
		gl.glBegin(GL.GL_LINE_LOOP);
		gl.glVertex3f(offset, offset, 0.02f);
		gl.glVertex3f(x - offset, offset, 0.02f);
		gl.glVertex3f(x - offset, y - offset, 0.02f);
		gl.glVertex3f(offset, y - offset, 0.02f);
		gl.glEnd();
	}

	@Override
	protected boolean permitsWrappingDisplayLists() {
		return true;
	}

}
