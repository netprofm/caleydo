/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.filterpipeline;

import java.util.Set;

import javax.media.opengl.GL2;

import org.caleydo.core.data.filter.Filter;
import org.caleydo.core.data.filter.event.CombineFilterEvent;
import org.caleydo.core.data.filter.event.MoveFilterEvent;
import org.caleydo.core.data.filter.event.RemoveFilterEvent;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.picking.PickingManager;
import org.caleydo.core.view.opengl.picking.PickingType;
import org.caleydo.core.view.opengl.util.draganddrop.DragAndDropController;
import org.caleydo.core.view.opengl.util.draganddrop.IDraggable;
import org.caleydo.core.view.opengl.util.draganddrop.IDropArea;
import org.caleydo.core.view.opengl.util.text.CaleydoTextRenderer;
import org.caleydo.view.filterpipeline.representation.FilterRepresentation;
import org.caleydo.view.filterpipeline.representation.IRenderable;

/**
 * @brief Represents a filter in the GLFilterPipeline view
 *
 *        Also renders a context menu if needed
 *
 * @author Thomas Geymayer
 *
 */
public class FilterItem implements IRenderable, IDropArea {
	private int id;
	private int pickingId;
	private Filter filter;
	private FilterRepresentation representation = null;

	VirtualArray input = null;
	VirtualArray output = null;
	VirtualArray outputUncertainty = null;

	/**
	 * Constructor
	 *
	 * @param id
	 * @param filter
	 * @param pickingManager
	 * @param uniqueID
	 */
	public FilterItem(int id, Filter filter, PickingManager pickingManager, int iUniqueID) {
		this.id = id;
		pickingId = pickingManager.getPickingID(iUniqueID, PickingType.FILTERPIPE_FILTER, id);
		this.filter = filter;
	}

	public Filter getFilter() {
		return filter;
	}

	@Override
	public void render(GL2 gl, CaleydoTextRenderer textRenderer) {
		representation.render(gl, textRenderer);
	}

	/**
	 * Set the filter representation
	 *
	 * @param representation
	 */
	public void setRepresentation(FilterRepresentation representation) {
		representation.setFilter(this);
		this.representation = representation;
	}

	public FilterRepresentation getRepresentation() {
		return representation;
	}

	/**
	 *
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 *
	 * @return
	 */
	public String getLabel() {
		return filter.getLabel();
	}

	/**
	 * Set the items this filter should use as input
	 *
	 * @param input
	 */
	public void setInput(VirtualArray input) {
		this.input = input;
		output = this.input.clone();

		if (filter.getVADeltaUncertainty() != null)
			outputUncertainty = this.input.clone();

		output.setDelta(filter.getVADelta());

		if (filter.getVADeltaUncertainty() != null)
			outputUncertainty.setDelta(filter.getVADeltaUncertainty());
	}

	/**
	 * Get the items this filter received as input
	 *
	 * @return
	 */
	public VirtualArray getInput() {
		return input;
	}

	/**
	 * Get the items which passed this filter
	 *
	 * @return
	 */
	public VirtualArray getOutput() {
		return output;
	}

	/**
	 * Get the uncertain items which passed this filter
	 *
	 * @return
	 */
	public VirtualArray getUncertaintyOutput() {
		return outputUncertainty;
	}

	public int getSizeVADelta() {
		return filter.getVADelta().size();
	}

	public void showDetailsDialog() {
		filter.getFilterRep().create();
	}

	public void triggerRemove() {
		RemoveFilterEvent filterEvent = null;

		filterEvent = new RemoveFilterEvent();
		filterEvent.setFilter(filter);

		if (filterEvent != null) {
			filterEvent.setEventSpace(filter.getDataDomain().getDataDomainID());
			
			EventPublisher.trigger(filterEvent);
		}
	}

	public void triggerMove(int offset) {
		MoveFilterEvent filterEvent = null;

		filterEvent = new MoveFilterEvent();
		filterEvent.setFilter(filter);

		filterEvent.setEventSpace(filter.getDataDomain().getDataDomainID());
		filterEvent.setOffset(offset);
		
		EventPublisher.trigger(filterEvent);

	}

	public int getPickingID() {
		return pickingId;
	}

	private FilterRepresentation getFilterRepresentation(Set<IDraggable> draggables) {
		if (draggables.size() > 1) {
			System.err.println("getFilterRepresentation: More than one draggable?");
			return null;
		}

		IDraggable draggable = draggables.iterator().next();
		if (!(draggable instanceof FilterRepresentation))
			return null;

		return (FilterRepresentation) draggable;
	}

	@Override
	public void handleDragOver(GL2 gl, Set<IDraggable> draggables, float mouseCoordinateX, float mouseCoordinateY) {
		if (getFilterRepresentation(draggables) == representation)
			return;

		representation.handleDragOver(gl, draggables, mouseCoordinateX, mouseCoordinateY);
	}

	@Override
	public void handleDrop(GL2 gl, Set<IDraggable> draggables, float mouseCoordinateX, float mouseCoordinateY,
			DragAndDropController dragAndDropController) {
		if (getFilterRepresentation(draggables) == representation)
			return;

		representation.handleDrop(gl, draggables, mouseCoordinateX, mouseCoordinateY, dragAndDropController);

		CombineFilterEvent filterEvent = null;

		filterEvent = new CombineFilterEvent();
		filterEvent.setFilter(filter);
		filterEvent.addCombineFilter(getFilterRepresentation(draggables).getFilter().filter);

		if (filterEvent != null) {
			filterEvent.setEventSpace(filter.getDataDomain().getDataDomainID());
			
			EventPublisher.trigger(filterEvent);
		}
	}

	public void handleIconMouseOver(int externalID) {
		representation.handleIconMouseOver(externalID);
	}

	public void handleClearMouseOver() {
		representation.handleClearMouseOver();
	}

	@Override
	public void handleDropAreaReplaced() {
		// TODO Auto-generated method stub

	}
}
