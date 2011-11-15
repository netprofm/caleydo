package org.caleydo.view.visbricks.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.caleydo.core.data.container.DataContainer;
import org.caleydo.core.data.datadomain.DataDomainManager;
import org.caleydo.core.data.perspective.DimensionPerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.datadomain.genetic.GeneticDataDomain;
import org.caleydo.datadomain.pathway.PathwayDataDomain;
import org.caleydo.datadomain.pathway.data.PathwayDimensionGroupData;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Dialog where the user can specify the pathway that shall be displayed as a
 * dimension group with small multiples in Visbricks.
 * 
 * @author Marc Streit
 * 
 */
public class CreateSmallPathwayMultiplesGroupDialog extends TitleAreaDialog {

	private DataContainer dataContainer;
	private DimensionPerspective dimensionPerspective;

	private PathwayDataDomain pathwayDataDomain;

	private Table pathwayTable;

	private Composite parent;

	private PathwayDimensionGroupData pathwayDimensionGroupData;
	private HashMap<PathwayGraph, Integer> pathwayGraphsWithOccurrences;

	public CreateSmallPathwayMultiplesGroupDialog(Shell parentShell,
			DataContainer dataContainer, DimensionPerspective dimensionPerspective) {

		super(parentShell);
		this.dimensionPerspective = dimensionPerspective;
		this.dataContainer = dataContainer;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Create Small Pathway Multiples Group");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.parent = parent;

		parent.setLayout(new GridLayout());

		GridData data = new GridData();
		GridLayout layout = new GridLayout(1, true);

		parent.setLayout(layout);

		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		Label descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setText("Select the pathways for the group.");
		descriptionLabel.setLayoutData(data);

		VirtualArray<?, ?, ?> va = null;
		// if (dataContainer.getDataDomain().isColumnDimension())
		va = dataContainer.getDataDomain().getTable().getDefaultDimensionPerspective()
				.getVirtualArray();

		pathwayGraphsWithOccurrences = PathwayManager.get()
				.getPathwayGraphsWithOccurencesByGeneIDs(
						(GeneticDataDomain) dataContainer.getDataDomain(),
						va.getIdType(), va.getIndexList());

		// Create a list that contains pathways sorted by gene occurrences
		ArrayList<Pair<Integer, PathwayGraph>> sortedPathwayList = new ArrayList<Pair<Integer, PathwayGraph>>();
		for (PathwayGraph pathway : pathwayGraphsWithOccurrences.keySet()) {
			sortedPathwayList.add(new Pair<Integer, PathwayGraph>(
					pathwayGraphsWithOccurrences.get(pathway), pathway));
		}
		Collections.sort(sortedPathwayList);

		Collection<PathwayGraph> dbPathways = new ArrayList<PathwayGraph>();

		for (int count = sortedPathwayList.size() - 1; count >= 0; count--) {
			Pair<Integer, PathwayGraph> pair = sortedPathwayList.get(count);
			if (pair.getFirst() > 1) {

				PathwayGraph pathway = pair.getSecond();
				dbPathways.add(pathway);
			}
		}

		// final Combo databaseCombo = new Combo(parent, SWT.DROP_DOWN);
		// List<String> databaseNames = new
		// ArrayList<String>(pathwayMap.keySet());
		// if (!databaseNames.isEmpty()) {
		// Collections.sort(databaseNames);
		// for (String dbName : databaseNames) {
		// databaseCombo.add(dbName);
		// }
		// databaseCombo.select(databaseCombo.getItemCount() - 1);
		// databaseCombo.addSelectionListener(new SelectionListener() {
		//
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// setTableContent(databaseCombo.getText());
		// }
		//
		// @Override
		// public void widgetDefaultSelected(SelectionEvent e) {
		// widgetSelected(e);
		//
		// }
		// });
		// }

		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		pathwayTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);

		pathwayTable.setHeaderVisible(true);
		TableColumn column1 = new TableColumn(pathwayTable, SWT.CHECK);
		column1.setText("Pathway");
		// column1.addListener(SWT.Selection, pathwayTableSorter);
		TableColumn column2 = new TableColumn(pathwayTable, SWT.NONE);
		column2.setText("Database");
		TableColumn column3 = new TableColumn(pathwayTable, SWT.NONE);
		column3.setText("Gene Occurences");
		pathwayTable.setLayoutData(data);
		pathwayTable.setSortColumn(column1);
		pathwayTable.setSortDirection(SWT.UP);
		pathwayTable.setEnabled(true);

		// setTableContent(databaseCombo.getText());
		setTableContent(dbPathways);

		return parent;
	}

	private void setTableContent(Collection<PathwayGraph> pathways) {
		// List<PathwayGraph> pathways = pathwayMap.get(pathwayDatabase);

		if (pathways == null)
			return;

		pathwayTable.removeAll();

		for (PathwayGraph pathway : pathways) {
			TableItem item = new TableItem(pathwayTable, SWT.NONE);
			item.setText(0, pathway.getTitle());
			item.setText(1, pathway.getType().getName());
			item.setText(2, pathwayGraphsWithOccurrences.get(pathway).toString());
			item.setData(pathway);
		}

		// pathwayTableSorter.sort(0, true);

		for (TableColumn column : pathwayTable.getColumns()) {
			column.pack();
		}

		pathwayTable.pack();
		parent.layout();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		ArrayList<PathwayGraph> pathways = new ArrayList<PathwayGraph>();

		for (TableItem item : pathwayTable.getItems()) {
			if (item.getChecked()) {
				pathways.add((PathwayGraph) item.getData());
			}
		}

		pathwayDataDomain = (PathwayDataDomain) DataDomainManager.get()
				.getDataDomainByType(PathwayDataDomain.DATA_DOMAIN_TYPE);

		// list
		if (!pathways.isEmpty()) {

			ArrayList<PathwayGraph> pathwayGraphs = new ArrayList<PathwayGraph>();
			
			// TODO do this for all pathway - not just the first from the pathway
			PathwayGraph pathway = pathways.get(0);
			pathwayGraphs.add(pathway);

			pathwayDimensionGroupData = new PathwayDimensionGroupData(
					dataContainer.getDataDomain(), pathwayDataDomain,
					dataContainer.getRecordPerspective(), dimensionPerspective, pathwayGraphs,
					pathway.getTitle());

			super.okPressed();
		}

	}

	public PathwayDimensionGroupData getPathwayDimensionGroupData() {
		return pathwayDimensionGroupData;
	}
}