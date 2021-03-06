/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 *******************************************************************************/
package org.caleydo.view.histogram.v2.internal;

import org.caleydo.core.data.collection.EDimension;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.basic.ScrollingDecorator.IHasMinSize;
import org.caleydo.core.view.opengl.layout2.manage.GLElementDimensionDesc;
import org.caleydo.core.view.opengl.layout2.manage.GLElementFactoryContext;
import org.caleydo.view.histogram.v2.PieDistributionElement;

/**
 * element factory for creating distribution elements
 *
 * @author Samuel Gratzl
 *
 */
public class DistributionPieElementFactory extends ADistributionBarElementFactory {
	@Override
	public String getId() {
		return "distribution.pie";
	}

	@Override
	public GLElementDimensionDesc getDesc(EDimension dim, GLElement elem) {
		final float v = dim.select(((IHasMinSize) elem).getMinSize());
		return GLElementDimensionDesc.newFix(v).minimum(v * 0.5f).build();
	}

	@Override
	public GLElement create(GLElementFactoryContext context) {
		return new PieDistributionElement(createData(context));
	}

	@Override
	public GLElement createParameters(GLElement elem) {
		return null;
	}
}
