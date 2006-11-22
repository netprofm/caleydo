
package cerberus.manager.event.mediator;

import cerberus.data.collection.ISet;

/**
 * Object that shall receive an event.
 * 
 * @author Michael Kalkusch
 * @author Marc Streit
 * 	
 */
public interface IMediatorReceiver {
	
	/**
	 * Update called by Mediator triggered by IMediatorSender.
	 * 
	 * @param eventTrigger Calling object, that created the update
	 */
	public void update(Object eventTrigger);	
	
	public void updateSelection(Object eventTrigger, 
			ISet updatedSelectionSet);
	
	//public void updateViewingData(Object eventTrigger, ***);
}
