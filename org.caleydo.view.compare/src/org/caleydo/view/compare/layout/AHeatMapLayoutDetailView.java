package org.caleydo.view.compare.layout;

import gleem.linalg.Vec3f;

import java.util.HashMap;

import org.caleydo.core.data.selection.ContentVAType;
import org.caleydo.core.data.selection.Group;
import org.caleydo.view.compare.GroupInfo;
import org.caleydo.view.compare.rendercommand.ERenderCommandType;
import org.caleydo.view.compare.rendercommand.RenderCommandFactory;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;

public abstract class AHeatMapLayoutDetailView extends AHeatMapLayout {

	protected static float OVERVIEW_TOTAL_WIDTH_PORTION = 0.25f;
	protected static float OVERVIEW_GROUP_WIDTH_PORTION = 0.04f;
	protected static float OVERVIEW_HEATMAP_WIDTH_PORTION = 0.15f;
	protected static float OVERVIEW_SLIDER_WIDTH_PORTION = 0.06f;

	protected static float DETAIL_WIDTH_PORTION = 0.5f;
	protected static float DETAIL_HEATMAP_GAP_PORTION = 0.02f;
	protected static float OVERVIEW_TO_DETAIL_GAP_PORTION = 0.25f;

	protected static float CAPTION_LABEL_HEIGHT_PORTION = 0.03f;
	protected static float CAPTION_LABEL_HORIZONTAL_SPACING_PORTION = 0.03f;
	protected static float CAPTION_LABEL_VERTICAL_SPACING_PORTION = 0.01f;
	protected static float OVERVIEW_HEIGHT_PORTION = 0.95f;
	protected static float DETAIL_HEIGHT_PORTION = 0.95f;

	private HashMap<Integer, Vec3f> hashHeatMapPositions;
	private HashMap<Integer, Float> hashHeatMapHeights;

	public AHeatMapLayoutDetailView(RenderCommandFactory renderCommandFactory) {
		super(renderCommandFactory);
		localRenderCommands.add(renderCommandFactory
				.getRenderCommand(ERenderCommandType.OVERVIEW_GROUP_BAR));
		localRenderCommands.add(renderCommandFactory
				.getRenderCommand(ERenderCommandType.OVERVIEW_HEATMAP));
		localRenderCommands.add(renderCommandFactory
				.getRenderCommand(ERenderCommandType.OVERVIEW_SLIDER));
		localRenderCommands.add(renderCommandFactory
				.getRenderCommand(ERenderCommandType.CAPTION_LABEL));

		remoteRenderCommands.add(renderCommandFactory
				.getRenderCommand(ERenderCommandType.DETAIL_HEATMAPS));

		hashHeatMapPositions = new HashMap<Integer, Vec3f>();
		hashHeatMapHeights = new HashMap<Integer, Float>();
	}

	public void setLayoutParameters(float positionX, float positionY,
			float totalHeight, float totalWidth) {
		this.positionX = positionX;
		this.positionY = positionY;
		this.totalHeight = totalHeight;
		this.totalWidth = totalWidth;
	}

	public float getTotalOverviewWidth() {
		return totalWidth * OVERVIEW_TOTAL_WIDTH_PORTION;
	}

	public float getGapWidth() {
		return totalWidth * OVERVIEW_TO_DETAIL_GAP_PORTION;
	}

	public float getDetailWidth() {
		return totalWidth * DETAIL_WIDTH_PORTION;
	}

	public float getOverviewHeight() {
		return totalHeight * OVERVIEW_HEIGHT_PORTION;
	}

	public float getDetailHeight() {
		return totalHeight * DETAIL_HEIGHT_PORTION;
	}

	public float getOverviewGroupWidth() {
		return totalWidth * OVERVIEW_GROUP_WIDTH_PORTION;
	}

	public float getOverviewHeatmapWidth() {
		return totalWidth * OVERVIEW_HEATMAP_WIDTH_PORTION;
	}

	public float getOverviewSliderWidth() {
		return totalWidth * OVERVIEW_SLIDER_WIDTH_PORTION;
	}

	public float getOverviewMaxSliderHeight() {
		return totalHeight;
	}

	public float getOverviewMaxSliderPositionY() {
		return positionY + getOverviewHeight();
	}

	public float getOverviewMinSliderPositionY() {
		return positionY;
	}

