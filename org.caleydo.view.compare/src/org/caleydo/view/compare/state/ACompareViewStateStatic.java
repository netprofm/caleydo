package org.caleydo.view.compare.state;

import java.util.ArrayList;

import org.caleydo.core.data.collection.ISet;
import org.caleydo.core.data.mapping.EIDCategory;
import org.caleydo.core.data.selection.ContentGroupList;
import org.caleydo.core.data.selection.ContentSelectionManager;
import org.caleydo.core.data.selection.ContentVAType;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.data.selection.SelectionTypeEvent;
import org.caleydo.core.data.selection.delta.ISelectionDelta;
import org.caleydo.core.data.selection.delta.SelectionDelta;
import org.caleydo.core.manager.IUseCase;
import org.caleydo.core.manager.event.view.storagebased.SelectionUpdateEvent;
import org.caleydo.core.manager.picking.EPickingMode;
import org.caleydo.core.manager.picking.EPickingType;
import org.caleydo.core.manager.picking.Pick;
import org.caleydo.core.manager.picking.PickingManager;
import org.caleydo.core.manager.usecase.EDataDomain;
import org.caleydo.core.view.opengl.mouse.GLMouseListener;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.texture.TextureManager;
import org.caleydo.view.compare.GLCompare;
import org.caleydo.view.compare.HeatMapWrapper;
import org.caleydo.view.compare.SetBar;
import org.caleydo.view.compare.rendercommand.RenderCommandFactory;

import com.sun.opengl.util.j2d.TextRenderer;

public abstract class ACompareViewStateStatic extends ACompareViewState {

	public ACompareViewStateStatic(GLCompare view, int viewID, TextRenderer textRenderer,
			TextureManager textureManager, PickingManager pickingManager,
			GLMouseListener glMouseListener, SetBar setBar,
			RenderCommandFactory renderCommandFactory, EDataDomain dataDomain,
			IUseCase useCase, DragAndDropController dragAndDropController,

			CompareViewStateController compareViewStateController) {
		super(view, viewID, textRenderer, textureManager, pickingManager,
				glMouseListener, setBar, renderCommandFactory, dataDomain, useCase,
				dragAndDropController, compareViewStateController);
	}
	
	public void setSetsToCompare(ArrayList<ISet> setsToCompare) {
		setBar.setSets(setsToCompare);
	}

	public void handlePickingEvents(EPickingType ePickingType, EPickingMode pickingMode,
			int iExternalID, Pick pick, boolean isControlPressed) {
		SelectionType selectionType = null;

		switch (ePickingType) {

		case POLYLINE_SELECTION:

			switch (pickingMode) {
			case CLICKED:
				selectionType = SelectionType.SELECTION;
				break;
			case MOUSE_OVER:
				selectionType = SelectionType.MOUSE_OVER;
				break;
			case RIGHT_CLICKED:
				selectionType = SelectionType.SELECTION;

				// ContentContextMenuItemContainer
				// contentContextMenuItemContainer = new
				// ContentContextMenuItemContainer();
				// contentContextMenuItemContainer.setID(
				// EIDType.EXPRESSION_INDEX, iExternalID);
				// contextMenu
				// .addItemContanier(contentContextMenuItemContainer);
				break;

			default:
				return;

			}

			// FIXME: Check if is ok to share the content selection manager
			// of the use case
			ContentSelectionManager contentSelectionManager = useCase
					.getContentSelectionManager();
			if (contentSelectionManager.checkStatus(selectionType, iExternalID)) {
				break;
			}

			contentSelectionManager.clearSelection(selectionType);
			contentSelectionManager.addToType(selectionType, iExternalID);

			ISelectionDelta selectionDelta = contentSelectionManager.getDelta();
			SelectionUpdateEvent event = new SelectionUpdateEvent();
			event.setSender(this);
			event.setSelectionDelta((SelectionDelta) selectionDelta);
			// event.setInfo(getShortInfoLocal());
			eventPublisher.triggerEvent(event);

			view.setDisplayListDirty();
			break;

		case COMPARE_SET_BAR_ITEM_SELECTION:
			setBar.handleSetBarItemSelection(iExternalID, pickingMode, pick);
			break;

		case COMPARE_SELECTION_WINDOW_SELECTION:
		case COMPARE_SELECTION_WINDOW_ARROW_LEFT_SELECTION:
		case COMPARE_SELECTION_WINDOW_ARROW_RIGHT_SELECTION:
			setBar.handleSetBarSelectionWindowSelection(iExternalID, ePickingType,
					pickingMode, pick);
			break;
		}

		handleStateSpecificPickingEvents(ePickingType, pickingMode, iExternalID, pick,
				isControlPressed);
	}

	public int getNumSetsInFocus() {
		return numSetsInFocus;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void handleContentGroupListUpdate(int setID, ContentGroupList contentGroupList) {
		for (HeatMapWrapper heatMapWrapper : heatMapWrappers) {
			if (heatMapWrapper.getSet().getID() == setID) {
				heatMapWrapper.handleContentGroupListUpdate(contentGroupList);
				view.setDisplayListDirty();
				return;
			}
		}
	}

	public void handleReplaceContentVA(int setID, EIDCategory idCategory,
			ContentVAType vaType) {

		// FIXME: we should not destroy all the heat map wrappers when a
		// contentVA is handled
		setSetsInFocus(setBar.getSetsInFocus());
	}

	public abstract void handleStateSpecificPickingEvents(EPickingType ePickingType,
			EPickingMode pickingMode, int iExternalID, Pick pick, boolean isControlPressed);
}
