/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.heatmap.v2.internal;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.selection.SelectionManager;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.view.heatmap.heatmap.GLHeatMap;
import org.caleydo.view.heatmap.v2.EShowLabels;
import org.caleydo.view.heatmap.v2.ISpacingStrategy;
import org.caleydo.view.heatmap.v2.ISpacingStrategy.ISpacingLayout;
import org.caleydo.view.heatmap.v2.SpacingStrategies;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
/**
 * utility class to render the selection of a heatmap as one ore more crosses
 *
 * @author Samuel Gratzl
 *
 */
public class DimensionRenderer {
	private final EDimension dim;
	private final IHeatMapDataProvider data;

	private EShowLabels label = EShowLabels.NONE;
	private ISpacingStrategy spacingStrategy = SpacingStrategies.UNIFORM;
	private ISpacingLayout spacing = null;
	protected IndexedId hoveredID = null;

	public DimensionRenderer(IHeatMapDataProvider data, EDimension dimension) {
		this.dim = dimension;
		this.data = data;
	}

	/**
	 * @return the spacingStrategy, see {@link #spacingStrategy}
	 */
	public ISpacingStrategy getSpacingStrategy() {
		return spacingStrategy;
	}

	/**
	 * @param spacingStrategy
	 *            setter, see {@link spacingStrategy}
	 */
	public void setSpacingStrategy(ISpacingStrategy spacingStrategy) {
		this.spacingStrategy = spacingStrategy;
	}

	/**
	 * @return the spacing, see {@link #spacing}
	 */
	public ISpacingLayout getSpacing() {
		return spacing;
	}

	/**
	 * @param labels
	 *            setter, see {@link labels}
	 */
	public void setLabel(EShowLabels labels) {
		this.label = labels;
	}

	public float minSize() {
		return spacingStrategy.minSize(size(), label.show());
	}

	public boolean isUniformSpacing() {
		return spacing.isUniform();
	}

	/**
	 * @return the labels, see {@link #label}
	 */
	public EShowLabels getLabel() {
		return label;
	}

	public int size() {
		return getData().size();
	}

	public void renderSelectionRects(GLGraphics g, SelectionType selectionType, float w, float h, boolean fill) {
		List<Integer> indices = prepareRender(selectionType);
		final boolean isDimension = dim.isDimension();

		renderSelectionRects(g, w, h, indices, selectionType, isDimension, fill);
	}

	public List<Vec2f> getNotSelectedRanges(SelectionType type, float w, float h) {
		float total = dim.select(w, h);
		List<Integer> indices = prepareRender(type);
		if (indices.isEmpty())
			return null;
		List<Vec2f> r = new ArrayList<>();
		int lastIndex = -1;
		float x = 0;
		for (int index : indices) {
			if (index != (lastIndex + 1)) {
				float to = spacing.getPosition(index);
				r.add(new Vec2f(x, to - x));
				x = to + spacing.getSize(index);
			}
			lastIndex = index;
		}
		if ((lastIndex + 1) < getData().size()) {
			x = spacing.getPosition(lastIndex) + spacing.getSize(lastIndex);
			r.add(new Vec2f(x, total - x));
		}
		return r;
	}

	private void renderSelectionRects(GLGraphics g, float w, float h, List<Integer> indices, SelectionType type,
			final boolean isDimension, boolean fill) {
		g.color(type.getColor());
		if (!fill)
			g.lineWidth(3);
		int lastIndex = -1;
		float x = 0;
		float wi = 0;
		for (int index : indices) {
			if (index != (lastIndex + 1)) // just the outsides
			{
				// flush previous
				if (isDimension)
					renderRect(fill, g, x, 0, wi, h);
				else
					renderRect(fill, g, 0, x, w, wi);
				x = spacing.getPosition(index);
				wi = 0;
			}
			wi += spacing.getSize(index);
			lastIndex = index;
		}
		if (wi > 0)
			if (isDimension)
				renderRect(fill, g, x, 0, wi, h);
			else
				renderRect(fill, g, 0, x, w, wi);
	}

	private static void renderRect(boolean fill, GLGraphics g, float x, float y, float w, float h) {
		if (fill)
			g.fillRect(x, y, w, h);
		else
			g.drawRect(x, y, w, h);
	}