	protected void calculateDetailHeatMapHeights() {

		hashHeatMapHeights.clear();

		HashMap<Group, GroupInfo> selectedGroups = heatMapWrapper
				.getSelectedGroups();
		int numTotalSamples = 0;
		int numTotalScalableSamples = 0;
		float totalHeatMapOverheadSize = 0;
		float totalMinSize = 0;

		for (Group group : selectedGroups.keySet()) {
			GLHeatMap heatMap = heatMapWrapper
					.getHeatMap(group.getGroupIndex());
			int numSamples = heatMap.getNumberOfVisibleElements();
			numTotalSamples += numSamples;
			totalHeatMapOverheadSize += heatMap.getRequiredOverheadSpacing();
			if (heatMap.isForceMinSpacing()) {
				totalMinSize += heatMap.getMinSpacing() * numSamples;
			} else {
				numTotalScalableSamples += numSamples;
			}
		}

		for (Group group : selectedGroups.keySet()) {
			GLHeatMap heatMap = heatMapWrapper
					.getHeatMap(group.getGroupIndex());
			int numSamplesInHeatMap = heatMap.getNumberOfVisibleElements();
			float currentHeatMapOverheadSize = heatMap
					.getRequiredOverheadSpacing();

			float spaceForHeatMaps = getDetailHeight()
					- (getDetailHeight() * DETAIL_HEATMAP_GAP_PORTION * (selectedGroups
							.size() - 1));
			spaceForHeatMaps -= totalHeatMapOverheadSize;
			float sampleHeight = (spaceForHeatMaps / (float) numTotalSamples);
			heatMap.setCaptionsImpossible(false);
			if (totalMinSize < getDetailHeight()
					&& sampleHeight < heatMap.getMinSpacing()) {
				if (heatMap.isForceMinSpacing()) {
					hashHeatMapHeights.put(group.getGroupIndex(),
							numSamplesInHeatMap * heatMap.getMinSpacing()
									+ currentHeatMapOverheadSize);
					continue;
				}
			} else {

				if (sampleHeight < heatMap.getMinSpacing()
						&& heatMap.isForceMinSpacing()) {
					heatMap.setCaptionsImpossible(true);
				}

				totalMinSize = 0;
				numTotalScalableSamples = numTotalSamples;
			}

			spaceForHeatMaps -= totalMinSize;

			hashHeatMapHeights
					.put(
							group.getGroupIndex(),
							((spaceForHeatMaps / (float) numTotalScalableSamples) * (float) numSamplesInHeatMap)
									+ currentHeatMapOverheadSize);
		}
	}

	public float getDetailHeatMapHeight(int heatMapID) {
		return hashHeatMapHeights.get(heatMapID);
	}

	public float getDetailHeatMapGapHeight() {
		return getDetailHeight() * DETAIL_HEATMAP_GAP_PORTION;
	}

	public float getCaptionLabelWidth() {
		return totalWidth;
	}

	public float getCaptionLabelHeight() {
		return totalHeight * CAPTION_LABEL_HEIGHT_PORTION;
	}

	public float getCaptionLabelHorizontalSpacing() {
		return totalWidth * CAPTION_LABEL_HORIZONTAL_SPACING_PORTION;
	}

	public float getCaptionLabelVerticalSpacing() {
		return totalHeight * CAPTION_LABEL_VERTICAL_SPACING_PORTION;
	}

	protected void calculateHeatMapPositions() {

		hashHeatMapPositions.clear();
		HashMap<Group, GroupInfo> selectedGroups = heatMapWrapper
				.getSelectedGroups();

		Vec3f detailPosition = getDetailPosition();
		float currentPositionY = detailPosition.y() + getDetailHeight();

		for (Group group : heatMapWrapper.getSet().getContentVA(
				ContentVAType.CONTENT).getGroupList()) {

			if (!selectedGroups.containsKey(group))
				continue;
			GLHeatMap heatMap = heatMapWrapper
					.getHeatMap(group.getGroupIndex());
			if (heatMap == null)
				continue;

			float heatMapHeight = getDetailHeatMapHeight(group.getGroupIndex());
			hashHeatMapPositions.put(group.getGroupIndex(), new Vec3f(
					detailPosition.x(), currentPositionY - heatMapHeight,
					detailPosition.z()));
			currentPositionY -= (heatMapHeight + getDetailHeatMapGapHeight());
		}
	}

	@Override
	public Vec3f getDetailHeatMapPosition(int heatMapID) {
		return hashHeatMapPositions.get(heatMapID);
	}

	@Override
	public void calculateDrawingParameters() {
		calculateDetailHeatMapHeights();
		calculateHeatMapPositions();
	}
}
