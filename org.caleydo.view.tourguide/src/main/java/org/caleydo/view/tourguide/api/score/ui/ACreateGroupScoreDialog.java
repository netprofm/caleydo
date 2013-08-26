/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.tourguide.api.score.ui;

import org.caleydo.core.util.color.Color;
import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.DataDomainOracle;
import org.caleydo.core.data.perspective.variable.Perspective;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.view.tourguide.api.query.EDataDomainQueryMode;
import org.caleydo.view.tourguide.api.score.MultiScore;
import org.caleydo.view.tourguide.api.util.ui.CaleydoLabelProvider;
import org.caleydo.view.tourguide.internal.event.AddScoreColumnEvent;
import org.caleydo.view.tourguide.spi.score.IRegisteredScore;
import org.caleydo.view.tourguide.spi.score.IScore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;

/**
 * a basic dialog for creating a group score
 *
 * @author Samuel Gratzl
 *
 */
public abstract class ACreateGroupScoreDialog extends Dialog {

	private static final Group ALL_GROUP = new Group();

	static {
		ALL_GROUP.setLabel("--ALL--");
	}

	private final Object receiver;

	private Text labelUI;
	private ComboViewer dataDomainUI;
	private ComboViewer stratificationUI;
	private ComboViewer groupUI;

	public ACreateGroupScoreDialog(Shell shell, Object receiver) {
		super(shell);
		this.receiver = receiver;
	}

	@Override
	public void create() {
		super.create();
		getShell().setText("Create a new " + getLabel());
		this.setBlockOnOpen(false);
	}

	protected abstract String getLabel();

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite c = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		c.setLayoutData(new GridData(GridData.FILL_BOTH));

		new Label(c, SWT.NONE).setText("Name: ");
		this.labelUI = new Text(c, SWT.BORDER);
		this.labelUI.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		new Label(c, SWT.NONE).setText("Data Domain: ");
		this.dataDomainUI = new ComboViewer(c, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		this.dataDomainUI.setContentProvider(ArrayContentProvider.getInstance());
		this.dataDomainUI.setLabelProvider(new CaleydoLabelProvider());
		this.dataDomainUI.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.dataDomainUI.setInput(EDataDomainQueryMode.STRATIFICATIONS.getAllDataDomains());
		this.dataDomainUI.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ATableBasedDataDomain dataDomain = (ATableBasedDataDomain) selection.getFirstElement();
				updateStratifications(dataDomain);
				dataDomainUI.refresh();
			}
		});
		new Label(c, SWT.NONE).setText("Stratification: ");
		this.stratificationUI = new ComboViewer(c, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		this.stratificationUI.setContentProvider(ArrayContentProvider.getInstance());
		this.stratificationUI.setLabelProvider(new CaleydoLabelProvider());
		this.stratificationUI.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.stratificationUI.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Perspective dataDomain = (Perspective) selection.getFirstElement();
				updateGroups(dataDomain);
			}
		});
		this.stratificationUI.getCombo().setEnabled(false);

		new Label(c, SWT.NONE).setText("Group: ");
		this.groupUI = new ComboViewer(c, SWT.DROP_DOWN | SWT.BORDER | SWT.READ_ONLY);
		this.groupUI.setContentProvider(ArrayContentProvider.getInstance());
		this.groupUI.setLabelProvider(new CaleydoLabelProvider());
		this.groupUI.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		this.groupUI.getCombo().setEnabled(false);

		addTypeSpecific(c);

		return c;
	}

	/**
	 * adds specific widgets to this dialog
	 * 
	 * @param c
	 */
	protected abstract void addTypeSpecific(Composite c);

	/**
	 * updates stratifications based on the given {@link ATableBasedDataDomain}
	 * 
	 * @param dataDomain
	 */
	protected void updateStratifications(ATableBasedDataDomain dataDomain) {
		if (dataDomain == null) {
			this.stratificationUI.setInput(null);
			this.stratificationUI.getCombo().setEnabled(false);
		} else {
			ATableBasedDataDomain d = dataDomain;
			DataDomainOracle.initDataDomain(d);
			List<Perspective> data = new ArrayList<>(); // just stratifications

			for (String id : d.getTable().getRecordPerspectiveIDs()) {
				Perspective p = d.getTable().getRecordPerspective(id);
				if (p.isDefault())
					continue;
				data.add(p);
			}
			this.stratificationUI.setInput(data);
			this.stratificationUI.getCombo().setEnabled(true);
		}
	}

	/**
	 * updates group based on the given {@link Perspective}
	 * 
	 * @param perspective
	 */
	protected void updateGroups(Perspective perspective) {
		if (perspective == null) {
			this.groupUI.setInput(null);
			this.groupUI.getCombo().setEnabled(false);
		} else {
			List<Group> data = Lists.newArrayList(perspective.getVirtualArray().getGroupList());
			data.add(0, ALL_GROUP);
			this.groupUI.setInput(data);
			this.groupUI.getCombo().setEnabled(true);
		}
	}

	@Override
	protected void okPressed() {
		if (!validate())
			return;
		save();
		super.okPressed();
	}

	protected boolean validate() {
		boolean valid = true;
		if (stratificationUI.getSelection() == null) {
			MessageDialog.openError(getParentShell(), "A stratification is required", "A stratification is required");
			valid = false;
		}
		return valid;
	}

	protected abstract IRegisteredScore createScore(String label, Perspective strat, Group g);

	private void save() {
		String label = labelUI.getText();
		Perspective per = (Perspective) ((IStructuredSelection) stratificationUI.getSelection())
				.getFirstElement();
		Group group = groupUI.getSelection() == null ? null : (Group) ((IStructuredSelection) groupUI.getSelection())
				.getFirstElement();
		IScore s;
		if (group == null || group == ALL_GROUP) { // score all
			MultiScore composite = new MultiScore(label == null ? per.getLabel() : label, Color.GRAY, new Color(
					0.95f, .95f, .95f));
			for (Group g : per.getVirtualArray().getGroupList()) {
				composite.add(createScore(null, per, g));
			}
			s = composite;
		} else { // score single
			s = createScore(label, per, group);
		}
		EventPublisher.trigger(new AddScoreColumnEvent(s).to(receiver));
	}
}