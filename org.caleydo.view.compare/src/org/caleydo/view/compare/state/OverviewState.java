package org.caleydo.view.compare.state;

import gleem.linalg.Vec3f;

import java.awt.Point;
import java.util.ArrayList;

import javax.media.opengl.GL;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.selection.SelectionCommand;
import org.caleydo.core.data.selection.StorageVAType;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.view.opengl.camera.IViewFrustum;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.GLCoordinateUtils;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.compare.GLCompare;
import org.caleydo.view.compare.HeatMapWrapper;
import org.caleydo.view.compare.SetBar;
import org.caleydo.view.compare.layout.AHeatMapLayout;
import org.caleydo.view.compare.layout.HeatMapLayoutOverviewLeft;
import org.caleydo.view.compare.layout.HeatMapLayoutOverviewMid;
import org.caleydo.view.compare.layout.HeatMapLayoutOverviewRight;
import org.caleydo.view.compare.rendercommand.RenderCommandFactory;
import org.caleydo.view.compare.renderer.CompareConnectionBandRenderer;
import org.caleydo.view.compare.renderer.ICompareConnectionRenderer;

import com.sun.opengl.util.j2d.TextRenderer;

public class OverviewState extends ACompareViewStateStatic {

	private static final float HEATMAP_WRAPPER_OVERVIEW_GAP_PORTION = 0.8f;
	private static final float HEATMAP_WRAPPER_SPACE_PORTION = 0.7f;

	public OverviewState(GLCompare view, int viewID, TextRenderer textRenderer,
			TextureManager textureManager, PickingManager pickingManager,
			GLMouseListener glMouseListener, SetBar setBar,
			RenderCommandFactory renderCommandFactory, EDataDomain dataDomain,
			IUseCase useCase, DragAndDropController dragAndDropController,
			CompareViewStateController compareViewStateController) {

		super(view, viewID, textRenderer, textureManager, pickingManager,
				glMouseListener, setBar, renderCommandFactory, dataDomain, useCase,
				dragAndDropController, compareViewStateController);
		this.setBar.setPosition(new Vec3f(0.0f, 0.0f, 0.5f));
		numSetsInFocus = 4;
	}

