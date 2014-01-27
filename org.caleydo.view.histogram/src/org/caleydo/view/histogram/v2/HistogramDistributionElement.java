/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.histogram.v2;

import gleem.linalg.Vec2f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.manage.GLLocation;
import org.caleydo.view.histogram.HistogramRenderStyle;
import org.caleydo.view.histogram.v2.internal.IDistributionData;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Rendering the distribution of a categorical element in various forms
 *
 * @author Samuel Gratzl
 */
public class HistogramDistributionElement extends ADistributionElement {

	private EDimension dim;

	/**
	 * @param data
	 * @param dim
	 */
	public HistogramDistributionElement(IDistributionData data, EDimension dim) {
		super(data);
		this.dim = dim;
	}

	/**
	 * @return the dimension, see {@link #dim}
	 */
	public EDimension getDimension() {
		return dim;
	}

	@Override
	protected void render(GLGraphics g, float w, float h) {
		if (dim.isDimension())
			renderHistImpl(g, w, h);
		else {
			g.save();
			g.gl.glRotatef(90, 0, 0, 1);
			g.move(0, -w);
			renderHistImpl(g, h, w);
			g.restore();
		}
	}

	private void renderHistImpl(GLGraphics g, float w, float h) {
		h -= HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW * 2;
		w -= HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW * 2;

		final Histogram hist = data.getHist();
		final float factor = h / hist.getLargestValue();
		final float delta = w / hist.size();

		final float lineWidth = Math.min(delta - 1, 25);
		final float lineWidthHalf = lineWidth * 0.5f;
		float x = delta / 2;

		g.save().move(HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW,
				HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW + h - 1);
		g.color(Color.DARK_GRAY).drawLine(0, 0, w, 0);

		for (int i = 0; i < hist.size(); ++i) {
			g.color(toHighlight(data.getBinColor(i), i));
			float v = -hist.get(i) * factor;

			if (v <= -1) {
				g.pushName(bucketPickingIds.get(i));
				g.fillRect(x - lineWidthHalf, 0, lineWidth, v);
				g.popName();
			}
			x += delta;
		}

		if (!g.isPickingPass()) {
			if (RenderStyle.COLOR_BORDER != null) {
				g.color(RenderStyle.COLOR_BORDER);
				x = delta / 2;
				for (int i = 0; i < hist.size(); ++i) {
					float v = -hist.get(i) * factor;
					if (v <= -1) {
						g.drawRect(x - lineWidthHalf, 0, lineWidth, v);
					}
					x += delta;
				}
			}
			g.lineWidth(2);
			for (SelectionType selectionType : SELECTIONTYPES) {
				Set<Integer> elements = data.getElements(selectionType);
				if (elements.isEmpty())
					continue;
				g.color(toHighlightColor(selectionType));
				x = delta / 2;
				for (int i = 0; i < hist.size(); ++i) {
					Set<Integer> ids = hist.getIDsForBucket(i);
					float v = -Sets.intersection(elements, ids).size() * factor;
					if (v <= -1) {
						g.fillRect(x - lineWidthHalf, 0, lineWidth, v);
					}
					x += delta;
				}
			}
		}

		g.restore();
	}

	@Override
	public final Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}

	@Override
	public GLLocation apply(int dataIndex) {
		int bin = data.getBinOf(dataIndex);
		float w = dim.select(getSize());
		w -= HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW * 2;
		final Histogram hist = data.getHist();
		final float delta = w / hist.size();

		final float lineWidth = Math.min(delta - 1, 25);
		final float lineWidthHalf = lineWidth * 0.5f;
		float x = delta / 2 + HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW;

		x += delta * bin - lineWidthHalf;
		return new GLLocation(x, lineWidth);
	}

	@Override
	public Set<Integer> unapply(GLLocation location) {
		Set<Integer> r = new HashSet<>();
		final Histogram hist = data.getHist();
		for (Integer bin : toBins(location)) {
			r.addAll(hist.getIDsForBucket(bin));
		}
		return ImmutableSet.copyOf(r);
	}

	/**
	 * @param location
	 * @return
	 */
	private Iterable<Integer> toBins(GLLocation location) {
		float w = dim.select(getSize());
		w -= HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW * 2;
		final Histogram hist = data.getHist();
		final float delta = w / hist.size();
		final float lineWidth = Math.min(delta - 1, 25);
		final float lineWidthHalf = lineWidth * 0.5f;
		float x = delta / 2 + HistogramRenderStyle.SIDE_SPACING_DETAIL_LOW;
		List<Integer> r = new ArrayList<>(hist.size());
		for (int i = 0; i < hist.size(); ++i) {
			float xi = x + delta * i - lineWidthHalf;
			float wi = lineWidth;
			if ((xi + wi) < location.getOffset())
				continue;
			if (xi > location.getOffset2())
				break;
			r.add(i);
		}
		return r;
	}
}
