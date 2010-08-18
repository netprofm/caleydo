package org.caleydo.rcp.view.rcp;

import org.caleydo.core.manager.GeneralManager;
import org.caleydo.core.view.swt.collab.CollabViewRep;
import org.eclipse.swt.widgets.Composite;

public class RcpCollabView
	extends CaleydoRCPViewPart {
	public static final String ID = "org.caleydo.rcp.views.swt.CollabView";

	private CollabViewRep testingView;

	@Override
	public void createPartControl(Composite parent) {
		testingView =
			(CollabViewRep) GeneralManager.get().getViewGLCanvasManager()
				.createView("org.caleydo.view.collab", -1, "Collaboration");

		testingView.initViewRCP(parent);
		testingView.drawView();

		parentComposite = parent;

		GeneralManager.get().getViewGLCanvasManager().registerItem(testingView);
		view = testingView;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		testingView.dispose();
	}

	public CollabViewRep getTestingView() {
		return testingView;
	}
}
