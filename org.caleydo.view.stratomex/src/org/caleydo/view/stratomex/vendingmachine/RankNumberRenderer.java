package org.caleydo.view.stratomex.vendingmachine;

import javax.media.opengl.GL2;
import org.caleydo.core.view.opengl.layout.LayoutRenderer;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;

public class RankNumberRenderer
	extends LayoutRenderer {

	private final static float[] RANK_NUMBER_COLOR = { 0f, 0f, 0f, 1};
	
	private CaleydoTextRenderer textRenderer;
	
	private String rankNumber;
	
	public RankNumberRenderer(String rankNumber, CaleydoTextRenderer textRender) {
		this.textRenderer = textRender;
		this.rankNumber = rankNumber;
	}

	@Override
	public void renderContent(GL2 gl) {

		textRenderer.setColor(RANK_NUMBER_COLOR);
		textRenderer.renderText(gl, rankNumber, 0.03f, 0, 0, 0.007f, 3);
	}
	
	@Override
	protected boolean permitsDisplayLists() {
		return false;
	}
}
