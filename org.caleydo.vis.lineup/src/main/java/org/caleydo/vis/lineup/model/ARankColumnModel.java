/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.vis.lineup.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.vis.lineup.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.lineup.model.mixin.IRankColumnModel;
import org.caleydo.vis.lineup.ui.detail.ValueElement;

/**
 * @author Samuel Gratzl
 *
 */
public abstract class ARankColumnModel implements IRankColumnModel {
	public static final String PROP_WIDTH = "width";

	protected final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

	protected float width = 100;

	private IGLRenderer header = GLRenderers.DUMMY;
	protected final Color color;
	protected final Color bgColor;
	private boolean collapsed = false;

	protected IRankColumnParent parent;
	private Object parentData;

	public ARankColumnModel(Color color, Color bgColor) {
		this.color = color;
		this.bgColor = bgColor;
	}

	public ARankColumnModel(ARankColumnModel copy) {
		this.color = copy.color;
		this.bgColor = copy.bgColor;
		this.width = copy.width;
		this.parent = copy.parent;
		this.collapsed = copy.collapsed;
		this.header = copy.header;
		this.width = copy.width;
	}

	@Override
	public abstract ARankColumnModel clone();

	protected final void setHeaderRenderer(IGLRenderer header) {
		this.header = header;
	}

	protected void init(IRankColumnParent parent) {
		this.parent = parent;
	}

	protected void takeDown() {
		this.parent = null;
	}

	/**
	 * @return the parent, see {@link #parent}
	 */
	@Override
	public IRankColumnParent getParent() {
		return parent;
	}

	/**
	 * @return the parentData, see {@link #parentData}
	 */
	public Object getParentData() {
		return parentData;
	}

	/**
	 * @param parentData
	 *            setter, see {@link parentData}
	 */
	public void setParentData(Object parentData) {
		this.parentData = parentData;
	}

	/**
	 * @param weight
	 *            setter, see {@link weight}
	 */
	public ARankColumnModel setWidth(float width) {
		propertySupport.firePropertyChange(PROP_WIDTH, this.width, this.width = width);
		return this;
	}

	public void setWidthImpl(float with) {
		this.width = with;
	}

	public float getWidth() {
		if (isCollapsed())
			return ICollapseableColumnMixin.COLLAPSED_WIDTH;
		return width;
	}

	public abstract String getValue(IRow row);

	public abstract GLElement createSummary(boolean interactive);

	public abstract ValueElement createValue();

	public final IGLRenderer getHeaderRenderer() {
		return header;
	}

	@Override
	public String getLabel() {
		return header.toString();
	}


	public final void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(propertyName, listener);
	}

	public final void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(propertyName, listener);
	}

	/**
	 * @return the bgColor, see {@link #bgColor}
	 */
	public Color getBgColor() {
		return bgColor;
	}

	/**
	 * @return the color, see {@link #color}
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * @return the table, see {@link #table}
	 */
	@Override
	public RankTableModel getTable() {
		return parent == null ? null : parent.getTable();
	}

	public final boolean isCombineAble(ARankColumnModel with, boolean clone, int combineMode) {
		return getTable().isCombineAble(this, with, clone, combineMode);
	}

	public final boolean combine(ARankColumnModel with, boolean clone, int combineMode) {
		if (!isCombineAble(with, clone, combineMode))
			return false;
		return combine(this, with, clone, combineMode);
	}

	private static boolean combine(ARankColumnModel model, ARankColumnModel with, boolean clone, int combineMode) {
		final RankTableModel table = model.getTable();
		model.setCollapsed(false);
		with.setCollapsed(false);

		boolean isWithComposite = with instanceof ACompositeRankColumnModel;
		boolean isModelComposite = model instanceof ACompositeRankColumnModel;
		if (isModelComposite) {
			ACompositeRankColumnModel t = (ACompositeRankColumnModel) model;
			if (table.getConfig().canBeReusedForCombining(t, combineMode)) {
				addAll(with, clone, table, t);
			} else {
				createNewCombined(model, with, clone, combineMode, table);
			}
		} else {
			if (isWithComposite) {
				ACompositeRankColumnModel w = (ACompositeRankColumnModel) with;
				if (!clone && table.getConfig().canBeReusedForCombining(w, combineMode)) {
					w.getParent().remove(w);
					model.getParent().replace(model, w);
					w.add(0, model);
				} else
					createNewCombined(model, with, clone, combineMode, table);
			} else {
				createNewCombined(model, with, clone, combineMode, table);
			}
		}
		return true;
	}

	private static void createNewCombined(ARankColumnModel model, ARankColumnModel with, boolean clone,
			int combineMode, final RankTableModel table) {
		ACompositeRankColumnModel new_ = table.getConfig().createNewCombined(combineMode);
		model.getParent().replace(model, new_);
		addAll(model, false, table, new_);
		addAll(with, clone, table, new_);
		new_.setWidth(model.getWidth());
	}

	private static void addAll(ARankColumnModel with, boolean clone, final RankTableModel table,
			ACompositeRankColumnModel model) {
		boolean isWithComposite = with instanceof ACompositeRankColumnModel;

		if (isWithComposite && model.isFlatAdding((ACompositeRankColumnModel) with)) {
			ACompositeRankColumnModel w = (ACompositeRankColumnModel) with;
			Collection<ARankColumnModel> tmp = new ArrayList<>(w.getChildren());
			if (clone) {
				for (ARankColumnModel wi : tmp)
					model.add(wi.clone());
			} else {
				for (ARankColumnModel wi : tmp) {
					w.remove(wi);
					model.add(wi);
				}
			}
			if (with.getParent() != null)
				with.getParent().remove(with);
		} else if (clone)
			model.add(with.clone());
		else {
			if (with.getParent() != null)
				with.getParent().remove(with);
			model.add(with);
		}
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public final boolean isCollapseAble() {
		return parent.isCollapseAble(this);
	}

	public ARankColumnModel setCollapsed(boolean collapsed) {
		if (this.collapsed == collapsed)
			return this;
		if (collapsed && parent != null && !parent.isCollapseAble(this))
			return this;
		propertySupport.firePropertyChange(ICollapseableColumnMixin.PROP_COLLAPSED, this.collapsed,
				this.collapsed = collapsed);
		return this;
	}

	public boolean hide() {
		return parent.hide(this);
	}

	public boolean isHideAble() {
		return parent.isHideAble(this);
	}

	public boolean isHidden() {
		return parent.isHidden(this);
	}

	public boolean isDestroyAble() {
		return parent.isDestroyAble(this);
	}

	public boolean destroy() {
		if (!isDestroyAble())
			return false;
		getParent().remove(this);
		return true;
	}

	public ColumnRanker getMyRanker() {
		return parent.getMyRanker(this);
	}

	public void onRankingInvalid() {

	}

	public void takeSnapshot() {
		getTable().addSnapshot(this);
	}

}
