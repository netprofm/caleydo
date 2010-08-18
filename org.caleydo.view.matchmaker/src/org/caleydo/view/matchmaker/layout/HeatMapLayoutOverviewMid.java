package org.caleydo.view.matchmaker.layout;

import gleem.linalg.Vec3f;

import org.caleydo.view.matchmaker.rendercommand.RenderCommandFactory;

public class HeatMapLayoutOverviewMid extends AHeatMapLayoutOverview {

	public HeatMapLayoutOverviewMid(RenderCommandFactory renderCommandFactory) {
		super(renderCommandFactory);
	}

	@Override
	public Vec3f getCaptionLabelPosition(float textWidth) {
		return new Vec3f(positionX + (totalWidth / 2.0f) - (textWidth / 2.0f), positionY
				+ getOverviewHeight() + getCaptionLabelVerticalSpacing(), 0.0f);
	}

	@Override
	public Vec3f getOverviewHeatMapPosition() {
		return new Vec3f(positionX + (totalWidth - getOverviewHeatMapWidth()) / 2.0f,
				positionY, 0.0f);
	}

}
