/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dvi.node;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.ITablePerspectiveBasedView;
import org.caleydo.core.view.IView;
import org.caleydo.core.view.listener.AddTablePerspectivesEvent;
import org.caleydo.core.view.opengl.layout.Column;
import org.caleydo.core.view.opengl.layout.ElementLayout;
import org.caleydo.core.view.opengl.layout.Row;
import org.caleydo.core.view.opengl.layout.util.BorderedAreaRenderer;
import org.caleydo.core.view.opengl.layout.util.TextureRenderer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;
import org.caleydo.core.view.opengl.util.draganddrop.IDropArea;
import org.caleydo.datadomain.pathway.data.PathwayTablePerspective;
import org.caleydo.view.dvi.GLDataViewIntegrator;
import org.caleydo.view.dvi.ViewNodeBackGroundRenderer;
import org.caleydo.view.dvi.contextmenu.OpenViewItem;
import org.caleydo.view.dvi.contextmenu.RenameLabelHolderItem;
import org.caleydo.view.dvi.layout.AGraphLayout;
import org.caleydo.view.dvi.tableperspective.AMultiTablePerspectiveRenderer;
import org.caleydo.view.dvi.tableperspective.TablePerspectiveRenderer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ViewNode extends ADefaultTemplateNode implements IDropArea {

	// private TablePerspectiveListRenderer overviewTablePerspectiveRenderer;
	protected IView representedView;
	protected Set<IDataDomain> dataDomains = new HashSet<IDataDomain>();
	protected String iconPath;

	public ViewNode(AGraphLayout graphLayout, GLDataViewIntegrator view, DragAndDropController dragAndDropController,
			Integer id, IView representedView) {
		super(graphLayout, view, dragAndDropController, id);

		this.representedView = representedView;
		dataDomains = new HashSet<>(representedView.getDataDomains()); // local copy

		setRepresentedViewInfo();
		// setupLayout();
	}

	@Override
	protected void registerPickingListeners() {

		super.registerPickingListeners();

		view.addIDPickingListener(new APickingListener() {

			@Override
			public void rightClicked(Pick pick) {
				view.getContextMenuCreator().addContextMenuItem(new OpenViewItem(representedView));

				view.getContextMenuCreator().addContextMenuItem(new RenameLabelHolderItem(representedView));
			}

			@Override
			public void doubleClicked(Pick pick) {
				view.openView(representedView);
			}

		}, DATA_GRAPH_NODE_PICKING_TYPE, id);

		view.addIDPickingListener(new APickingListener() {

			@Override
			public void dragged(Pick pick) {

				DragAndDropController dragAndDropController = ViewNode.this.dragAndDropController;
				if (dragAndDropController.isDragging()
						&& dragAndDropController.getDraggingMode().equals("DimensionGroupDrag")) {
					dragAndDropController.setDropArea(ViewNode.this);
				}

			}
		}, DATA_GRAPH_NODE_PENETRATING_PICKING_TYPE, id);

	}

	private void setRepresentedViewInfo() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint("org.eclipse.ui.views");
		IExtension[] extensions = point.getExtensions();
		String viewID = representedView.getViewType();
		iconPath = null;
		boolean viewNameObtained = false;

		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getAttribute("id").equals(viewID)) {
					// element.getAttribute("name");
					iconPath = element.getAttribute("icon");
					viewNameObtained = true;
					break;

				}
			}
			if (viewNameObtained) {
				break;
			}
		}

		if (iconPath != null && iconPath.equals("")) {
			iconPath = null;
		}
		if (iconPath != null) {
			Bundle viewPlugin = FrameworkUtil.getBundle(representedView.getClass());

			URL iconURL = viewPlugin.getEntry(iconPath);
			try {
				iconPath = FileLocator.toFileURL(iconURL).getPath();
			} catch (IOException e) {
				new IllegalStateException("Cannot load view icon texture");
			}
		}
	}

	@Override
	protected ElementLayout setupLayout() {
		Row baseRow = createDefaultBaseRow(BorderedAreaRenderer.DEFAULT_COLOR, id);

		ElementLayout spacingLayoutX = createDefaultSpacingX();

		baseColumn = new Column();

		baseRow.append(spacingLayoutX);
		baseRow.append(baseColumn);
		baseRow.append(spacingLayoutX);

		Row titleRow = new Row("titleRow");
		titleRow.setYDynamic(true);

		if (iconPath != null) {
			ElementLayout iconLayout = new ElementLayout("icon");
			iconLayout.setPixelSizeX(CAPTION_HEIGHT_PIXELS);
			iconLayout.setPixelSizeY(CAPTION_HEIGHT_PIXELS);
			iconLayout.setRenderer(new TextureRenderer(iconPath, view.getTextureManager()));
			titleRow.append(iconLayout);
			titleRow.append(spacingLayoutX);
		}

		ElementLayout captionLayout = createDefaultCaptionLayout(id);

		titleRow.append(captionLayout);

		ElementLayout lineSeparatorLayout = createDefaultLineSeparatorLayout();

		Row bodyRow = new Row("bodyRow");
		bodyRow.addBackgroundRenderer(new ViewNodeBackGroundRenderer(new float[] { 1, 1, 1, 1 }, iconPath, view
				.getTextureManager()));

		bodyColumn = new Column("bodyColumn");

		ElementLayout bodySpacingLayoutY = new ElementLayout("compGroupOverview");

		bodySpacingLayoutY.setRatioSizeY(1);

		ElementLayout spacingLayoutY = createDefaultSpacingY();

		bodyColumn.append(bodySpacingLayoutY);
		// bodyColumn.append(spacingLayoutY);

		bodyRow.append(bodyColumn);

		baseColumn.append(spacingLayoutY);
		baseColumn.append(bodyRow);
		baseColumn.append(spacingLayoutY);
		baseColumn.append(lineSeparatorLayout);
		baseColumn.append(titleRow);
		baseColumn.append(spacingLayoutY);

		setUpsideDown(isUpsideDown);

		return baseRow;
	}

	@Override
	public List<TablePerspective> getTablePerspectives() {

		if (representedView instanceof ITablePerspectiveBasedView) {
			return new ArrayList<TablePerspective>(
					((ITablePerspectiveBasedView) representedView).getTablePerspectives());

		}

		return new ArrayList<TablePerspective>();
	}

	public Set<IDataDomain> getDataDomains() {
		return dataDomains;
	}

	public IView getRepresentedView() {
		return representedView;
	}

	@Override
	public void update() {

		dataDomains = new HashSet<>(representedView.getDataDomains());
		recalculateNodeSize();
	}

	@Override
	protected AMultiTablePerspectiveRenderer getTablePerspectiveRenderer() {
		return null;
	}

	@Override
	public void destroy() {
		super.destroy();
		view.removeAllIDPickingListeners(DATA_GRAPH_NODE_PICKING_TYPE, id);
		view.removeAllIDPickingListeners(DATA_GRAPH_NODE_PENETRATING_PICKING_TYPE, id);
	}

	@Override
	public boolean showsTablePerspectives() {
		return false;
	}

	@Override
	protected int getMinTitleBarWidthPixels() {
		float textWidth = view.getTextRenderer().getRequiredTextWidthWithMax(representedView.getLabel(),
				pixelGLConverter.getGLHeightForPixelHeight(CAPTION_HEIGHT_PIXELS), MIN_TITLE_BAR_WIDTH_PIXELS);

		return pixelGLConverter.getPixelWidthForGLWidth(textWidth) + CAPTION_HEIGHT_PIXELS + SPACING_PIXELS;
	}

	@Override
	public String getLabel() {
		return representedView.getLabel();
	}

	@Override
	public String getProviderName() {
		return "View Node";
	}

	@Override
	public void handleDrop(GL2 gl, Set<IDraggable> draggables, float mouseCoordinateX, float mouseCoordinateY,
			DragAndDropController dragAndDropController) {
		ArrayList<TablePerspective> tablePerspectives = new ArrayList<TablePerspective>();
		for (IDraggable draggable : draggables) {
			if (draggable instanceof TablePerspectiveRenderer) {
				TablePerspectiveRenderer tablePerspectiveRenderer = (TablePerspectiveRenderer) draggable;
				if (tablePerspectiveRenderer.hasTablePerspective()) {
					tablePerspectives.add(tablePerspectiveRenderer.createOrGetTablePerspective());
				} else {

				}
			}
		}

		if (!tablePerspectives.isEmpty()) {
			// FIXME: this needs to be looked at again
			// System.out.println("Drop");
			TablePerspective tablePerspective = tablePerspectives.get(0);
			AddTablePerspectivesEvent event = new AddTablePerspectivesEvent(tablePerspective);
			event.to(representedView).from(this);
			
			EventPublisher.trigger(event);

			if (tablePerspective instanceof PathwayTablePerspective) {
				dataDomains.add(((PathwayTablePerspective) tablePerspective).getPathwayDataDomain());
			} else {
				dataDomains.add(tablePerspective.getDataDomain());
			}
			view.updateGraphEdgesOfViewNode(this);
			graphLayout.fitNodesToDrawingArea(view.calculateGraphDrawingArea());
			view.setDisplayListDirty();
		}

		// dragAndDropController.clearDraggables();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.util.draganddrop.IDropArea#handleDragOver (javax.media.opengl.GL2,
	 * java.util.Set, float, float)
	 */
	@Override
	public void handleDragOver(GL2 gl, Set<IDraggable> draggables, float mouseCoordinateX, float mouseCoordinateY) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.caleydo.core.view.opengl.util.draganddrop.IDropArea# handleDropAreaReplaced()
	 */
	@Override
	public void handleDropAreaReplaced() {
		// TODO Auto-generated method stub

	}

}
