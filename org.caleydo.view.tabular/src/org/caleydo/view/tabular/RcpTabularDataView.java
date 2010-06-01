package org.caleydo.view.tabular;

import org.caleydo.core.manager.ISetBasedDataDomain;
import org.caleydo.core.manager.datadomain.DataDomainManager;
import org.caleydo.core.manager.general.GeneralManager;
import org.caleydo.rcp.view.rcp.CaleydoRCPViewPart;
import org.eclipse.swt.widgets.Composite;

public class RcpTabularDataView extends CaleydoRCPViewPart {

	private TabularDataView tabularDataView;

	@Override
	public void createPartControl(Composite parent) {
		tabularDataView = (TabularDataView) GeneralManager.get().getViewGLCanvasManager()
				.createView("org.caleydo.view.tabular", -1, "Tabular Data View");

		tabularDataView.setDataDomain((ISetBasedDataDomain) DataDomainManager
				.getInstance().getDataDomain(dataDomainType));
		tabularDataView.initViewRCP(parent);
		tabularDataView.drawView();

		parentComposite = parent;

		GeneralManager.get().getViewGLCanvasManager().registerItem(tabularDataView);
		view = tabularDataView;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		super.dispose();
		tabularDataView.unregisterEventListeners();
		GeneralManager.get().getViewGLCanvasManager().unregisterItem(
				tabularDataView.getID());
	}

	public TabularDataView getTabularDataView() {
		return tabularDataView;
	}
}
