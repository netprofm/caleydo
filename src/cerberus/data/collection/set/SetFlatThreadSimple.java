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

import cerberus.manager.GeneralManager;
import cerberus.manager.type.ManagerObjectType;

import cerberus.data.collection.IMetaData;
import cerberus.data.collection.ISelection;
import cerberus.data.collection.IStorage;
import cerberus.data.collection.ISet;
import cerberus.data.collection.parser.CollectionSetParseSaxHandler;
import cerberus.data.collection.selection.iterator.SelectionVectorIterator;
import cerberus.xml.parser.DParseSaxHandler;
import cerberus.data.collection.selection.iterator.SelectionIterator;
import cerberus.data.collection.thread.impl.CollectionThreadItem;
import cerberus.data.collection.thread.lock.CollectionLock;

/**
 * @author Michael Kalkusch
 *
 */
public class SetFlatThreadSimple 
extends CollectionThreadItem
implements ISet {

	/**
	 * Since only one Selections is stored only one 
	 * MetaData object is needed. 
	 */
	protected IMetaData refMetaDataAllAndAny = null;
	
	/**
	 * Store reference to the ISelection.
	 */
	protected ISelection[] refFlatSelection = null;
	
	/**
	 * Store reference to the Storages.
	 */
	protected IStorage[] refFlatStorage = null;
	
	/**
	 * Variable for the dimension of this set.
	 */
	protected int iSizeDimension = 0;

	/**
	 * 
	 */
	public SetFlatThreadSimple( int iSetCollectionId, GeneralManager setGeneralManager,
			CollectionLock setCollectionLock) {

		super( iSetCollectionId, setGeneralManager, setCollectionLock );
		
		refFlatSelection = new ISelection[1];
		
		refFlatStorage = new IStorage[1];
	}


	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean setSelectionByDim(ISelection[] addSelection, int iAtDimension) {		
		
		if ( iAtDimension >= refFlatSelection.length) {
			assert false :"Can not address dimension != 0";
		}
		//FIXME Test Range!
		refFlatSelection[iAtDimension] = addSelection[0];
		
		return true;
	}
	
	public boolean setSelectionByDimAndIndex( final ISelection addSelection, 
			final int iAtDimension, 
			final int iAtIndex ) {
		
		if ( iAtDimension >= refFlatSelection.length) {
			assert false :"Can not address dimension != 0";
		}
		
		if ( refFlatSelection.length < iAtIndex) {
			/* create a new ISelection[] and copy to new ISelection[]... */
			ISelection[] copyToNewSelectionArray = new ISelection[iAtIndex+1];
			
			for ( int i=0; i < refFlatSelection.length; i++ ) {				
				copyToNewSelectionArray[i] = refFlatSelection[i];
			}
			refFlatSelection = copyToNewSelectionArray;
		}
		
		refFlatSelection[iAtIndex] = addSelection;
		
		return true;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setStorageByDimAndIndex(cerberus.data.collection.IStorage, int, int)
	 */
	public boolean setStorageByDimAndIndex( final IStorage addStorage, 
			final int iAtDimension, 
			final int iAtIndex ) {
		
		if ( iAtDimension >= refFlatStorage.length) {
			assert false :"Can not address dimension != 0";
		}
		
		if ( refFlatStorage.length < iAtIndex) {
			/* create a new ISelection[] and copy to new ISelection[]... */
			IStorage[] copyToNewSelectionArray = new IStorage[iAtIndex+1];
			
			for ( int i=0; i < refFlatStorage.length; i++ ) {				
				copyToNewSelectionArray[i] = refFlatStorage[i];
			}
			refFlatStorage = copyToNewSelectionArray;
		}
		
		refFlatStorage[iAtIndex] = addStorage;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#removeSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean removeSelection( final ISelection[] removeSelection, final int iFromDimension) {
		
		for ( int i=0; i< removeSelection.length; i++ ) {
			if ( refFlatSelection[iFromDimension] == removeSelection[i] ) {
				refFlatSelection[iFromDimension] = null;
				return true;
			}
		}
		return false;		
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelection(cerberus.data.collection.ISelection, int)
	 */
	public boolean hasSelection(ISelection testSelection, int iAtDimension) {
		
		assert testSelection != null: "SetFlatSimple.hasSelection() test with null pointer!";
		
		//FIXME add range check...
		if ( refFlatSelection[iAtDimension] ==  testSelection ) {
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#hasSelectionInSet(cerberus.data.collection.ISelection)
	 */
	public boolean hasSelectionInSet(ISelection testSelection) {
		
		//FIXME return index instead of true or false
		
		for ( int iIndex=0; iIndex < this.refFlatSelection.length ; iIndex++ ) {
			if ( refFlatSelection[iIndex] ==  testSelection)
				return true;
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
		//FIXME what shall that function do?
		assert refFlatSelection != null : "no ISelection[] is set";
		assert refFlatSelection.length > 0 : "no ISelection is set";
		
		return refFlatSelection[0].length();
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getDimensions()
	 */
	public int getDimensions() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setDimensionSize(int, int)
	 */
	public void setDimensionSize(int iIndexDimension, int iValueDimensionSize) {
		//FIXME what shall that function do?
		iSizeDimension = iValueDimensionSize;
	}





	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelection(int)
	 */
	public ISelection[] getSelectionByDim(int iAtDimension) {
		//FIXME add range check..
		return refFlatSelection;
	}

	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getSelectionByDimAndIndex(int, int)
	 */
	public ISelection getSelectionByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		return refFlatSelection[iAtIndex];
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
		
		try {
			CollectionSetParseSaxHandler parser = 
				(CollectionSetParseSaxHandler) refSaxHandler;
			
			if ( parser.getDim() < 0 ) {
				assert false:"Parser does not return informations neede";
				return false;
			}
			/**
			 * Stores the a list of Selections and Storages used by this set.
			 */
			final int[] iListOfSellectionId = parser.getSelectByDim( 0 );
			final int[] iListOfStorageId = parser.getStorageByDim( 0 );
			
			
			/**
			 * Store reference to the Selections.
			 */
			refFlatSelection = new ISelection[iListOfSellectionId.length];
			
			for ( int iIndex=0; iIndex< iListOfSellectionId.length ; iIndex++ ) {
				
				try {					
					Object buffer = getManager().getItem( iListOfSellectionId[iIndex] );
					refFlatSelection[iIndex] = (ISelection) buffer;
				}
				catch ( NullPointerException npe) {
					npe.printStackTrace();
					throw npe; 
				}
			}
			
			/**
			 * Store reference to the Storages.
			 */
			refFlatStorage = new IStorage[iListOfStorageId.length];
			
			for ( int iIndex=0; iIndex< iListOfStorageId.length ; iIndex++ ) {
				
				try {					
					Object buffer = getManager().getItem( iListOfStorageId[iIndex] );
					refFlatStorage[iIndex] = (IStorage) buffer;
				}
				catch ( NullPointerException npe) {
					npe.printStackTrace();
					throw npe; 
				}
			}
			
			getManager().unregisterItem( getId(), 
					ManagerObjectType.SET_LINEAR );
			
			getManager().registerItem( this, 
					parser.getXML_DataComponent_Id(), 
					ManagerObjectType.SET_LINEAR );
			
			setId( parser.getXML_DataComponent_Id() );		
			
			return true;
		}
		catch (NullPointerException npe) {
			return false;
		}
		
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
		return ManagerObjectType.SET_LINEAR;
	}
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#getStorage()
	 */
	public final IStorage[] getStorageByDim( final int iAtDimension ) {
		return refFlatStorage;
	}
	
	public final IStorage getStorageByDimAndIndex( final int iAtDimension, 
			final int iAtIndex ) {
		return refFlatStorage[iAtIndex];
	}
	
	
	/* (non-Javadoc)
	 * @see cerberus.data.collection.ISet#setStorage(cerberus.data.collection.IStorage)
	 */
	public final void setStorageByDim(IStorage[] setStorage, final int iAtDimension ) {
		
		assert setStorage != null: "setStorage() with null-pointer";
		
		refFlatStorage[0] = setStorage[0];
	}
	

	public final boolean setSelectionByDim( Vector<ISelection> setSelection, final int iAtDimension ) {
		
		assert setSelection != null: "setStorage() with null-pointer";
		
		refFlatSelection = new ISelection[ setSelection.size() ];		
		Iterator <ISelection> iter = setSelection.iterator();
		int iIndex = 0;
		
		while ( iter.hasNext() ) {
			refFlatSelection[ iIndex ] = iter.next();
		}
		
		return true;
	}
	
	public final boolean setStorageByDim( Vector<IStorage> setStorage, final int iAtDimension ) {
		
		assert setStorage != null: "setStorage() with null-pointer";
		
		refFlatStorage = new IStorage[ setStorage.size() ];		
		Iterator <IStorage> iter = setStorage.iterator();
		int iIndex = 0;
		
		while ( iter.hasNext() ) {
			refFlatStorage[ iIndex ] = iter.next();
		}
		
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
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ICollection#getCacheId()
	 */
	public int getCacheId() {
		
		if ( this.refFlatSelection != null ) {
			for ( int i=0; i<refFlatSelection.length;i++) {
				setCacheIdCompared( refFlatSelection[i].getCacheId());
			}
		}
		
		if ( this.refFlatStorage != null ) {
			for ( int i=0; i<refFlatStorage.length;i++) {
				setCacheIdCompared( refFlatStorage[i].getCacheId());
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
	 * @see cerberus.data.collection.ISet#iteratorStorageByDim(int)
	 */
	public Iterator<IStorage> iteratorStorageByDim( final int iAtDimension ) {
		
		Vector<IStorage> vec_Storage = new Vector<IStorage> (refFlatStorage.length);
		
		for ( int i=0; i <refFlatStorage.length; i++ ) {					
			vec_Storage.addElement( refFlatStorage[i] );
		}
		
		return vec_Storage.iterator();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see cerberus.data.collection.ISet#iteratorSelectionByDim(int)
	 */
	public SelectionIterator iteratorSelectionByDim( final int iAtDimension ) {
		Vector<ISelection> vec_Selection = 
			new Vector<ISelection> (refFlatSelection.length);
		
		for ( int i=0; i <refFlatSelection.length; i++ ) {					
			vec_Selection.addElement( refFlatSelection[i] );
		}
		
		SelectionVectorIterator iterator = new SelectionVectorIterator();
		iterator.addSelectionVector( vec_Selection );
		
		return iterator;
	}
	
	public final Vector<IStorage> getStorageVectorByDim( final int iAtDimension ) {
		
		Vector<IStorage> resultVector = new Vector<IStorage> (refFlatStorage.length);
		
		for ( int i=0; i < refFlatStorage.length; i++ ) {
			resultVector.addElement( refFlatStorage[i] );
		}
		
		return resultVector;
	}
	
	public final Vector<ISelection> getSelectionVectorByDim( final int iAtDimension ) {
		
		Vector<ISelection> resultVector = new Vector<ISelection> (refFlatSelection.length);
		
		for ( int i=0; i < refFlatSelection.length; i++ ) {
			resultVector.addElement( refFlatSelection[i] );
		}
		
		return resultVector;
	}

}
