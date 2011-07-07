package org.caleydo.view.heatmap.heatmap.renderer;

import static org.caleydo.view.heatmap.HeatMapRenderStyle.FIELD_Z;

import javax.media.opengl.GL2;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.collection.storage.AStorage;
import org.caleydo.core.data.collection.storage.EDataRepresentation;
import org.caleydo.core.data.selection.ContentSelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.util.mapping.color.ColorMapper;
import org.caleydo.core.util.mapping.color.ColorMappingManager;
import org.caleydo.core.util.mapping.color.EColorMappingType;
import org.caleydo.core.view.opengl.canvas.PixelGLConverter;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;
import org.caleydo.view.heatmap.heatmap.template.AHeatMapTemplate;
import org.caleydo.view.heatmap.uncertainty.GLUncertaintyHeatMap;
import org.eclipse.jface.layout.PixelConverter;

public class BarPlotRenderer extends AContentRenderer {

	public BarPlotRenderer(GLHeatMap heatMap) {
		super(heatMap);

	}

	@Override
	public void updateSpacing(ElementLayout parameters) {

		AHeatMapTemplate heatMapTemplate = heatMap.getTemplate();

		int contentElements = heatMap.getContentVA().size();

		ContentSelectionManager selectionManager = heatMap
				.getContentSelectionManager();
		if (heatMap.isHideElements()) {

			contentElements -= selectionManager
					.getNumberOfElements(GLHeatMap.SELECTION_HIDDEN);
		}

		contentSpacing.calculateContentSpacing(contentElements, heatMap
				.getStorageVA().size(), parameters.getSizeScaledX(), parameters
				.getSizeScaledY(), heatMapTemplate.getMinSelectedFieldHeight());
		heatMapTemplate.setContentSpacing(contentSpacing);

		// ((AContentRenderer) renderer).setContentSpacing(contentSpacing);
	}

	@Override
	public void render(final GL2 gl) {

		contentSpacing.getYDistances().clear();
		float yPosition = y;
		float xPosition = 0;
		float fieldHeight = 0;
		float fieldWidth = x;

		int iCount = 0;

		for (Integer contentID : heatMap.getContentVA()) {
			iCount++;
			fieldHeight = contentSpacing.getFieldHeight(contentID);

			// we treat normal and deselected the same atm

			if (heatMap.isHideElements()
					&& heatMap.getContentSelectionManager().checkStatus(
							GLHeatMap.SELECTION_HIDDEN, contentID)) {
				contentSpacing.getYDistances().add(yPosition);
				continue;
			}

			yPosition -= fieldHeight;
			xPosition = 0;

			// GLHelperFunctions.drawPointAt(gl, 0, fYPosition, 0);
			ISet set = heatMap.getSet();
			if (set == null)
				return;
			float[] certainty = new float[3];
			try {
				certainty[0] = set.getNormalizedUncertainty(contentID);
				certainty[1] = set.getNormalizedUncertainty(contentID);
				certainty[2] = set.getNormalizedUncertainty(contentID);
			} catch (IllegalStateException ex) {
				certainty[0] = 1f;
				certainty[1] = 1f;
				certainty[2] = 1f;
			}

			PixelGLConverter conv = heatMap.getParentGLCanvas()
					.getPixelGLConverter();
			int screenHeight = conv.getPixelHeightForGLHeight(fieldHeight);
			if (screenHeight < 3) {
				int i = 0;
				certainty[i] = certainty[i] > 1 ? 1 : certainty[i];
				certainty[i] = certainty[i] < 0 ? 0 : certainty[i];

				renderLine(gl, yPosition, xPosition, fieldHeight, fieldWidth,
						certainty[i], GLUncertaintyHeatMap.lightLight);
			} else {
				for (int i = 0; i < certainty.length; i++) {
					float height = fieldHeight / (float) certainty.length;
					float yPos = yPosition + height * i;
					certainty[i] = certainty[i] > 1 ? 1 : certainty[i];
					certainty[i] = certainty[i] < 0 ? 0 : certainty[i];

					renderLine(gl, yPos, xPosition, height, fieldWidth,
							certainty[i],
							GLUncertaintyHeatMap.levelLightColor[i]);
				}
			}

			contentSpacing.getYDistances().add(yPosition);

		}
	}

	private void renderLine(final GL2 gl, final float fYPosition,
			final float fXPosition, final float fFieldHeight,
			final float fFieldWidth, final float certainty, float[] rgba) {

		float certainWidth = fFieldWidth * (1 - certainty);
		float unCertainWidth = fFieldWidth * (certainty);

		// gl.glPushName(heatMap.getPickingManager().getPickingID(heatMap.getID(),
		// EPickingType.HEAT_MAP_STORAGE_SELECTION, iStorageIndex));
		// gl.glPushName(heatMap.getPickingManager().getPickingID(heatMap.getID(),
		// EPickingType.HEAT_MAP_LINE_SELECTION, iContentIndex));

		// uncertain

		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor4fv(GLUncertaintyHeatMap.darkDark, 0);
		gl.glVertex3f(fXPosition, fYPosition, FIELD_Z);
		gl.glVertex3f(fXPosition + unCertainWidth, fYPosition, FIELD_Z);
		gl.glVertex3f(fXPosition + unCertainWidth, fYPosition + fFieldHeight,
				FIELD_Z);
		gl.glVertex3f(fXPosition, fYPosition + fFieldHeight, FIELD_Z);
		gl.glEnd();

		// certain
		gl.glBegin(GL2.GL_POLYGON);
		gl.glColor4fv(rgba, 0);
		gl.glVertex3f(fXPosition + unCertainWidth, fYPosition, FIELD_Z);
		gl.glVertex3f(fXPosition + certainWidth + unCertainWidth, fYPosition,
				FIELD_Z);
		gl.glVertex3f(fXPosition + certainWidth + unCertainWidth, fYPosition
				+ fFieldHeight, FIELD_Z);
		gl.glVertex3f(fXPosition + unCertainWidth, fYPosition + fFieldHeight,
				FIELD_Z);
		gl.glEnd();

		// gl.glPopName();
		// gl.glPopName();
	}

	public float getYCoordinateByContentIndex(int contentIndex) {
		if (contentSpacing != null)
			return y
					- contentSpacing.getYDistances().get(contentIndex)
					- contentSpacing.getFieldHeight(heatMap.getContentVA().get(
							contentIndex)) / 2;
		return 0;
	}

	public float getXCoordinateByStorageIndex(int storageIndex) {
		return contentSpacing.getFieldWidth() * storageIndex;
	}

	@Override
	public String toString() {
		return "BarPlotRenderer";
	}

}
