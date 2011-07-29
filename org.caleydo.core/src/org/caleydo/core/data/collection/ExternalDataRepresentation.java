package org.caleydo.core.data.collection;

/**
 * Enum that is used to signal the external data representation of a set, namely what the normalized data
 * refers to. This influences the visualization of the data. The raw data is not changed
 * 
 * @author Alexander Lex
 */

public enum ExternalDataRepresentation {
	NORMAL,
	LOG10,
	LOG2,
	FOLD_CHANGE;
}