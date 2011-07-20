package org.caleydo.core.data.collection.storage;

/**
 * Describes of what kind a container is, independent of its data type. Examples are raw data, normalized data
 * etc. Raw data can e.g. be primitive float, int, double or (numerical) objects, or even nominal data.
 * 
 * @author Alexander Lex
 */
public enum EDataRepresentation {
	RAW,
	LOG10,
	LOG2,
	NORMALIZED,
	
	FOLD_CHANGE_RAW,
	FOLD_CHANGE_NORMALIZED,
	
	UNCERTAINTY_RAW,
	UNCERTAINTY_NORMALIZED;
}
