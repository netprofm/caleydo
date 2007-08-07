package cerberus.view.gui.opengl.canvas.pathway;

import gleem.linalg.Transform;

import java.util.HashMap;
import java.util.LinkedList;


public class JukeboxHierarchyLayer {
	
	private HashMap<Integer, Transform> hashElementPositionIndexToTransform;
	
	/**
	 * Index in list determines position in stack
	 */
	private LinkedList<Integer> llElementId;
	
	private LinkedList<Integer> llElementIdImportanceQueue;
	
	private LinkedList<Boolean> llElementIdVisibleState;
	
	private JukeboxHierarchyLayer parentLayer;
	private JukeboxHierarchyLayer childLayer;
	
	private int iCapacity = 0;
	
	public JukeboxHierarchyLayer(int iCapacity) {
		
		hashElementPositionIndexToTransform = new HashMap<Integer, Transform>();
		llElementId = new LinkedList<Integer>();
		llElementIdImportanceQueue = new LinkedList<Integer>();
		llElementIdVisibleState = new LinkedList<Boolean>();
		this.iCapacity = iCapacity;
	}
	
	public void setParentLayer(final JukeboxHierarchyLayer parentLayer) {
		
		this.parentLayer = parentLayer;
	}

	public final JukeboxHierarchyLayer getParentLayer() {
		
		return parentLayer;
	}
	
	public void setChildLayer(final JukeboxHierarchyLayer childLayer) {
		
		this.childLayer = childLayer;
	}

	public final JukeboxHierarchyLayer getChildLayer() {
		
		return childLayer;
	}
	
	/**
	 * Add the element permanently to that layer.
	 * 
	 * @param iElementId
	 */
	public void addElement(int iElementId) {
		
		// Check if element limit is reached
		if (llElementId.size() >= iCapacity)
		{
			// Find and remove least important element
			int iLeastImportantElementId = llElementIdImportanceQueue.removeLast();

			int iReplacePosition = llElementId.indexOf(iLeastImportantElementId);
			llElementId.set(iReplacePosition, iElementId);
			llElementIdVisibleState.set(iReplacePosition, false);
			
			// FIFO replace strategy
			llElementIdImportanceQueue.addFirst(iElementId);	
		}
		else
		{
			// Add to the end of the stack (because there is free space)
			llElementId.addLast(iElementId);
			llElementIdVisibleState.addLast(false);
			llElementIdImportanceQueue.addFirst(iElementId);
		}
	}

//	public void setElement(int iPosIndex, int iElementId) {
//		
//		if (llElementId.size() <= iPosIndex)
//			addElement(iElementId);
//		else
//			llElementId.set(iPosIndex, iElementId);
//	}
	
	public int getElementIdByPositionIndex(int iPosIndex) {
		
		return llElementId.get(iPosIndex);
	}
	
	public LinkedList<Integer> getElementList() {
		
		return llElementId;
	}
	
	public void setTransformByPositionIndex(int iPosIndex, Transform transform) {
		
		hashElementPositionIndexToTransform.put(iPosIndex, transform);
	}
	
	public void removeElement(int iElementId) {
		
		llElementId.remove((Integer)iElementId);
		llElementIdImportanceQueue.remove((Integer)iElementId);
	}
	
	public boolean containsElement(int iElementId) {
		
		return llElementId.contains(iElementId);
	}
	
	public final Transform getTransformByElementId(int iElementId) {
		
		return hashElementPositionIndexToTransform.get(getPositionIndexByElementId(iElementId));
	}
	
	public final Transform getTransformByPositionIndex(int iPosIndex) {
		
		return hashElementPositionIndexToTransform.get(iPosIndex);
	}
	
	public final int getPositionIndexByElementId(int iElementId) {
		
		return llElementId.indexOf(iElementId);
	}
	
	public final int getNextPositionIndex() {
		
		// Check if a spot is free
		if (llElementId.size() < iCapacity)
		{	
			// Append to the end of the list
			return llElementId.size(); 
		}
		else 
		{	
			// Get index of least important element for replacement
			return llElementId.indexOf(llElementIdImportanceQueue.getLast());
		}
	}
	
	public void setElementVisibilityById(final boolean bVisibility,
			final int iElementId) {
		
//		if (!llElementId.contains(iElementId))
//			return;
			
		llElementIdVisibleState.set(llElementId.indexOf(iElementId), bVisibility);
	}
	
	public final boolean getElementVisibilityById(final int iElementId) {

//		if (!llElementId.contains(iElementId))
//			return true;
		
		return llElementIdVisibleState.get(llElementId.indexOf(iElementId));
	}
}
