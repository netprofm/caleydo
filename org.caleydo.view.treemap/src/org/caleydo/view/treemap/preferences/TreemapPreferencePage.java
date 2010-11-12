package org.caleydo.view.treemap.preferences;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference Page for Treemap appearance and behavior.
 * 
 * @author Michael Lafer
 * @author Oliver Pimas
 * @author Alexander Lex
 */
public class TreemapPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	private Combo layoutAlgorithmCB;
	private Button frameButton;
	private Spinner maxDepthSp;
	private Button maxDepthButton;
	
	private PreferenceStore preferenceStore;

	private static final String LAYOUT_ALGORITHM_DISPLAYNAME[] = {"Simple Layout Algorithm", "Squarified Treemap Layout Algorithm"};
	
	public TreemapPreferencePage() {
		super(GRID);
		preferenceStore = GeneralManager.get().getPreferenceStore();
		setPreferenceStore(preferenceStore);
		setDescription("Set treemap appearance");
	}

	/**
	 * Creates the gui components which are initialized with default values or from the pref store.
	 */
	@Override
	public void createFieldEditors() {

		Composite baseComposite = new Composite(getFieldEditorParent(), SWT.NULL);
		baseComposite.setLayout(new GridLayout(1, false));

		Label l= new Label(baseComposite, SWT.SHADOW_NONE);
		l.setText("The Algorithm used to layout the Treemap");
		layoutAlgorithmCB = new Combo(baseComposite, SWT.READ_ONLY);
		
		layoutAlgorithmCB.setItems(LAYOUT_ALGORITHM_DISPLAYNAME);
		int selectedLayout = preferenceStore.getInt(PreferenceConstants.TREEMAP_LAYOUT_ALGORITHM);
		layoutAlgorithmCB.setText(LAYOUT_ALGORITHM_DISPLAYNAME[selectedLayout]);

		frameButton = new Button(baseComposite, SWT.CHECK);
		boolean frameSelection=preferenceStore.getBoolean(PreferenceConstants.TREEMAP_DRAW_CLUSTER_FRAME);
		frameButton.setSelection(frameSelection);
		frameButton.setText("Draw frame for Clusters");
		
		int maxDepth=preferenceStore.getInt(PreferenceConstants.TREEMAP_MAX_DEPTH);
		maxDepthButton= new Button(baseComposite, SWT.CHECK);
		maxDepthButton.setSelection(maxDepth>0);
		maxDepthButton.setText("Enable node abstraction");
		maxDepthButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				maxDepthSp.setEnabled(maxDepthButton.getSelection());
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		maxDepthSp= new Spinner(baseComposite, SWT.BORDER);
		maxDepthSp.setEnabled(maxDepth>0);
		maxDepthSp.setDigits(0);
		maxDepthSp.setMaximum(999);
		maxDepthSp.setMinimum(1);
		maxDepthSp.setIncrement(1);
		maxDepthSp.setSelection(maxDepth);
		

		baseComposite.pack();	
	}

	@Override
	protected void performDefaults() {

	}

	@Override
	public boolean performOk() {
		boolean bReturn = super.performOk();


		int selectedLayout = layoutAlgorithmCB.getSelectionIndex();
		preferenceStore.setValue(PreferenceConstants.TREEMAP_LAYOUT_ALGORITHM, selectedLayout);
		
		preferenceStore.setValue(PreferenceConstants.TREEMAP_DRAW_CLUSTER_FRAME, frameButton.getSelection());
		
		preferenceStore.setValue(PreferenceConstants.TREEMAP_MAX_DEPTH, maxDepthSp.getSelection());
		
		return bReturn;
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

}
