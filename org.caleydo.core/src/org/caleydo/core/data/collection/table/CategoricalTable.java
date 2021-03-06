/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.data.collection.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.collection.column.AColumn;
import org.caleydo.core.data.collection.column.CategoricalColumn;
import org.caleydo.core.data.collection.column.container.CategoricalClassDescription;
import org.caleydo.core.data.collection.column.container.CategoryProperty;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.color.mapping.ColorMapper;
import org.caleydo.core.util.color.mapping.ColorMarkerPoint;

import com.google.common.collect.Maps;

/**
 * Extension of {@link Table} to add functionality specific to homogeneous categorical tables, such as a joint set of
 * categories or a joint order of categories.
 *
 * @author Alexander Lex
 */
public class CategoricalTable<CategoryType extends Comparable<CategoryType>> extends Table {
	private CategoricalClassDescription<CategoryType> categoricalClassDescription;

	private List<Map<CategoryType, Integer>> oppositeNumMatches;

	/**
	 * @param dataDomain
	 */
	public CategoricalTable(ATableBasedDataDomain dataDomain) {
		super(dataDomain);
	}

	@Override
	public boolean isDataHomogeneous() {
		return true;
	}

	@Override
	protected void normalize() {
		if (categoricalClassDescription == null) {
			Set<CategoryType> uniqueCategories = new HashSet<>();
			for (AColumn<?, ?> column : columns) {
				@SuppressWarnings("unchecked")
				CategoricalColumn<CategoryType> catCol = (CategoricalColumn<CategoryType>) column;
				uniqueCategories.addAll(catCol.getCategories());
			}
			categoricalClassDescription = new CategoricalClassDescription<>();
			categoricalClassDescription.autoInitialize(uniqueCategories);
			for (AColumn<?, ?> column : columns) {
				@SuppressWarnings("unchecked")
				CategoricalColumn<CategoryType> catCol = (CategoricalColumn<CategoryType>) column;
				catCol.setCategoryDescriptions(categoricalClassDescription);
			}

		}
		super.normalize();

		//no build the opposite num matches
		//first one use to initialize

		{
			@SuppressWarnings("unchecked")
			CategoricalColumn<CategoryType> firstColumn = (CategoricalColumn<CategoryType>) columns.get(0);
			int numCategories = getCategoryDescriptions().getCategoryProperties().size();
			oppositeNumMatches = new ArrayList<>(firstColumn.size());
			for (int i = 0; i < firstColumn.size(); ++i) {
				CategoryType v = firstColumn.getRaw(i);
				HashMap<CategoryType, Integer> hist = Maps.newHashMapWithExpectedSize(numCategories);
				hist.put(v, 1);
				oppositeNumMatches.add(hist);
			}
		}
		for (AColumn<?, ?> col : columns.subList(1, columns.size())) {
			@SuppressWarnings("unchecked")
			CategoricalColumn<CategoryType> column = (CategoricalColumn<CategoryType>) col;
			for (int i = 0; i < col.size(); ++i) {
				CategoryType v = column.getRaw(i);
				Map<CategoryType, Integer> hist = oppositeNumMatches.get(i);
				Integer h = hist.get(v);
				if (h == null)
					hist.put(v, 1);
				else
					hist.put(v, h + 1);
			}
		}
	}

	public void setCategoryDescritions(CategoricalClassDescription<CategoryType> categoryDescriptions) {
		this.categoricalClassDescription = categoryDescriptions;
		// for (AColumn<?, ?> column : columns) {
		// @SuppressWarnings("unchecked")
		// CategoricalColumn<CategoryType> categoricalColum = (CategoricalColumn<CategoryType>) column;
		// categoricalColum.setCategoryDescritions(categoricalClassDescription);
		// }
	}

	/**
	 * @return the categoricalClassDescription, see {@link #categoricalClassDescription}
	 */
	public CategoricalClassDescription<CategoryType> getCategoryDescriptions() {
		return categoricalClassDescription;
	}

	/**
	 * Creates a new color map based on the colors and the order of {@link #categoricalClassDescription}.
	 *
	 * @return
	 */
	public ColorMapper createColorMapper() {
		ColorMapper mapper = new ColorMapper();
		float normalizedDistance = 0;
		if (categoricalClassDescription.size() > 1) {
			normalizedDistance = 1f / (categoricalClassDescription.size() - 1);
		}
		float currentDistance = 0;
		ArrayList<ColorMarkerPoint> markerPoints = new ArrayList<>(categoricalClassDescription.size());
		for (CategoryProperty<?> property : categoricalClassDescription) {
			markerPoints.add(new ColorMarkerPoint(currentDistance, property.getColor()));
			currentDistance += normalizedDistance;
		}

		mapper.setMarkerPoints(markerPoints);
		return mapper;

	}

	/**
	 * @param category
	 * @param columnID
	 * @return
	 */
	public int getNumberOfMatches(Object category, IDType idType, Integer id) {
		if ((idType == getDataDomain().getDimensionIDType()) == isColumnDimension) {
			CategoricalColumn<?> catCol = (CategoricalColumn<?>) columns.get(id);
			if (catCol == null)
				return 0;
			return catCol.getNumberOfMatches(category);
		} else {
			Map<CategoryType, Integer> map = oppositeNumMatches.get(id);
			Integer c = map.get(category);
			return c == null ? 0 : c;
		}
	}
}
