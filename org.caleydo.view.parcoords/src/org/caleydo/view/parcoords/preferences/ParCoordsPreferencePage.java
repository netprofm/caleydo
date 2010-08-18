package org.caleydo.view.parcoords.preferences;

import java.util.Collection;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.util.preferences.PreferenceConstants;
import org.caleydo.core.view.opengl.canvas.AGLView;
import org.caleydo.view.parcoords.GLParallelCoordinates;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for heat map specific settings
 * 
 * @author Alexander Lex
 * @deprecated STILL IN USE????
 */
@Deprecated
public class ParCoordsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	private IntegerFieldEditor numRandomSamplesFE;
	private BooleanFieldEditor limitRemoteToContext;

	public ParCoordsPreferencePage() {
		super(GRID);
		// setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setPreferenceStore(GeneralManager.get().getPreferenceStore());
		setDescription("Preferences for the Parallel Coordinates view.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		// Create the layout.
		RowLayout layout = new RowLayout();
		// Optionally set layout fields.
		layout.wrap = true;
		getFieldEditorParent().setLayout(layout);
		numRandomSamplesFE = new IntegerFieldEditor(
				PreferenceConstants.PC_NUM_RANDOM_SAMPLING_POINT,
				"Number of Random Samples:", getFieldEditorParent());
		numRandomSamplesFE.loadDefault();
		addField(numRandomSamplesFE);

		limitRemoteToContext = new BooleanFieldEditor(
				PreferenceConstants.PC_LIMIT_REMOTE_TO_CONTEXT,
				"Limit remote views to show contextual information only",
				getFieldEditorParent());
		limitRemoteToContext.loadDefault();
		addField(limitRemoteToContext);

		getFieldEditorParent().pack();
	}

	@Override
	protected void performDefaults() {

	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	public boolean performOk() {
		boolean bReturn = super.performOk();

		Collection<AGLView> eventListeners = GeneralManager.get()
				.getViewGLCanvasManager().getAllGLViews();
		for (AGLView glView : eventListeners) {
			if (glView.getViewType().equals("org.caleydo.view.parcoords")) {
				GLParallelCoordinates parCoords = (GLParallelCoordinates) glView;
				// if(!heatMap.isRenderedRemote())
				// {
				parCoords.setNumberOfSamplesToShow(numRandomSamplesFE
						.getIntValue());
				// }
			}
		}

		return bReturn;
	}

}