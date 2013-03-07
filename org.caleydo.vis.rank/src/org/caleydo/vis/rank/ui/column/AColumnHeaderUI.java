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
package org.caleydo.vis.rank.ui.column;

import static org.caleydo.core.view.opengl.layout2.layout.GLLayouts.defaultValue;
import static org.caleydo.vis.rank.ui.RenderStyle.LABEL_HEIGHT;
import gleem.linalg.Vec2f;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.caleydo.core.util.base.ILabelProvider;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout.Column.VAlign;
import org.caleydo.core.view.opengl.layout2.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.layout2.IMouseLayer;
import org.caleydo.core.view.opengl.layout2.PickableGLElement;
import org.caleydo.core.view.opengl.layout2.animation.Durations;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.animation.Transitions;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.vis.rank.internal.ui.ButtonBar;
import org.caleydo.vis.rank.model.ARankColumnModel;
import org.caleydo.vis.rank.model.mixin.IAnnotatedColumnMixin;
import org.caleydo.vis.rank.model.mixin.ICollapseableColumnMixin;
import org.caleydo.vis.rank.model.mixin.ICompressColumnMixin;
import org.caleydo.vis.rank.model.mixin.IExplodeableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IFilterColumnMixin;
import org.caleydo.vis.rank.model.mixin.IHideableColumnMixin;
import org.caleydo.vis.rank.model.mixin.IMappedColumnMixin;
import org.caleydo.vis.rank.model.mixin.ISnapshotableColumnMixin;
import org.caleydo.vis.rank.ui.IColumnRenderInfo;
import org.caleydo.vis.rank.ui.RenderStyle;
import org.eclipse.swt.SWT;

/**
 * @author Samuel Gratzl
 *
 */
public class AColumnHeaderUI extends AnimatedGLElementContainer implements IGLLayout, IColumnRenderInfo, ILabelProvider {
	private final static int HIST = 0;
	private final static int DRAG_WEIGHT = 1;
	private final static int BUTTONS = 2;
	private final static int UNCOLLAPSE = 3;

	private final boolean interactive;
	private boolean canDrag;
	private boolean armDropColum;
	private String armDropHint;

	private boolean isCollapsed;

	private boolean isDragging;
	private boolean headerHovered;

