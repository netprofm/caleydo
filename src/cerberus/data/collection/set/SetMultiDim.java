/*
 * Project: GenView
 * 
 * Author: Michael Kalkusch
 * 
 *  creation date: 18-05-2005
 *  
 */
package cerberus.data.collection.set;

import java.util.Iterator;
import java.util.Vector;
//import java.util.Iterator;

import cerberus.manager.GeneralManager;
import cerberus.manager.type.ManagerObjectType;

import cerberus.data.collection.IMetaData;
import cerberus.data.collection.ISelection;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.ISet;
//import cerberus.data.collection.parser.CollectionSetParseSaxHandler;
import cerberus.xml.parser.DParseSaxHandler;
import cerberus.data.collection.thread.impl.CollectionThreadItem;
import cerberus.data.collection.thread.lock.CollectionLock;
import cerberus.data.collection.selection.iterator.SelectionVectorIterator;
import cerberus.data.collection.selection.iterator.SelectionIterator;

/**
 * @author Michael Kalkusch
 *
 */
public class SetMultiDim 
extends CollectionThreadItem
implements ISet {

	/**
	 * Since only one Selections is stored only one 
	 * MetaData object is needed. 
	 */
	protected IMetaData refMetaDataAllAndAny = null;
	
	protected Vector< Vector<ISelection> > vecSelectionDim;
	
	protected Vector< Vector<IStorage> > vecStorageDim;
	
	/**
	 * Store reference to the ISelection.
	 */
	//protected ISelection[] refFlatSelection = null;
	
	/**
	 * Store reference to the Storages.
	 */
	//protected IStorage[] refFlatStorage = null;
	
	/**
	 * Variable for the dimension of this set.
	 */
	protected int iSizeDimension = 0;

	/**
	 * 
	 */
	public SetMultiDim( int iSetCollectionId, 
			GeneralManager setGeneralManager,
			CollectionLock setCollectionLock,
			final int iSetDimension ) {

		super( iSetCollectionId, setGeneralManager, setCollectionLock );
		
		iSizeDimension = iSetDimension;
		
		vecSelectionDim = new Vector< Vector<ISelection> > (iSetDimension);
		vecStorageDim = new Vector< Vector<IStorage> > (iSetDimension);
		
		for (int i=0; i<iSetDimension; i++) {
			vecSelectionDim.addElement( new Vector<ISelection> (2) );
			vecStorageDim.addElement( new Vector<IStorage> (2) );
		}
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean setSelectionByDim(ISelection[] addSelection, int iAtDimension) {		
		
		assert addSelection != null: "setStorage() with null-pointer";
		
		Vector <ISelection> bufferInsertVector = 
			new Vector <ISelection> (addSelection.length);
		
		for ( int i=0; i < addSelection.length; i++ ) {
			bufferInsertVector.addElement( addSelection[i] );
		}
		
		setSelectionByDim( bufferInsertVector , iAtDimension );
		
		return true;
	}
	
	public boolean setSelectionByDimAndIndex( final ISelection addSelection, 
			final int iAtDimension, 
			final int iAtIndex ) {
		
		vecSelectionDim.get( iAtDimension ).setElementAt( addSelection, iAtIndex );
		
		return true;
	}
	
	public boolean addSelectionByDim( final ISelection addSelection, 
			final int iAtDimension ) {
		
		vecSelectionDim.get( iAtDimension ).addElement( addSelection );
		
		return true;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setStorageByDimAndIndex(cerberus.data.collection.IStorage, int, int)
	 */
	public boolean setStorageByDimAndIndex( final IStorage addStorage, 
			final int iAtDimension, 
			final int iAtIndex ) {
		
		vecStorageDim.get( iAtDimension ).setElementAt( addStorage, iAtIndex );
		
		return true;
	}
	
	public boolean addStorageByDim( final IStorage addStorage, 
			final int iAtDimension ) {
		
		Vector <IStorage> buffer = vecStorageDim.get( iAtDimension );
		
		buffer.addElement( addStorage );
		
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#removeSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean removeSelection( final ISelection removeSelection, final int iFromDimension) {
		
		Vector <ISelection> bufferVectorSelection = vecSelectionDim.get( iFromDimension );
		
		return bufferVectorSelection.removeElement( removeSelection );		
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean hasSelection(ISelection testSelection, int iAtDimension) {
		
		assert testSelection != null: "SetFlatSimple.hasSelection() test with null pointer!";
		
		return vecSelectionDim.get( iAtDimension ).contains( testSelection );	
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelectionInSet(cerberus.data.collection.ISelection)
	 */
	public boolean hasSelectionInSet(ISelection testSelection) {
		
		Iterator <Vector <ISelection> > iterSelection = vecSelectionDim.iterator();
		
		while ( iterSelection.hasNext() ) {		
						
			Vector <ISelection> vecInnerSelection = iterSelection.next();
			
			if ( vecInnerSelection.contains( testSelection ) ) {
				return true;
			}
		
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensionSizeForAllSelections()
	 */
	public int[] getDimensionSizeForAllSelections() {
		
		//FIXME what shall that function do?
		return null;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensionSize(int)
	 */
	public int getDimensionSize(int iAtDimension) {
		
		Iterator <ISelection> iter = 
			vecSelectionDim.get( iAtDimension ).iterator();
		
		int iLength = 0;
		while ( iter.hasNext() ) {
			iLength += iter.next().length();
		}
		
		return iLength;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensions()
	 */
	public int getDimensions() {
		return this.vecSelectionDim.size();
	}

//	/* (non-Javadoc)
//	 * @see cerberus.data.collection.ISet#setDimensionSize(int, int)
//	 */
//	public void setDimensionSize(int iIndexDimension, int iValueDimensionSize) {
//		//FIXME what shall that function do?
//		iSizeDimension = iValueDimensionSize;
//	}





	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelection(int)
	 */
	public ISelection[] getSelectionByDim(int iAtDimension) {
		
		Vector <ISelection> buffer = vecSelectionDim.get( iAtDimension );
		Iterator <ISelection> iter = buffer.iterator();
		
		ISelection[] resultBuffer = new ISelection[buffer.size()];
		
		for ( int i=0; iter.hasNext(); i++ ) {
			resultBuffer[i] = iter.next();
		}
		return resultBuffer;
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelectionByDimAndIndex(int, int)
	 */
	public ISelection getSelectionByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		
		return this.vecSelectionDim.get(iAtDimension).get(iAtIndex);
	}
	


	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @param setMetaData sets the meta data
	 * @see cerberus.data.collection.IMetaDataCollection#setMetaData(cerberus.data.collection.IMetaData)
	 * 
	 */
	public void setMetaData(IMetaData setMetaData) {
		
		assert setMetaData != null :"setMetaData() with null-pointer.";
		
		refMetaDataAllAndAny = setMetaData;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.IMetaDataCollection#getMetaData()
	 */
	public IMetaData getMetaData() {
		return refMetaDataAllAndAny;
	}
	
	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @return curretn meta data
	 * 
	 * @see cerberus.data.collection.IMetaDataSet#getMetaData()
	 */
	public IMetaData getMetaDataAny() {
		return refMetaDataAllAndAny;
	}

	/**
	 * Since only one selection is stored only one MetaData obejct is used.
	 * 
	 * @param sets the meta data
	 * 
	 * @see cerberus.data.collection.IMetaDataSet#setMetaData(cerberus.data.collection.IMetaData)
	 */
	public void setMetaDataAny(IMetaData setMetaData) {
		setMetaData( setMetaData );
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#getSubSets()
	 */
	public ISet[] getSubSets() {
		
		assert false: "SetFlatSimple.getSubSets() SetFlatSimple does not supper ISubSet's.";
	
		return null;
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#hasSubSets()
	 */
	public boolean hasSubSets() {
		return false;
	}

	/**
	 * No subsets are available.
	 * 
	 * @see cerberus.data.collection.ISubSet#addSubSet(cerberus.data.collection.ISet)
	 */
	public boolean addSubSet(ISet addSet) {
		throw new RuntimeException("SetFlatSimple.addSubSet() SetFlatSimple does not supper ISubSet's.");
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISubSet#swapSubSet(cerberus.data.collection.ISet, cerberus.data.collection.ISet)
	 */
	public boolean swapSubSet(ISet fromSet, ISet toSet) {
		
		assert false: "SetFlatSimple.swapSubSet() SetFlatSimple does not supper ISubSet's.";
	
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISubSet#removeSubSet(cerberus.data.collection.ISet)
	 */
	public boolean removeSubSet(ISet addSet) {

		assert false: "SetFlatSimple.removeSubSet() SetFlatSimnple does not supper ISubSet's.";
	
		return false;
	}
	
	/**
	 * Restore state of object by update data from SaxHandler
	 * 
	 * @param refSaxHandler reference to SaxHandler
	 * @return TRUE if the provided handler provided same Id as object.
	 */
	public boolean setMementoXML_usingHandler( final DParseSaxHandler refSaxHandler ) {
		
		return false;
		
//		try {
//			CollectionSetParseSaxHandler parser = 
//				(CollectionSetParseSaxHandler) refSaxHandler;
//			
//			if ( parser.getDim() < 0 ) {
//				assert false:"Parser does not return informations neede";
//				return false;
//			}
//			/**
//			 * Stores the a list of Selections and Storages used by this set.
//			 */
//			final int[] iListOfSellectionId = parser.getSelectByDim( 0 );
//			final int[] iListOfStorageId = parser.getStorageByDim( 0 );
//			
//			
//			/**
//			 * Store reference to the Selections.
//			 */
//			refFlatSelection = new ISelection[iListOfSellectionId.length];
//			
//			for ( int iIndex=0; iIndex< iListOfSellectionId.length ; iIndex++ ) {
//				
//				try {					
//					Object buffer = getManager().getItem( iListOfSellectionId[iIndex] );
//					refFlatSelection[iIndex] = (ISelection) buffer;
//				}
//				catch ( NullPointerException npe) {
//					npe.printStackTrace();
//					throw npe; 
//				}
//			}
//			
//			/**
//			 * Store reference to the Storages.
//			 */
//			refFlatStorage = new IStorage[iListOfStorageId.length];
//			
//			for ( int iIndex=0; iIndex< iListOfStorageId.length ; iIndex++ ) {
//				
//				try {					
//					Object buffer = getManager().getItem( iListOfStorageId[iIndex] );
//					refFlatStorage[iIndex] = (IStorage) buffer;
//				}
//				catch ( NullPointerException npe) {
//					npe.printStackTrace();
//					throw npe; 
//				}
//			}
//			
//			getManager().unregisterItem( getId(), 
//					ManagerObjectType.SET_LINEAR );
//			
//			getManager().registerItem( this, 
//					parser.getXML_DataComponent_Id(), 
//					ManagerObjectType.SET_LINEAR );
//			
//			setId( parser.getXML_DataComponent_Id() );		
//			
//			return true;
//		}
//		catch (NullPointerException npe) {
//			return false;
//		}
		
	}
	
	/**
	 * @see cerberus.data.xml.MementoXML#createMementoXML()
	 * @return String containing all information on the state 
	 * of the object in XML form with out a header.
	 */
	public String createMementoXML() {
		
//		final String openDetail = "<DataComponentItemDetails type=\"";
//		final String closeDetail = "</DataComponentItemDetails>\n";
		
		//FIXME IMemento is not created yet!
		
		assert false:"IMemento of ISet is not created yet!";
		
		return createMementoXML_Intro(
				ManagerObjectType.SELECTION_SINGLE_BLOCK.name())
			+ "</DataComponentItem>\n";
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.UniqueManagedInterface#getBaseType()
	 */
	public ManagerObjectType getBaseType() {
		return ManagerObjectType.SET_MULTI_DIM;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getStorage()
	 */
	public final IStorage[] getStorageByDim( final int iAtDimension ) {
		return null;
	}
	
	public final Vector<IStorage> getStorageVectorByDim( final int iAtDimension ) {
		return vecStorageDim.get( iAtDimension );
	}
	
	public final Vector<ISelection> getSelectionVectorByDim( final int iAtDimension ) {
		return vecSelectionDim.get( iAtDimension );
	}
	

	
	public final Vector<ISelection> setSelectionVectorByDim( final int iAtDimension ) {
		return vecSelectionDim.get( iAtDimension );
	}
	
	/**
	 * Test is a certain index to address a IStorage is avlid.
	 * 
	 * @param iAtDimension Dimension of IStorage
	 * @param iAtIndex index inside Dimension
	 * 
	 * @return TRUE if a IStorage is present at this index.
	 */
	public final boolean hasStorageByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		if (( iAtDimension >= 0 )&&
				( iAtIndex >= 0 )&&
				( iAtDimension < vecStorageDim.size() )) {
			
			if ( iAtIndex < vecStorageDim.get(iAtDimension).size() ) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Test is a certain index to address a ISelection is avlid.
	 * 
	 * @param iAtDimension Dimension of ISelection
	 * @param iAtIndex index inside Dimension
	 * 
	 * @return TRUE if a ISelection is present at this index.
	 */
	public final boolean hasSelectionByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		if (( iAtDimension >= 0 )&&
				( iAtIndex >= 0 )&&
				( iAtDimension < vecSelectionDim.size() )) {
			
			if ( iAtIndex < vecSelectionDim.get(iAtDimension).size() ) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	public final IStorage getStorageByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		return vecStorageDim.get( iAtDimension ).get( iAtIndex );
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setStorage(cerberus.data.collection.IStorage)
	 */
	public final void setStorageByDim(IStorage[] setStorage, final int iAtDimension ) {
		
		assert setStorage != null: "setStorage() with null-pointer";
		
		Vector <IStorage> bufferInsertVector = new Vector <IStorage> (setStorage.length);
		
		for ( int i=0; i < setStorage.length; i++ ) {
			bufferInsertVector.addElement( setStorage[i] );
		}
		
		this.setStorageByDim( bufferInsertVector , iAtDimension );
	}
	
	public final boolean setStorageByDim( Vector <IStorage> setVecStorage, final int iAtDimension ) {
		
		assert setVecStorage != null: "setStorageVectorByDim() with null-pointer";
		
		this.vecStorageDim.setElementAt( setVecStorage, iAtDimension );

		return true;
	}
	
	public final boolean setSelectionByDim( Vector <ISelection> setVecSelection, final int iAtDimension ) {
		
		assert setVecSelection != null: "setStorageVectorByDim() with null-pointer";
		
		this.vecSelectionDim.setElementAt( setVecSelection, iAtDimension );

		return true;
	}
	
	/**
	 * Create "Header" for all Selections.
	 * 
	 * @see cerberus.data.xml.MementoXML#createMementoXML()
	 * 
	 * @return String containign the XML-header for this selection
	 */
	protected final String createMementoXML_Intro( 
			final String sSelectionType ) {		
		
		return "<DataComponentItem data_Id=\""
			+ getId() + 
			"\" type=\"" +
			sSelectionType + "\">\n";
	}
	
	/**
	 * Sets internal CacheId.
	 * 
	 * @see getCacheId()
	 * 
	 * @param iCompareAndSet cacheID to be compared with internal cacheId
	 */
	private void setInternalCacheId( final int iCompareAndSet ) {
		if ( iCacheId < iCompareAndSet ) {
			iCacheId = iCompareAndSet;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ICollection#getCacheId()
	 */
	public int getCacheId() {
		
		Iterator <Vector <ISelection>> iterSelection = vecSelectionDim.iterator();		
		while ( iterSelection.hasNext() ) {
			
			Iterator <ISelection> iterInnerSelect = iterSelection.next().iterator();
			
			while ( iterInnerSelect.hasNext() ) {
				setInternalCacheId( iterInnerSelect.next().getCacheId() );
			}
		}
		
		
		Iterator <Vector <IStorage>> iterStorage = vecStorageDim.iterator();
		while ( iterStorage.hasNext() ) {
			
			Iterator <IStorage> iterInnerStore = iterStorage.next().iterator();
			
			while ( iterInnerStore.hasNext() ) {
				setInternalCacheId( iterInnerStore.next().getCacheId() );
			}
		}
		
		return this.iCacheId;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.CollectionThreadObject#hasCacheChanged(int)
	 */
	public boolean hasCacheChanged( final int iCompareCacheId ) {
		return (iCompareCacheId < this.getCacheId());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasCacheChangedReadOnly(int)
	 */
	public final boolean hasCacheChangedReadOnly( final int iCompareCacheId ) {
		return (iCompareCacheId > this.iCacheId);
	}
	

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#iteratorSelection()
	 */
	public SelectionIterator iteratorSelectionByDim( final int iAtDimension ) {
		
//		Vector<ISelection> bufferVecSelection = 
//			vecSelectionDim.get( iAtDimension );
		
		SelectionVectorIterator iterator = 
			new SelectionVectorIterator();
		
		iterator.addSelectionVector( vecSelectionDim.get( iAtDimension ) );
		
		return iterator;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#iteratorStorage()
	 */
	public Iterator<IStorage> iteratorStorageByDim( final int iAtDimension ) {
			
		Vector<IStorage> vec_Storage = vecStorageDim.get( iAtDimension );
		
		return vec_Storage.iterator();
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#removeSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean removeSelection( final ISelection[] removeSelection, final int iFromDimension) {
		
		Vector <ISelection> bufferVecSelection = vecSelectionDim.get( iFromDimension );
		
		boolean bAllElementsRemoved = true;
		
		for ( int i=0; i< removeSelection.length; i++ ) {
			
			if ( ! bufferVecSelection.removeElement(  removeSelection[iFromDimension] )) {
				bAllElementsRemoved = false;
			}
		}
		
		return bAllElementsRemoved;		
	}
}
