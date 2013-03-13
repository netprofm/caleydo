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
package org.caleydo.vis.rank.model;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.vis.rank.internal.event.FilterEvent;
import org.caleydo.vis.rank.internal.ui.MultiLineInputDialog;
import org.caleydo.vis.rank.internal.ui.TextRenderer;
import org.caleydo.vis.rank.model.mixin.IAnnotatedColumnMixin;
import org.caleydo.vis.rank.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.rank.model.mixin.ICompressColumnMixin;
import org.caleydo.vis.rank.model.mixin.IFilterColumnMixin;
import org.caleydo.vis.rank.model.mixin.IHideableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IMappedColumnMixin;
import org.caleydo.vis.rank.model.mixin.IRankableColumnMixin;
import org.caleydo.vis.rank.model.mixin.ISnapshotableColumnMixin;
import org.caleydo.vis.rank.ui.RenderStyle;
import org.caleydo.vis.rank.ui.detail.ScoreBarElement;
import org.caleydo.vis.rank.ui.detail.ScoreSummary;
import org.caleydo.vis.rank.ui.detail.ValueElement;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Iterables;
import com.jogamp.common.util.IntObjectHashMap;

/**
 * the stacked column
 *
 * @author Samuel Gratzl
 *
 */
public class StackedRankColumnModel extends AMultiRankColumnModel implements IHideableColumnMixin,
		IAnnotatedColumnMixin, ISnapshotableColumnMixin, ICompressColumnMixin, ICollapseableColumnMixin {
	public static final String PROP_ALIGNMENT = "alignment";

	private final PropertyChangeListener listener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			switch(evt.getPropertyName()) {
			case PROP_WIDTH:
				cacheMulti.clear();
				onWeightChanged((ARankColumnModel) evt.getSource(), (float) evt.getOldValue(),
						(float) evt.getNewValue());
				break;
			case IFilterColumnMixin.PROP_FILTER:
			case IMappedColumnMixin.PROP_MAPPING:
				cacheMulti.clear();
				propertySupport.firePropertyChange(evt);
				break;
			}
		}
	};

	/**
	 * which is the current aligned column index or -1 for all
	 */
	private int alignment = 0;
	private boolean isCompressed = false;
	private float compressedWidth = 100;

	private String annotation = "";
	private IntObjectHashMap cacheMulti = new IntObjectHashMap();

	public StackedRankColumnModel() {
		super(Color.GRAY, new Color(0.95f, .95f, .95f));
		setHeaderRenderer(new TextRenderer("SUM", this));
		width = RenderStyle.COLUMN_SPACE;
	}

	public StackedRankColumnModel(StackedRankColumnModel copy) {
		super(copy);
		this.alignment = copy.alignment;
		this.annotation = copy.annotation;
		this.isCompressed = copy.isCompressed;
		setHeaderRenderer(new TextRenderer("SUM", this));
		width = RenderStyle.COLUMN_SPACE;
		cloneInitChildren();
	}

	@Override
	public StackedRankColumnModel clone() {
		return new StackedRankColumnModel(this);
	}


	/**
	 * @return the annotation, see {@link #annotation}
	 */
	@Override
	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		propertySupport.firePropertyChange(PROP_ANNOTATION, this.annotation, this.annotation = annotation);
	}

	@Override
	public void editAnnotation(final GLElement summary) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				InputDialog d = new MultiLineInputDialog(null, "Edit Annotation of: " + getTooltip(),
						"Edit Annotation",
						annotation, null);
				if (d.open() == Window.OK) {
					String v = d.getValue().trim();
					if (v.length() == 0)
						v = null;
					EventPublisher.publishEvent(new FilterEvent(v).to(summary));
				}
			}
		});
	}

	@Override
	protected void init(ARankColumnModel model) {
		super.init(model);
		model.addPropertyChangeListener(PROP_WIDTH, listener);
		model.addPropertyChangeListener(IFilterColumnMixin.PROP_FILTER, listener);
		model.addPropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, listener);
		// addDirectWeight(model.getWeight());
		cacheMulti.clear();
		super.setWidth(width + model.getWidth() + RenderStyle.COLUMN_SPACE);
		model.setParentData(model.getWidth());
	}

	@Override
	protected void takeDown(ARankColumnModel model) {
		super.takeDown(model);
		model.removePropertyChangeListener(PROP_WIDTH, listener);
		model.removePropertyChangeListener(IFilterColumnMixin.PROP_FILTER, listener);
		model.removePropertyChangeListener(IMappedColumnMixin.PROP_MAPPING, listener);
		// addDirectWeight(-model.getWeight());
		if (alignment > size() - 2) {
			setAlignment(alignment - 1);
		}
		super.setWidth(width - model.getWidth() - RenderStyle.COLUMN_SPACE);
		model.setParentData(null);
		cacheMulti.clear();
	}

	protected void onWeightChanged(ARankColumnModel child, float oldValue, float newValue) {
		child.setParentData(newValue);
		super.setWidth(width + (newValue - oldValue));
	}

	@Override
	public ARankColumnModel setWidth(float width) {
		if (isCompressed) {
			this.propertySupport.firePropertyChange(PROP_WIDTH, compressedWidth, this.compressedWidth = width);
			return this;
		}
		float shift = (this.size() + 1) * RenderStyle.COLUMN_SPACE;
		float factor = (width - shift) / (this.width - shift); // new / old
		for (ARankColumnModel col : this) {
			float wi = ((float) col.getParentData()) * factor;
			col.setParentData(wi);
			col.removePropertyChangeListener(PROP_WIDTH, listener);
			col.setWidth(wi);
			col.addPropertyChangeListener(PROP_WIDTH, listener);
		}
		return super.setWidth(width);
	}

	@Override
	public float getWidth() {
		if (isCollapsed())
			return COLLAPSED_WIDTH;
		if (isCompressed)
			return compressedWidth;
		return super.getWidth();
	}

	@Override
	public boolean canAdd(ARankColumnModel model) {
		return model instanceof IRankableColumnMixin && super.canAdd(model);
	}

	@Override
	public GLElement createSummary(boolean interactive) {
		return new MyElement(this, interactive);
	}

	@Override
	public ValueElement createValue() {
		return new ScoreBarElement(this);
	}

	@Override
	public float applyPrimitive(IRow row) {
		float s = 0;
		final int size = children.size();
		MultiFloat f = getSplittedValue(row);
		float[] ws = this.getDistributions();
		for (int i = 0; i < size; ++i) {
			s += f.values[i] * ws[i];
		}
		return s;
	}

	@Override
	public boolean isValueInferred(IRow row) {
		for (IRankableColumnMixin child : Iterables.filter(this, IRankableColumnMixin.class))
			if (child.isValueInferred(row))
				return true;
		return false;
	}

	@Override
	public MultiFloat getSplittedValue(IRow row) {
		if (cacheMulti.containsKey(row.getIndex()))
			return (MultiFloat) cacheMulti.get(row.getIndex());
		float[] s = new float[this.size()];
		for (int i = 0; i < s.length; ++i) {
			s[i] = ((IRankableColumnMixin) get(i)).applyPrimitive(row);
		}
		MultiFloat f = new MultiFloat(-1, s);
		cacheMulti.put(row.getIndex(), f);
		return f;
	}

	/**
	 * @return the alignment, see {@link #alignment}
	 */
	public int getAlignment() {
		return alignment;
	}

	/**
	 * @param alignment
	 *            setter, see {@link alignment}
	 */
	public void setAlignment(int alignment) {
		if (alignment > this.children.size())
			alignment = this.children.size();
		if (alignment == this.alignment)
			return;
		propertySupport.firePropertyChange(PROP_ALIGNMENT, this.alignment, this.alignment = alignment);
	}

	/**
	 * returns the distributions how much a individual column contributes to the overall scores, i.e. the normalized
	 * weights
	 *
	 * @return
	 */
	public float[] getDistributions() {
		float[] r = new float[this.size()];
		float base = width - RenderStyle.COLUMN_SPACE * (size() + 1);
		int i = 0;
		for (ARankColumnModel col : this) {
			r[i++] = (float) col.getParentData() / base;
		}
		return r;
	}

	public void setDistributions(float[] distributions) {
		assert this.size() == distributions.length;
		float sum = 0;
		for (float v : distributions)
			sum += v;
		float factor = (width - RenderStyle.COLUMN_SPACE * (size() + 1)) / sum;
		int i = 0;
		for (ARankColumnModel col : this) {
			float w = distributions[i++] * factor;
			col.setParentData(w);
		}
	}

	public float getChildWidth(int i) {
		return (float) get(i).getParentData();
	}

	public boolean isAlignAll() {
		return alignment < 0;
	}

	public void setAlignAll(boolean alignAll) {
		if (isAlignAll() == alignAll)
			return;
		this.setAlignment(-alignment - 1);
	}

	static class MyElement extends ScoreSummary {
		public MyElement(StackedRankColumnModel model, boolean interactive) {
			super(model, interactive);
		}

		@ListenTo(sendToMe = true)
		private void onSetAnnotation(FilterEvent event) {
			((StackedRankColumnModel) model).setAnnotation(Objects.toString(event.getFilter(), null));
		}
	}

	/**
	 * @return the isCompressed, see {@link #isCompressed}
	 */
	@Override
	public boolean isCompressed() {
		return isCompressed || isCollapsed();
	}

	@Override
	public void setCompressed(boolean compressed) {
		this.propertySupport.firePropertyChange(PROP_COMPRESSED, this.isCompressed, this.isCompressed = compressed);
	}

	@Override
	public boolean isFlatAdding(ACompositeRankColumnModel model) {
		return model instanceof StackedRankColumnModel;
	}
}
