/*******************************************************************************
 * Caleydo - visualization for molecular biology - http://caleydo.org
 *
 * Copyright(C) 2005, 2012 Graz University of Technology, Marc Streit, Alexander
 * Lex, Christian Partl, Johannes Kepler University Linz </p>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 *******************************************************************************/
package org.caleydo.view.tourguide.v3.model;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.caleydo.core.io.gui.dataimport.widget.ICallback;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.function.AFloatList;
import org.caleydo.core.util.function.IFloatList;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.view.tourguide.v3.data.IFloatDataProvider;
import org.caleydo.view.tourguide.v3.model.mixin.IFilterColumnMixin;
import org.caleydo.view.tourguide.v3.model.mixin.IMappedColumnMixin;
import org.caleydo.view.tourguide.v3.model.mixin.IRankableColumnMixin;
import org.caleydo.view.tourguide.v3.ui.GLPropertyChangeListeners;
import org.caleydo.view.tourguide.v3.ui.PiecewiseLinearMappingUI;
import org.caleydo.view.tourguide.v3.ui.RenderUtils;
import org.caleydo.view.tourguide.v3.ui.detail.ScoreBarRenderer;
import org.eclipse.swt.SWT;

/**
 * @author Samuel Gratzl
 *
 */
public class FloatRankColumnModel extends ABasicRankColumnModel implements IFilterColumnMixin, IMappedColumnMixin,
		IRankableColumnMixin {
	private float selectionMin = 0;
	private float selectionMax = 1;
	private final BitSet mask = new BitSet();
	private final BitSet maskInvalid = new BitSet();

	private SimpleHistogram cacheHist = null;
	private boolean dirtyMinMax = true;
	private final PiecewiseLinearMapping mapping;

	private final IFloatDataProvider data;
	private final PropertyChangeListener listerner = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch (evt.getPropertyName()) {
			case RankTableModel.PROP_DATA:
				@SuppressWarnings("unchecked")
				Collection<IRow> news = (Collection<IRow>) evt.getNewValue();
				data.prepareFor(news);
				maskInvalid.set(getTable().getDataSize() - news.size(), getTable().getDataSize());
				break;
			case RankTableModel.PROP_INVALID:
				if (!mapping.hasDefinedMappingBounds())
					maskInvalid.set(0, getTable().getDataSize());
				cacheHist = null;
				dirtyMinMax = true;
				break;
			}
		}
	};
	private final ICallback<PiecewiseLinearMapping> callback = new ICallback<PiecewiseLinearMapping>() {
		@Override
		public void on(PiecewiseLinearMapping data) {
			cacheHist = null;
			maskInvalid.set(0, getTable().getDataSize());
			propertySupport.firePropertyChange(PROP_MAPPING, null, data);
		}
	};

	private IGLRenderer valueRenderer = new ScoreBarRenderer(this);

	public FloatRankColumnModel(IFloatDataProvider data, IGLRenderer header, Color color, Color bgColor,
			PiecewiseLinearMapping mapping) {
		super(color, bgColor);
		this.data = data;
		this.mapping = mapping;

		setHeaderRenderer(header);

	}

	public void addSelection(boolean isMin, float delta) {
		if (delta == 0)
			return;
		Pair<Float, Float> bak = Pair.make(selectionMin, selectionMax);
		if (isMin) {
			this.selectionMin += delta;
		} else {
			this.selectionMax += delta;
		}
		propertySupport.firePropertyChange(PROP_FILTER, bak, Pair.make(selectionMin, selectionMax));
		maskInvalid.set(0, getTable().getDataSize());
	}

	@Override
	protected void init(RankTableModel table) {
		table.addPropertyChangeListener(RankTableModel.PROP_DATA, listerner);
		table.addPropertyChangeListener(RankTableModel.PROP_INVALID, listerner);
		this.data.prepareFor(table.getData());
		super.init(table);
	}

	@Override
	protected void takeDown(RankTableModel table) {
		table.removePropertyChangeListener(RankTableModel.PROP_DATA, listerner);
		table.removePropertyChangeListener(RankTableModel.PROP_INVALID, listerner);
		super.takeDown(table);
	}

	@Override
	public GLElement createSummary(boolean interactive) {
		return new FloatSummary(interactive);
	}

	@Override
	public GLElement createValue() {
		return new GLElement(valueRenderer);
	}

	@Override
	public void editMapping(GLElement summary) {
		PiecewiseLinearMappingUI m = new PiecewiseLinearMappingUI(mapping, asData(), getColor(), getBgColor(), callback);
		m.setzDelta(0.5f);
		FloatSummary s = (FloatSummary) summary;
		Vec2f location = s.getAbsoluteLocation();
		Vec2f size = s.getSize();
		s.getContext().getPopupLayer().show(m, new Vec4f(location.x(), location.y() + size.y(), 260, 260));
	}

	private IFloatList asData() {
		return new AFloatList() {
			@Override
			public float getPrimitive(int index) {
				return getValue(getTable().get(index));
			}

			@Override
			public int size() {
				return getTable().size();
			}
		};
	}


	@Override
	public boolean isFiltered() {
		return selectionMin > 0 || selectionMax < 1;
	}

	protected float map(float value) {
		checkMapping();
		return mapping.apply(value);
	}

	private void checkMapping() {
		if (dirtyMinMax && mapping.isMappingDefault() && !mapping.hasDefinedMappingBounds()) {
			float[] minmax = asData().computeStats();
			mapping.setAct(minmax[0], minmax[1]);
			dirtyMinMax = false;
		}
	}

	@Override
	public float getValue(IRow row) {
		return map(data.applyPrimitive(row));
	}

	@Override
	public SimpleHistogram getHist(int bins) {
		if (cacheHist != null)
			return cacheHist;
		SimpleHistogram hist = new SimpleHistogram(bins);
		for (IRow row : getTable()) {
			hist.add(getValue(row));
		}
		cacheHist = hist;
		return hist;
	}

	@Override
	public void filter(List<IRow> data, BitSet mask) {
		if (selectionMin <= 0 && selectionMax >= 1)
			return;
		updateMask(data, mask);
		mask.and(this.mask);
	}

	private void updateMask(List<IRow> data, BitSet mask) {
		if (maskInvalid.isEmpty())
			return;

		for (int i = mask.nextSetBit(0); i >= 0; i = mask.nextSetBit(i + 1)) {
			if (maskInvalid.get(i)) {
				maskInvalid.clear(i);
				float v = getValue(data.get(i));
				this.mask.set(i++, (!Float.isNaN(v) && v >= selectionMin && v <= selectionMax));
			}
		}
	}

	class FloatSummary extends PickableGLElement {
		private int cursorMinPickingID = -1, cursorMaxPickingID = -1;
		private boolean cursorMinHovered = false, cursorMaxHovered = true;

		private final PropertyChangeListener repaintOnEvent = GLPropertyChangeListeners.repaintOnEvent(this);
		private final PropertyChangeListener selectRowListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				onSelectRow((IRow) evt.getNewValue());
			}
		};
		private IRow selectedRow = null;
		private final boolean interactive;

		/**
		 * @param interactive
		 */
		public FloatSummary(boolean interactive) {
			this.interactive = interactive;
			if (interactive) {
				setzDelta(.2f);
			} else {
				setVisibility(EVisibility.VISIBLE); // disable picking
			}
		}

		public IGLElementContext getContext() {
			return context;
		}

		@Override
		protected void init(IGLElementContext context) {
			super.init(context);

			RankTableModel table = getTable();
			table.addPropertyChangeListener(RankTableModel.PROP_SELECTED_ROW, selectRowListener);
			this.selectedRow = table.getSelectedRow();

			addPropertyChangeListener(PROP_FILTER, repaintOnEvent);
			addPropertyChangeListener(PROP_MAPPING, repaintOnEvent);

			cursorMinPickingID = context.registerPickingListener(new IPickingListener() {
				@Override
				public void pick(Pick pick) {
					onCursorPick(true, pick);
				}
			});
			cursorMaxPickingID = context.registerPickingListener(new IPickingListener() {
				@Override
				public void pick(Pick pick) {
					onCursorPick(false, pick);
				}
			});
		}

		@Override
		protected void takeDown() {
			context.unregisterPickingListener(cursorMinPickingID);
			context.unregisterPickingListener(cursorMaxPickingID);
			RankTableModel table = getTable();
			table.removePropertyChangeListener(RankTableModel.PROP_SELECTED_ROW, selectRowListener);
			removePropertyChangeListener(PROP_FILTER, repaintOnEvent);
			removePropertyChangeListener(PROP_MAPPING, repaintOnEvent);
			super.takeDown();
		}

		@Override
		protected void onClicked(Pick pick) {
			if (pick.isAnyDragging())
				return;
			Vec2f p = toRelative(pick.getPickedPoint());
			float at = p.x() / this.getSize().x();
			float min = selectionMin;
			float max = selectionMax;
			if (at < min)
				addSelection(true, at - min);
			else if (at > max)
				addSelection(false, at - max);
			else if ((at - min) < (max - min) * .5f)
				addSelection(true, at - min);
			else
				addSelection(false, at - max);
			repaint();
		}

		protected void onCursorPick(boolean isMin, Pick pick) {
			switch (pick.getPickingMode()) {
			case CLICKED:
				pick.setDoDragging(true);
				break;
			case MOUSE_OVER:
				context.setCursor(SWT.CURSOR_HAND);
				if (isMin)
					cursorMinHovered = true;
				else
					cursorMaxHovered = true;
				repaintAll();
				break;
			case MOUSE_OUT:
				if (!pick.isDoDragging()) {
					if (isMin)
						cursorMinHovered = false;
					else
						cursorMaxHovered = false;
					repaintAll();
				}
				break;
			case DRAGGED:
				if (!pick.isDoDragging())
					return;
				int dx = pick.getDx();
				if (dx != 0) {
					float delta = dx / this.getSize().x();
					addSelection(isMin, delta);
					repaintAll();
				}
				break;
			case MOUSE_RELEASED:
				if (pick.isDoDragging()) {
					context.setCursor(-1);
					if (isMin)
						cursorMinHovered = false;
					else
						cursorMaxHovered = false;
					repaintAll();
				}
				break;
			default:
				break;
			}
		}

		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			// background
			g.color(bgColor).fillRect(0, 0, w, h);
			// hist
			int bins = Math.round(w);
			SimpleHistogram hist = getHist(bins);
			int selectedBin = selectedRow == null ? -1 : hist.getBinOf(getValue(selectedRow));
			RenderUtils.renderHist(g, hist, w, h, selectedBin, color, color.darker());
			// selection
			if (w > 20)
				renderSelection(g, selectionMin, selectionMax, w, h);
			checkMapping();
			DecimalFormat d = new DecimalFormat("#.##");
			float[] m = mapping.getMappedMin();
			g.drawText(d.format(m[0]), 1, h - 23, 15, 10);
			g.drawText(d.format(m[1]), 1, h - 12, 15, 10);
			m = mapping.getMappedMax();
			g.drawText(d.format(m[0]), w - 16, h - 23, 15, 10, VAlign.RIGHT);
			g.drawText(d.format(m[1]), w - 16, h - 12, 15, 10,
					VAlign.RIGHT);
		}

		@Override
		protected void renderPickImpl(GLGraphics g, float w, float h) {
			super.renderPickImpl(g, w, h);
			if (w <= 20 || !interactive)
				return;
			g.incZ().incZ();
			float from = selectionMin;
			if (from > 0) {
				g.pushName(cursorMinPickingID);
				if (cursorMinHovered)
					g.fillRect(from * w - 8, 0, 16, h);
				else
					g.fillRect(from * w, 0, 1, h);
				g.popName();
			}
			float to = selectionMax;
			if (to < 1) {
				g.pushName(cursorMaxPickingID);
				if (cursorMaxHovered)
					g.fillRect(to * w - 8, 0, 16, h);
				else
					g.fillRect(to * w, 0, 1, h);
				g.popName();
			}
			g.decZ().decZ();
		}

		private void renderSelection(GLGraphics g, float from, float to, float w, float h) {
			assert from < to;
			if (from > 0 && from < 1) {
				g.color(0, 0, 0, 0.25f).fillRect(0, 0, from * w, h);
				if (cursorMinHovered)
					g.color(Color.BLACK).fillRect(from * w - 2, 0, 4, h);
				else
					g.color(Color.BLACK).fillRect(from * w, 0, 1, h);
			}
			if (to > 0 && to < 1) {
				g.color(0, 0, 0, 0.25f).fillRect(to * w, 0, (1 - to) * w, h);
				if (cursorMaxHovered)
					g.color(Color.BLACK).fillRect(to * w - 2, 0, 4, h);
				else
					g.color(Color.BLACK).fillRect(to * w, 0, 1, h);
			}

		}

		protected void onSelectRow(IRow selectedRow) {
			if (this.selectedRow == selectedRow)
				return;
			this.selectedRow = selectedRow;
			repaint();
		}
	}


}