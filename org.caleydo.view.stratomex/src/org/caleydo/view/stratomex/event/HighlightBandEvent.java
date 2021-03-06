/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.stratomex.event;



import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.ADirectedEvent;
import org.caleydo.core.util.base.IUniqueObject;
import org.caleydo.core.util.color.Color;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;

/**
 * triggers that a specific band (if existing) will be highlighted
 *
 * @author Samuel Gratzl
 *
 */
public class HighlightBandEvent extends ADirectedEvent {
	private final Group groupA;
	/**
	 * either a {@link Group} or a {@link PathwayGraph}
	 */
	private final IUniqueObject groupB;

	private final Color color;


	public HighlightBandEvent(Group a_group, IUniqueObject b_group, Color color) {
		this.groupA = a_group;
		this.groupB = b_group;
		this.color = color;
	}

	public boolean isClearAll() {
		return groupA == null && groupB == null;
	}

	/**
	 * @return the groupA, see {@link #groupA}
	 */
	public Group getGroupA() {
		return groupA;
	}

	/**
	 * @return the groupB, see {@link #groupB}
	 */
	public IUniqueObject getGroupB() {
		return groupB;
	}

	/**
	 * @return true if the specified element should be highlighted or stopping highlighting it
	 */
	public boolean isHighlight() {
		return !isClearAll() && color != null;
	}

	/**
	 * @return the color, see {@link #color}
	 */
	public Color getColor() {
		return color;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
}