	@Override
	public void drawActiveElements(GL gl) {

		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			heatMapWrapper.drawRemoteItems(gl, glMouseListener, pickingManager);
		}
	}

	@Override
	public void buildDisplayList(GL gl) {

		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			heatMapWrapper.drawLocalItems(gl, textureManager, pickingManager,
					glMouseListener, viewID);
		}

		IViewFrustum viewFrustum = view.getViewFrustum();

		setBar.setWidth(viewFrustum.getWidth());
		setBar.render(gl);

		for (int i = 0; i < heatMapWrappers.size() - 1; i++) {
			// renderTree(gl, heatMapWrappers.get(i), heatMapWrappers.get(i +
			// 1));
			// renderOverviewRelations(gl, heatMapWrappers.get(i),
			// heatMapWrappers
			// .get(i + 1));

			renderIndiviudalLineRelations(gl, heatMapWrappers.get(i), heatMapWrappers.get(i+1));
			
			if (bandBundlingActive) {
				
				renderOverviewToDetailBandRelations(gl, heatMapWrappers.get(i), true);
				renderOverviewToDetailBandRelations(gl, heatMapWrappers.get(i+1), false);
				renderDetailBandRelations(gl);
			}
		}
	}

	@Override
	public void init(GL gl) {

		compareConnectionRenderer.init(gl);
		setsChanged = false;

	}
	
	@Override
	public ECompareViewStateType getStateType() {
		return ECompareViewStateType.OVERVIEW;
	}

	@Override
	public void duplicateSetBarItem(int itemID) {
		setBar.handleDuplicateSetBarItem(itemID);

	}

	@Override
	public void handleSelectionUpdate(ISelectionDelta selectionDelta,
			boolean scrollToSelection, String info) {
		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			heatMapWrapper.handleSelectionUpdate(selectionDelta, scrollToSelection, info);
			heatMapWrapper.getOverview().updateHeatMapTextures(
					heatMapWrapper.getContentSelectionManager());
		}
	}

	@Override
	public void adjustPValue() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxSetsInFocus() {
		return 6;
	}

	@Override
	public int getMinSetsInFocus() {
		return 2;
	}

	@Override
	public void handleStateSpecificPickingEvents(EPickingType ePickingType,
			EPickingMode pickingMode, int iExternalID, Pick pick, boolean isControlPressed) {

	}

	@Override
	public void handleSelectionCommand(EIDCategory category,
			SelectionCommand selectionCommand) {

		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			if (category == heatMapWrapper.getContentSelectionManager().getIDType()
					.getCategory())
				heatMapWrapper.getContentSelectionManager().executeSelectionCommand(
						selectionCommand);
			else
				return;
		}
	}

	@Override
	public void setSetsInFocus(ArrayList<ISet> setsInFocus) {
		// FIXME: Maybe we can put this in the base class.

		if (setsInFocus.size() >= getMinSetsInFocus()
				&& setsInFocus.size() <= getMaxSetsInFocus()) {

			this.setsInFocus = setsInFocus;

			if (layouts.isEmpty() || setsInFocus.size() != layouts.size()) {
				layouts.clear();
				heatMapWrappers.clear();

				int heatMapWrapperID = 0;
				for (ISet set : setsInFocus) {
					AHeatMapLayout layout = null;
					if (heatMapWrapperID == 0) {
						layout = new HeatMapLayoutOverviewLeft(renderCommandFactory);
					} else if (heatMapWrapperID == setsInFocus.size() - 1) {
						layout = new HeatMapLayoutOverviewRight(renderCommandFactory);
					} else {
						layout = new HeatMapLayoutOverviewMid(renderCommandFactory);
					}

					layouts.add(layout);

					HeatMapWrapper heatMapWrapper = new HeatMapWrapper(heatMapWrapperID,
							layout, view, null, useCase, view, dataDomain);
					heatMapWrappers.add(heatMapWrapper);
					heatMapWrapperID++;
				}
			}

			// FIXME: Use array of relations?
			// ISet setLeft = setsInFocus.get(0);
			// ISet setRight = setsInFocus.get(1);
			// relations = SetComparer.compareSets(setLeft, setRight);

			for (int i = 0; i < heatMapWrappers.size(); i++) {
				HeatMapWrapper heatMapWrapper = heatMapWrappers.get(i);
				heatMapWrapper.setSet(setsInFocus.get(i));
			}
			setsChanged = true;
			numSetsInFocus = setsInFocus.size();

			view.setDisplayListDirty();
		}
	}

	@Override
	public void handleMouseWheel(GL gl, int amount, Point wheelPoint) {
		if (amount < 0) {

			OverviewToDetailTransition transition = (OverviewToDetailTransition) compareViewStateController
					.getState(ECompareViewStateType.OVERVIEW_TO_DETAIL_TRANSITION);

			float[] wheelPointWorldCoordinates = GLCoordinateUtils
					.convertWindowCoordinatesToWorldCoordinates(gl, wheelPoint.x,
							wheelPoint.y);

			int itemOffset = 0;
			for (int i = 0; i < layouts.size() - 1; i++) {

				if ((i == layouts.size() - 2)
						&& (wheelPointWorldCoordinates[0] >= layouts.get(i).getPosition()
								.x())) {
					itemOffset = i;
					break;
				}

				if ((wheelPointWorldCoordinates[0] >= layouts.get(i).getPosition().x())
						&& (wheelPointWorldCoordinates[0] <= layouts.get(i + 1)
								.getPosition().x()
								+ (layouts.get(i + 1).getWidth() / 2.0f))) {
					itemOffset = i;
					break;
				}
			}
			compareViewStateController
					.setCurrentState(ECompareViewStateType.OVERVIEW_TO_DETAIL_TRANSITION);

			transition.initTransition(gl, itemOffset);
			view.setDisplayListDirty();
		}

	}

	@Override
	protected void setupLayouts() {

		IViewFrustum viewFrustum = view.getViewFrustum();
		float setBarHeight = setBar.getHeight();
		float heatMapWrapperPosY = setBar.getPosition().y() + setBarHeight;

		float heatMapWrapperPosX = 0.0f;

		float spaceForHeatMapWrapperOverviews = (1.0f - HEATMAP_WRAPPER_OVERVIEW_GAP_PORTION)
				* viewFrustum.getWidth();
		float heatMapWrapperWidth = HEATMAP_WRAPPER_SPACE_PORTION
				* viewFrustum.getWidth() / (float) heatMapWrappers.size();
		int numTotalExperiments = 0;
		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			numTotalExperiments += heatMapWrapper.getSet().getStorageVA(
					StorageVAType.STORAGE).size();
		}
		float heatMapWrapperGapWidth = (1 - HEATMAP_WRAPPER_SPACE_PORTION)
				* viewFrustum.getWidth() / (float) (heatMapWrappers.size() - 1);

		for (int i = 0; i < heatMapWrappers.size(); i++) {
			HeatMapWrapper heatMapWrapper = heatMapWrappers.get(i);
			AHeatMapLayout layout = layouts.get(i);
			int numExperiments = heatMapWrapper.getSet().getStorageVA(
					StorageVAType.STORAGE).size();
			// TODO: Maybe get info in layout from heatmapwrapper
			layout.setTotalSpaceForAllHeatMapWrappers(spaceForHeatMapWrapperOverviews);
			layout.setNumExperiments(numExperiments);
			layout.setNumTotalExperiments(numTotalExperiments);

			layout.setLayoutParameters(heatMapWrapperPosX, heatMapWrapperPosY,
					viewFrustum.getHeight() - setBarHeight, heatMapWrapperWidth);
			layout.setHeatMapWrapper(heatMapWrapper);

			heatMapWrapperPosX += heatMapWrapperWidth + heatMapWrapperGapWidth;
		}

	}
}
