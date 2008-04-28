package org.caleydo.core.view.swt.widget;

import org.eclipse.swt.widgets.Composite;

import org.caleydo.core.data.IUniqueObject;
import org.caleydo.core.view.swt.ISWTWidget;

/**
 * Base class for SWT views.
 * 
 * @author Marc Streit
 *
 */
public abstract class ASWTWidget 
implements IUniqueObject, ISWTWidget {
	
	/**
	 * Composite in which the content of the View should be placed.
	 */
	protected final Composite refParentComposite;
	
	protected int iUniqueId;
	
	/**
	 * Constructor that takes the composite in which it should 
	 * place the content.
	 * 
	 * @param refComposite Reference to the composite 
	 * that is supposed to be filled.
	 */
	protected ASWTWidget(Composite refParentComposite) {
		
		this.refParentComposite = refParentComposite;
	}
	
	public final void setId(int iUniqueId) {
		
		this.iUniqueId = iUniqueId;
	}

	public final int getId() {
		
		return iUniqueId;
	}
}