	private List<Integer> prepareRender(SelectionType selectionType) {
		Set<Integer> selectedSet = getManager().getElements(selectionType);
		List<Integer> indices = toIndices(selectedSet);
		return indices;
	}

	private List<Integer> toIndices(Set<Integer> selectedSet) {
		SelectionManager m = getManager();
		List<Integer> indices = new ArrayList<>(selectedSet.size());
		for (Integer selectedColumn : selectedSet) {
			if (m.checkStatus(GLHeatMap.SELECTION_HIDDEN, selectedColumn))
				continue;
			int i = data.indexOf(dim, selectedColumn);
			if (i < 0)
				continue;
			indices.add(i);
		}
		Collections.sort(indices);
		return indices;
	}

	/**
	 * @return the data, see {@link #data}
	 */
	public List<Integer> getData() {
		return data.getData(dim);
	}

	/**
	 * @param x
	 */
	public void updateSpacing(float total) {
		spacing = spacingStrategy.apply(getData(), getManager(), total);
	}

	/**
	 * @return
	 */
	private SelectionManager getManager() {
		return data.getManager(dim);
	}

	/**
	 * @param index
	 */
	public GLLocation getLocation(int index, float textWidth) {
		if (spacing == null)
			return null;
		float pos = spacing.getPosition(index);
		if (label == EShowLabels.LEFT)
			pos += textWidth;
		return new GLLocation(pos, spacing.getSize(index));
	}

	public Set<Integer> forLocation(GLLocation location, float textWidth) {
		if (spacing == null)
			return GLLocation.UNKNOWN_IDS;
		float offset = (float) location.getOffset();
		float offset2 = (float) location.getOffset2();
		if (label == EShowLabels.LEFT) {
			offset -= textWidth;
			offset2 -= textWidth;
		}
		int from = spacing.getIndex(offset);
		int to = spacing.getIndex(offset2);
		return ContiguousSet.create(Range.closed(from, to), DiscreteDomain.integers());
	}

	/**
	 * @param position
	 * @param textWidth
	 * @return
	 */
	public int getIndex(float position, int textWidth) {
		if (spacing == null)
			return -1;
		if (label == EShowLabels.LEFT)
			position -= textWidth;
		return spacing.getIndex(position);
	}

	/**
	 * @param selection
	 * @param b
	 * @param first
	 * @return
	 */
	public void select(SelectionType selection, boolean clearExisting, IndexedId id) {
		if (id != null && id.getId() != null)
			select(selection, clearExisting, false, id.getId());
		hoveredID = id;
	}

	public void clear(SelectionType selection) {
		select(selection, true, false);
		hoveredID = null;
	}

	private void select(SelectionType selectionType, boolean clearExisting, boolean deSelect,
			int... ids) {
		SelectionManager m = getManager();
		if (clearExisting)
			m.clearSelection(selectionType);

		if (ids.length > 0) {
			if (deSelect)
				m.removeFromType(selectionType, Ints.asList(ids));
			else
				m.addToType(selectionType, Ints.asList(ids));
		}
		data.fireSelectionChanged(m);
	}

	/**
	 * @return the hoveredID, see {@link #hoveredID}
	 */
	public IndexedId getHoveredID() {
		return hoveredID;
	}

	private Integer get(int index) {
		List<Integer> data = getData();
		if (index < 0 || index >= data.size())
			return null;
		return data.get(index);
	}

	public IndexedId getIndexedId(float v, int textWidth) {
		int index = getIndex(v, textWidth);
		Integer id = get(index);
		return new IndexedId(index, id);
	}

	private int[] toRange(IndexedId from, IndexedId to) {
		List<Integer> data = getData();
		int fIndex = from.getIndex();
		int tIndex = to.getIndex();
		final int length = Math.abs(fIndex - tIndex);
		int[] d = new int[length];
		int delta = fIndex < tIndex ? +1 : -1;
		int index = fIndex;
		for (int i = 0; i < length; ++i) {
			d[i] = data.get(index);
			index += delta;
		}
		return d;
	}

	/**
	 * @param first
	 */
	public void drag(IndexedId id) {
		int[] range = toRange(hoveredID, id);
		boolean selected = getManager().checkStatus(SelectionType.SELECTION, id.getId());
		select(SelectionType.SELECTION, false, selected, range);
		hoveredID = id;
	}
}
