package org.caleydo.core.data.collection;

import java.util.Hashtable;

import org.caleydo.core.data.collection.thread.ICollectionThreadObject;
import org.caleydo.core.data.xml.IMementoNetEventXML;

/**
 * @author Michael Kalkusch
 * @author Alexander Lex
 */
public interface IStorage 
extends ICollection, IMementoNetEventXML, ICollectionThreadObject {

	/**
	 * Adds a new container with the storage type defined in setStorageType.
	 * Returns the index of the new container.
	 * 
	 * Note: allocate() must be called to make the change permanent.
	 * 
	 * @param setStorageType define the new storage type
	 * @return new index of the storage
	 */
	public int addStorageTypePerContainer( StorageType setStorageType );
	
	/**
	 * Sets the storage type for a container.
	 * Note: allocate() must be called to make the change permanent.
	 * 
	 * Some implementations use only one contaier, while others use several
	 * container.
	 * 
	 * @param setStorageType new storage type for the container.
	 */	
	public void setStorageTypePerContainer( StorageType setStorageType,
			final int iAtContainerPosition );

	
//	/**
//	 * Get the storage type of one container 
//	 * 
//	 * @param iAtContainerPosition address the container
//	 * @return storage type of the container.
//	 */
//	public StorageType getStorageTypePerContainer( 
//			final int iAtContainerPosition );
	
	/**
	 * Removes a container immediately.
	 * 
	 * @param iAtContainerPosition 
	 */
	public void removeStorage( final StorageType byStorageType );
	
	/**
	 * Allocates all arrays.
	 *  If the size of the array was change the content of the array is lost.
	 * 
	 * @return
	 */
	public boolean allocate();
	
//	/**
//	 * ISet size of all containers.
//	 * Note: allocate() must be called to make the change permanent.
//	 * 
//	 * @param size
//	 */
//	public void setAllSize( final int [] size );
	
	/**
	 * ISet size of all containers.
	 * Note: allocate() must be called to make the change permanent.
	 * 
	 * @see org.caleydo.core.data.collection.IStorage#getMaximumLengthOfAllArrays()
	 * 
	 * @param size
	 */
	public Hashtable <StorageType,Integer> getAllSize();
	
	/**
	 * Sets size for one container.
	 * Note: allocate() must be called to make the change permanent.
	 * 
	 * @param iAtContainerPosition address a container. range [0.. getNumberArrays()-1 ]
	 * @param iSetSize new size of the container (array)
	 * @return TRUE if all was fine
	 */
	public void setSize( final StorageType byStorageType, final int iSetSize);
	
	/**
	 * Get size for one container.
	 * 
	 * @param iAtContainerPosition address a container. range [0.. getNumberArrays()-1 ]
	 * @return number of allocated elements in container at position iAtContainerPosition
	 */
	public int getSize( final StorageType type );
	
//	/**
//	 * Get each size of each container (array)
//	 * @return
//	 */
//	public int[] getAllSize();
	
	/**
	 * Return the number of hosted containers (arrays).
	 *  
	 * @return number of container (arrays) hosted
	 */
	public int getNumberArrays();
	
	/**
	 * Get the size of the largest array in the storage.
	 * 
	 * @see org.caleydo.core.data.collection.IStorage#getAllSize()
	 * 
	 * @return size of largest array
	 */
	public int getMaximumLengthOfAllArrays();
	
	//-----------------------------------
	
	// FIXME: No setters for [][], Object and Boolean
	
	// Int
	
	public void setArrayInt( int[] set );
	
	public int[] getArrayInt();
	
	public int getMinInt();
	
	public int getMaxInt();
	
	public int[][] getArray2DInt();	
	
	
    // Float
	
	public void setArrayFloat( float[] set );
	
	public float[] getArrayFloat();

	public float getMinFloat();
	
	public float getMaxFloat();
	
	public float[][] getArray2DFloat();
	
	// Double
	
	public void setArrayDouble( double[] set );
	
	public double[] getArrayDouble();
	
	public double getMinDouble();
	
	public double getMaxDouble();
	
	public double[][] getArray2DDouble();
	
	// String
	
	public void setArrayString( String[] set );
		
	public String[] getArrayString();
	
	public String[][] getArray2DString();
	
	// Object
	
	public Object[] getArrayObject();
	
	public Object[][] getArray2DObject();
	
	// Boolean
	
	public boolean[] getArrayBoolean();

	public boolean[][] getArray2DBoolean();
	
}
