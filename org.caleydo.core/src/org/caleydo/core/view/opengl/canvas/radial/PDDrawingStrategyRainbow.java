package org.caleydo.core.view.opengl.canvas.radial;

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import org.caleydo.core.util.mapping.color.ColorMapping;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.ColorMarkerPoint;
import org.caleydo.core.util.mapping.color.EColorMappingType;

public class PDDrawingStrategyRainbow
	extends PDDrawingStrategyChildIndicator {

	public PDDrawingStrategyRainbow() {

		ArrayList<ColorMarkerPoint> alMarkerPoints = new ArrayList<ColorMarkerPoint>();

		alMarkerPoints.add(new ColorMarkerPoint(0.0f, 1.0f, 0.0f, 0.0f));
		alMarkerPoints.add(new ColorMarkerPoint((120.0f / 360.0f), 0.0f, 1.0f, 0.0f));
		alMarkerPoints.add(new ColorMarkerPoint((240.0f / 360.0f), 0.0f, 0.0f, 1.0f));
		alMarkerPoints.add(new ColorMarkerPoint(1.0f, 1.0f, 0.0f, 0.0f));

		ColorMappingManager.get().initColorMapping(EColorMappingType.RAINBOW, alMarkerPoints);
	}

	@Override
	public void drawPartialDisc(GL gl, GLU glu, PartialDisc pdDiscToDraw) {

		if (pdDiscToDraw == null)
			return;

		float fStartAngle = pdDiscToDraw.getCurrentStartAngle();
		float fAngle = pdDiscToDraw.getCurrentAngle();
		float fInnerRadius = pdDiscToDraw.getCurrentInnerRadius();
		float fWidth = pdDiscToDraw.getCurrentWidth();

		float fMidAngle = fStartAngle + (fAngle / 2.0f);
		while (fMidAngle >= 360) {
			fMidAngle -= 360;
		}
		while (fMidAngle < 0) {
			fMidAngle += 360;
		}
		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);

		if ((pdDiscToDraw.getCurrentDepth() == 1) && (pdDiscToDraw.hasChildren())) {
			drawChildIndicator(gl, fInnerRadius, fWidth, fStartAngle, fAngle);
		}

		ColorMapping cmRainbow = ColorMappingManager.get().getColorMapping(EColorMappingType.RAINBOW);
		float fArRGB[] = cmRainbow.getColor(fMidAngle / 360.0f);

		gl.glColor4f(fArRGB[0], fArRGB[1], fArRGB[2], 1);

		GLPrimitives.renderPartialDisc(gl, glu, fInnerRadius, fInnerRadius + fWidth, fStartAngle, fAngle,
			iNumSlicesPerFullDisc);

		gl.glColor4f(1, 1, 1, 1);
		GLPrimitives.renderPartialDiscBorder(gl, glu, fInnerRadius, fInnerRadius + fWidth, fStartAngle,
			fAngle, iNumSlicesPerFullDisc, 2);

		gl.glPopAttrib();
	}

	@Override
	public void drawFullCircle(GL gl, GLU glu, PartialDisc pdDiscToDraw) {

		if (pdDiscToDraw == null)
			return;

		float fRadius = pdDiscToDraw.getCurrentWidth();

		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);

		gl.glColor4f(1, 1, 1, 1);
		GLPrimitives.renderCircle(gl, glu, fRadius, iNumSlicesPerFullDisc);

		gl.glColor4f(1, 1, 1, 1);
		GLPrimitives.renderCircleBorder(gl, glu, fRadius, iNumSlicesPerFullDisc, 2);

		gl.glPopAttrib();
	}

}
