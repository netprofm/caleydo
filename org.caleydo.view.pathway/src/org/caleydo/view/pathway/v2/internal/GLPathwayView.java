/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.pathway.v2.internal;

import java.util.List;

import org.caleydo.core.data.datadomain.DataSupportDefinitions;
import org.caleydo.core.data.datadomain.IDataSupportDefinition;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.logging.Logger;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.GLElementDecorator;
import org.caleydo.core.view.opengl.layout2.view.AMultiTablePerspectiveElementView;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.pathway.v2.internal.serial.SerializedPathwayView;
import org.caleydo.view.pathway.v2.ui.AverageColorMappingAugmentation;
import org.caleydo.view.pathway.v2.ui.PathwayElement;
import org.caleydo.view.pathway.v2.ui.PathwayTextureRenderer;

/**
 *
 * @author Christian
 *
 */
public class GLPathwayView extends AMultiTablePerspectiveElementView {
	public static final String VIEW_TYPE = "org.caleydo.view.pathway.v2";
	public static final String VIEW_NAME = "Pathway";

	public static final Logger log = Logger.create(GLPathwayView.class);

	private PathwayElement pathwayElement = new PathwayElement();

	private String eventSpace = EventPublisher.INSTANCE.createUniqueEventSpace();

	public GLPathwayView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);
		pathwayElement.setPathwayRepresentation(new PathwayTextureRenderer(PathwayManager.get().getPathwayByTitle(
				"Glioma", EPathwayDatabaseType.KEGG)));
		AverageColorMappingAugmentation colorMappingAugmentation = new AverageColorMappingAugmentation(
				pathwayElement.getPathwayRepresentation());
		colorMappingAugmentation.setEventSpace(eventSpace);
		pathwayElement.addBackgroundAugmentation(colorMappingAugmentation);
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		return new SerializedPathwayView(this);
	}

	@Override
	public IDataSupportDefinition getDataSupportDefinition() {
		return DataSupportDefinitions.tableBased;
	}

	@Override
	protected void applyTablePerspectives(GLElementDecorator root, List<TablePerspective> all,
			List<TablePerspective> added, List<TablePerspective> removed) {
		root.setContent(pathwayElement);

	}

	/**
	 * @return the eventSpace, see {@link #eventSpace}
	 */
	public String getEventSpace() {
		return eventSpace;
	}

	/**
	 * @param eventSpace
	 *            setter, see {@link eventSpace}
	 */
	public void setEventSpace(String eventSpace) {
		this.eventSpace = eventSpace;
	}
}