	protected final ARankColumnModel model;
	private PropertyChangeListener filterChangedListener;
	private final PropertyChangeListener collapsedChanged = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			onCollapsedChanged(evt.getNewValue() == Boolean.TRUE);
		}
	};
	private int dragPickingId = -1;

	private boolean hasTitle;
	private final boolean canChangeWeight;
	private final boolean moveable;


	public AColumnHeaderUI(final ARankColumnModel model, boolean interactive, boolean moveable, boolean hasTitle,
			boolean hasHist, boolean canChangeWeight) {
		this.model = model;
		model.addPropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, collapsedChanged);
		this.interactive = interactive;
		this.moveable = moveable;
		this.hasTitle = hasTitle;
		this.canChangeWeight = canChangeWeight;

		setLayout(this);
		setLayoutData(model);
		if (moveable)
			this.setVisibility(EVisibility.PICKABLE);
		this.onPick(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onMainPick(pick);
			}
		});
		if (hasHist) {
			this.add(model.createSummary(interactive).setLayoutData(Durations.NO), 0);
		} else {
			this.add(new GLElement().setVisibility(EVisibility.HIDDEN));
		}
		if (interactive) {
			this.add(new DragElement().setLayoutData(MoveTransitions.GROW_LINEAR), 0);
			this.add(
					createButtons().setLayoutData(
							new MoveTransitions.MoveTransitionBase(Transitions.NO, Transitions.NO, Transitions.NO,
									Transitions.LINEAR)), 0);

			this.isCollapsed = (model instanceof ICollapseableColumnMixin) ? ((ICollapseableColumnMixin) model)
					.isCollapsed() : false;

			GLButton b = new GLButton();
			b.setzDelta(0.5f);
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_COLLAPSE));
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					((ICollapseableColumnMixin) model).setCollapsed(false);
				}
			});
			b.setTooltip("Toggle Collapse / Expand of this column");
			b.setLayoutData(Durations.NO);
			this.add(b, 0);

			this.isCollapsed = !isCollapsed;
			onCollapsedChanged(!isCollapsed); // force a change
		}

	}

	/**
	 * @param hasTitle
	 *            setter, see {@link hasTitle}
	 */
	public void setHasTitle(boolean hasTitle) {
		if (this.hasTitle == hasTitle)
			return;
		this.hasTitle = hasTitle;
		relayout();
	}

	@Override
	public boolean isCollapsed() {
		return ((model instanceof ICollapseableColumnMixin) && ((ICollapseableColumnMixin) model).isCollapsed());
	}

	@Override
	public VAlign getAlignment() {
		return VAlign.LEFT;
	}

	@Override
	public boolean hasFreeSpace() {
		return false;
	}

	@Override
	protected void init(IGLElementContext context) {
		super.init(context);
		onPick(context.createTooltip(this));
		dragPickingId = context.registerPickingListener(new IPickingListener() {
			@Override
			public void pick(Pick pick) {
				onDragPick(pick);
			}
		});
	}

	@Override
	public String getProviderName() {
		return null;
	}

	@Override
	public String getLabel() {
		String ann = ((model instanceof IAnnotatedColumnMixin) ? ((IAnnotatedColumnMixin) model).getAnnotation() : null);
		ann = ann == null ? "" : ann.trim();
		if (ann.trim().isEmpty())
			return model.getTooltip();
		return model.getTooltip() + "\n" + ann;
	}

	protected void onCollapsedChanged(boolean isCollapsed) {
		if (this.isCollapsed == isCollapsed)
			return;
		this.isCollapsed = isCollapsed;
		if (!interactive)
			return;
		if (isCollapsed) {
			this.get(DRAG_WEIGHT).setVisibility(EVisibility.HIDDEN);
			this.get(BUTTONS).setVisibility(EVisibility.HIDDEN);
			this.get(UNCOLLAPSE).setVisibility(EVisibility.PICKABLE);
		} else {
			this.get(DRAG_WEIGHT).setVisibility(canChangeWeight ? EVisibility.PICKABLE: EVisibility.HIDDEN);
			this.get(BUTTONS).setVisibility(EVisibility.VISIBLE);
			this.get(UNCOLLAPSE).setVisibility(EVisibility.HIDDEN);
		}
		repaintAll();
	}

	@Override
	protected void takeDown() {
		context.unregisterPickingListener(dragPickingId);
		dragPickingId = -1;
		model.removePropertyChangeListener(IFilterColumnMixin.PROP_FILTER, filterChangedListener);
		model.removePropertyChangeListener(ICollapseableColumnMixin.PROP_COLLAPSED, collapsedChanged);
		super.takeDown();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		renderBackground(g, w, h);
		super.renderImpl(g, w, h);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (moveable) {
			g.incZ();
			g.pushName(dragPickingId);
			g.fillRect(0, 0, w, h);
			g.popName();
			g.decZ();
		}
		super.renderPickImpl(g, w, h);
	}

	protected void renderBackground(GLGraphics g, float w, float h) {
		g.color(model.getBgColor()).renderRoundedRect(true, 0, 0, w, h, 5, 2, true, true, false, false);
		if (isCollapsed)
			return;
		if (hasTitle) {
			g.move(2, 2);
			model.getHeaderRenderer().render(g, w - 4, LABEL_HEIGHT - 6, this);
			g.move(-2, -2);
		}
		if (headerHovered) {
			g.color(Color.BLACK).renderRoundedRect(false, 0, 0, w, h, 5, 2, true, true, false, false);
		}
		if (this.armDropColum) {
			g.incZ(0.6f);
			g.drawText(armDropHint, 2, 2, w - 4, h - 4, VAlign.CENTER);
			g.incZ(-0.6f);
		}
	}



	protected GLElementContainer createButtons() {
		ButtonBar buttons = new ButtonBar();
		buttons.setzDelta(.5f);

		if (model instanceof IFilterColumnMixin) {
			final IFilterColumnMixin m = (IFilterColumnMixin) model;
			final GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_FILTER_DISABLED));
			b.setSelectedRenderer(GLRenderers.fillImage(RenderStyle.ICON_FILTER));
			b.setSelected(m.isFiltered());
			b.setTooltip("Edit the filter of this column");
			final ISelectionCallback callback = new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.editFilter(get(HIST), context);
				}
			};
			b.setCallback(callback);
			filterChangedListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					b.setCallback(null);
					b.setSelected(m.isFiltered());
					b.setCallback(callback);
				}
			};
			model.addPropertyChangeListener(IFilterColumnMixin.PROP_FILTER, filterChangedListener);
			buttons.addButton(b);
		}
		if (model instanceof IMappedColumnMixin) {
			final IMappedColumnMixin m = (IMappedColumnMixin) model;
			GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_MAPPING));
			b.setTooltip("Edit the mapping of this column");
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.editMapping(get(HIST), context);
				}
			});
			buttons.addButton(b);
		}
		if (model instanceof IExplodeableColumnMixin) {
			final IExplodeableColumnMixin m = (IExplodeableColumnMixin) model;
			GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_EXPLODE));
			b.setTooltip("Split this combined column in individual ones");
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.explode();
				}
			});
			buttons.addButton(b);
		}
		if (model instanceof IAnnotatedColumnMixin) {
			final IAnnotatedColumnMixin m = (IAnnotatedColumnMixin) model;
			GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_EDIT_ANNOTATION));
			b.setTooltip("Edit the annotation of this element");
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.editAnnotation(get(HIST));
				}
			});
			buttons.addButton(b);
		}
		if (model instanceof ISnapshotableColumnMixin) {
			final ISnapshotableColumnMixin m = (ISnapshotableColumnMixin) model;
			final GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_FREEZE));
			b.setTooltip("Take a snapshot of the current state");
			final ISelectionCallback callback = new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.takeSnapshot();
				}
			};
			b.setCallback(callback);
			buttons.addButton(b);
		}
		buttons.addSpacer();

		if (model instanceof ICollapseableColumnMixin) {
			final ICollapseableColumnMixin m = (ICollapseableColumnMixin) model;
			if (m.isCollapseAble()) {
				GLButton b = new GLButton();
				b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_UNCOLLAPSE));
				b.setCallback(new ISelectionCallback() {
					@Override
					public void onSelectionChanged(GLButton button, boolean selected) {
						m.setCollapsed(true);
					}
				});
				b.setTooltip("Toggle Collapse / Expand of this column");
				buttons.addButton(b);
			}
		}
		if (model instanceof ICompressColumnMixin) {
			final ICompressColumnMixin m = (ICompressColumnMixin) model;

			GLButton b = new GLButton(EButtonMode.CHECKBOX);
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_UNCOLLAPSE));
			b.setSelectedRenderer(GLRenderers.fillImage(RenderStyle.ICON_COLLAPSE));
			b.setSelected(m.isCompressed());
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					m.setCompressed(selected);
				}
			});
			b.setTooltip("Toggle Compress / Unpack of this column");
			buttons.addButton(b);
		}
		if (model instanceof IHideableColumnMixin) {
			final IHideableColumnMixin m = (IHideableColumnMixin) model;
			GLButton b = new GLButton();
			b.setRenderer(GLRenderers.fillImage(RenderStyle.ICON_HIDE));
			b.setTooltip("Removes this column");
			b.setCallback(new ISelectionCallback() {
				@Override
				public void onSelectionChanged(GLButton button, boolean selected) {
					if (m.isHideAble())
						m.hide();
				}
			});
			buttons.addButton(b);
		}
		return buttons;
	}


	@Override
	public void doLayout(List<? extends IGLLayoutElement> children, float w, float h) {

		IGLLayoutElement hist = children.get(HIST);
		hist.setBounds(1, hasTitle ? LABEL_HEIGHT : 0, w - 2, h - (hasTitle ? LABEL_HEIGHT : 0));

		if (interactive) {
			IGLLayoutElement weight = children.get(DRAG_WEIGHT);
			weight.setBounds(w, hasTitle ? LABEL_HEIGHT : 0, (canDrag && canChangeWeight) ? 8 : 0, h
					- (hasTitle ? LABEL_HEIGHT : 0));

			IGLLayoutElement buttons = children.get(BUTTONS);
			float minWidth = (buttons.asElement() instanceof ButtonBar) ? ((ButtonBar) buttons.asElement())
					.getMinWidth() : 0;
			if ((w - 4) < minWidth) {
				float missing = minWidth - (w - 4);
				buttons.setBounds(-missing * 0.5f, 2, minWidth, canDrag ? RenderStyle.BUTTON_WIDTH : 0);
			} else {
				buttons.setBounds(2, 2, w - 4, canDrag ? RenderStyle.BUTTON_WIDTH : 0);
			}

			IGLLayoutElement uncollapse = children.get(UNCOLLAPSE);
			uncollapse.setBounds((w - RenderStyle.BUTTON_WIDTH) * .5f, 2, RenderStyle.BUTTON_WIDTH,
					canDrag ? RenderStyle.BUTTON_WIDTH : 0);

			for (IGLLayoutElement r : children.subList(UNCOLLAPSE + 1, children.size()))
				r.setBounds(defaultValue(r.getSetX(), 0), defaultValue(r.getSetY(), h),
						defaultValue(r.getSetWidth(), w), defaultValue(r.getSetHeight(), 40));
		}

	}

	/**
	 * @param pick
	 */
	protected void onDragPick(Pick pick) {
		switch (pick.getPickingMode()) {
		case CLICKED:
			if (pick.isAnyDragging())
				return;
			pick.setDoDragging(true);
			onDragColumn(pick);
			break;
		case MOUSE_RELEASED:
			if (pick.isDoDragging())
				onDropColumn(pick);
			break;
		case MOUSE_OVER:
			if (pick.isAnyDragging())
				return;
			this.headerHovered = true;
			context.setCursor(SWT.CURSOR_HAND);
			repaint();
			break;
		case MOUSE_OUT:
			if (this.headerHovered) {
				this.headerHovered = false;
				context.setCursor(-1);
				repaint();
			}
			break;
		default:
			break;
		}
	}

	protected void onMainPick(Pick pick) {
		IMouseLayer m = context.getMouseLayer();
		switch (pick.getPickingMode()) {
		case MOUSE_OVER:
			if (!pick.isDoDragging() && m.hasDraggable(ARankColumnModel.class)) {
				Pair<GLElement, ARankColumnModel> pair = m.getFirstDraggable(ARankColumnModel.class);
				if (model.isCombineAble(pair.getSecond())) {
					m.setDropable(ARankColumnModel.class, true);
					this.armDropColum = true;
					armDropHint = model.getTable().getConfig().getCombineStringHint(model, pair.getSecond());
					repaint();
				}
			} else if (!pick.isAnyDragging()) {
				this.canDrag = true;
				this.relayout();
			}
			break;
		case MOUSE_OUT:
			if (armDropColum) {
				this.armDropColum = false;
				m.setDropable(ARankColumnModel.class, false);
				repaint();
			}
			if (this.canDrag) {
				this.canDrag = false;
				this.relayout();
			}
			break;
		case MOUSE_RELEASED:
			if (this.armDropColum) {
				Pair<GLElement, ARankColumnModel> info = m.getFirstDraggable(ARankColumnModel.class);
				m.removeDraggable(info.getFirst());
				m.setDropable(ARankColumnModel.class, false);
				context.setCursor(-1);
				if (info != null)
					model.combine(info.getSecond());
			}
			break;
		default:
			break;
		}
	}

	protected void onChangeWeight(int dx) {
		if (dx == 0)
			return;
		// float delta = (dx / getSize().x())*;
		model.addWeight(dx);
	}

	/**
	 * drop drag column again
	 *
	 * @param pick
	 */
	private void onDropColumn(Pick pick) {
		IMouseLayer l = context.getMouseLayer();
		if (this.isDragging) {
			if (!l.isDropable(this.model)) {
				l.removeDraggable(this.model);
			}
			this.isDragging = false;
			context.setCursor(-1);
			return;
		}
	}

	private void onDragColumn(Pick pick) {
		IMouseLayer l = context.getMouseLayer();
		GLElement elem = new DraggedScoreHeaderItem();
		elem.setSize(getSize().x(), getSize().y());
		Vec2f loc = toRelative(pick.getPickedPoint());
		elem.setLocation(-loc.x(), -loc.y());
		isDragging = true;
		l.addDraggable(elem, this.model);
	}

	class DraggedScoreHeaderItem extends GLElement {
		@Override
		protected void renderImpl(GLGraphics g, float w, float h) {
			renderBackground(g, w, h);
			if (get(HIST).getParent() != null)
				get(HIST).render(g);
		}
	}

	class DragElement extends PickableGLElement {
		private boolean hovered = false;
		public DragElement() {
			setRenderer(GLRenderers.fillImage(RenderStyle.ICON_DRAG));
			setTooltip("Drag this element to change the weight of this column");
			setzDelta(.5f);
		}

		@Override
		protected void onMouseOver(Pick pick) {
			if (pick.isAnyDragging())
				return;
			this.hovered = true;
			context.setCursor(SWT.CURSOR_HAND);
			repaintAll();
		}

		@Override
		protected void onMouseOut(Pick pick) {
			if (this.hovered)
				context.setCursor(-1);
			this.hovered = false;
			repaintAll();
		}


		@Override
		protected void onClicked(Pick pick) {
			if (pick.isAnyDragging())
				return;
			pick.setDoDragging(true);
		}

		@Override
		protected void onDragged(Pick pick) {
			if (pick.isDoDragging())
				onChangeWeight(pick.getDx());
		}

		@Override
		protected void renderPickImpl(GLGraphics g, float w, float h) {
			super.renderPickImpl(g, w, h);
			if (hovered) {
				g.fillRect(0, 0, w * 2, h);
			}
		}
	}
}
