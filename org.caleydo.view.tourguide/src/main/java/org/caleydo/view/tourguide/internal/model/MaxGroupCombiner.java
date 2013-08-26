/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.internal.model;

import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.view.tourguide.internal.score.ExternalIDTypeScore;
import org.caleydo.view.tourguide.spi.score.IDecoratedScore;
import org.caleydo.view.tourguide.spi.score.IGroupScore;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.caleydo.view.tourguide.spi.score.IStratificationScore;
import org.caleydo.vis.lineup.data.AFloatFunction;
import org.caleydo.vis.lineup.model.IRow;


/**
 * the current strategy up to now is to show just a single stratification but here the maximal value for a group score,
 *
 * therefore if we have a group score
 *
 * @author Samuel Gratzl
 *
 */
public class MaxGroupCombiner extends AFloatFunction<IRow> {

	private final IScore score;

	/**
	 * @param score
	 */
	public MaxGroupCombiner(IScore score) {
		this.score = score;
	}

	@Override
	public float applyPrimitive(IRow in) {
		AScoreRow row = (AScoreRow) in;
		if (score instanceof IStratificationScore && !(score instanceof IGroupScore)) {
			return score.apply(row, null); // as group independent
		}
		if (score instanceof ExternalIDTypeScore && !((ExternalIDTypeScore) score).isCompatible(row.getIdType())) {
			// working on dimension ids just once
			return score.apply(row, null);
		}
		if (score instanceof IDecoratedScore) {
			Group g = MaxGroupCombiner.getMax(in, ((IDecoratedScore) score).getUnderlying());
			if (g == null)
				return Float.NaN;
			return score.apply(row, g);
		} else
			return getMax(row);
	}

	private float getMax(AScoreRow row) {
		float v = Float.NaN;
		for(Group g : row.getGroups()) {
			float vg = score.apply(row, g);
			if (Float.isNaN(vg))
				continue;
			if (Float.isNaN(v) || vg > v)
				v = vg;
		}
		return v;
	}

	public static Group getMax(IRow in, IScore score) {
		if (score == null)
			return null;
		AScoreRow row = (AScoreRow) in;
		if (score instanceof IStratificationScore && !(score instanceof IGroupScore)) {
			return null;
		}

		// combine groups
		float v = Float.NaN;
		Group gm = null;
		for (Group g : row.getGroups()) {
			float vg = score.apply(row, g);
			if (Float.isNaN(vg))
				continue;
			if (Float.isNaN(v) || vg > v) {
				v = vg;
				gm = g;
			}
		}
		return gm;
	}

}
