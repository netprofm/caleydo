package org.caleydo.core.data.collection.storage;

import org.caleydo.core.data.collection.EExternalDataRepresentation;
import org.caleydo.core.data.collection.Histogram;
import org.caleydo.core.data.collection.ccontainer.FloatCContainer;
import org.caleydo.core.data.collection.ccontainer.FloatCContainerIterator;
import org.caleydo.core.data.collection.ccontainer.ICContainer;
import org.caleydo.core.data.collection.ccontainer.INumericalCContainer;
import org.caleydo.core.data.id.ManagedObjectType;
import org.caleydo.core.data.virtualarray.ContentVirtualArray;
import org.caleydo.core.manager.GeneralManager;

/**
 * INumericalStorage is a specialization of IStorage. It is meant for numerical data of a continuous range,
 * equivalent to the set of real numbers. In terms of scales it can be interpreted as a data structure for an
 * absolute scale. As a consequence raw data for a numerical set can only be of a number format, such as int
 * or float
 * 
 * @author Alexander Lex
 */

public class NumericalStorage
	extends AStorage {

	/**
	 * Constructor
	 */
	public NumericalStorage() {
		super(GeneralManager.get().getIDCreator().createID(ManagedObjectType.STORAGE_NUMERICAL));
	}

	@Override
	public void normalize() {

		INumericalCContainer iRawContainer = (INumericalCContainer) hashCContainers.get(dataRep);
		hashCContainers.put(EDataRepresentation.NORMALIZED, iRawContainer.normalize());
	}

	public void normalizeUncertainty(float invalidThreshold, float validThreshold) {

		FloatCContainer certainties =
			(FloatCContainer) hashCContainers.get(EDataRepresentation.UNCERTAINTY_RAW);
		FloatCContainer normalizedCertainties =
			certainties.normalizeWithExternalExtrema(invalidThreshold, validThreshold);
		hashCContainers.put(EDataRepresentation.UNCERTAINTY_NORMALIZED, normalizedCertainties);
	}

	/**
	 * Same as {@link #normalizeWithExternalExtrema(double, double)}, but with an additional parameter letting
	 * you specify the source of the normalization
	 * 
	 * @param sourceRep
	 * @param dMin
	 * @param dMax
	 */
	public void normalizeWithExternalExtrema(EDataRepresentation sourceRep, EDataRepresentation targetRep,
		double dMin, double dMax) {
		INumericalCContainer rawStorage = (INumericalCContainer) hashCContainers.get(sourceRep);

		INumericalCContainer numericalContainer = rawStorage.normalizeWithExternalExtrema(dMin, dMax);

		hashCContainers.put(targetRep, numericalContainer);
	}

	/**
	 * <p>
	 * If you want to consider extremas for normalization which do not occur in this storage (e.g., because
	 * the global extremas for the DataTable are used), use this method instead of normalize().
	 * </p>
	 * Values that are bigger or smaller then the extrema specified are set to 0 (minimum) or 1 (maximum) in
	 * the normalized data. The raw data is untouched. Therefore elements with values 0 or one can have
	 * different raw values associated.
	 * <p>
	 * Normalize operates on the raw data, except if you previously called log, then the logarithmized data is
	 * used.
	 * 
	 * @param dMin
	 *            the minimum
	 * @param dMax
	 *            the maximum
	 * @throws IlleagalAttributeStateException
	 *             if dMin >= dMax
	 */
	public void normalizeWithExternalExtrema(double dMin, double dMax) {
		normalizeWithExternalExtrema(dataRep, EDataRepresentation.NORMALIZED, dMin, dMax);
	}

	@Override
	public ERawDataType getRawDataType() {
		return rawDataType;
	}

	/**
	 * Get the minimum of the raw data, respectively the logarithmized data if log was applied
	 * 
	 * @return the minimum - a double since it can contain all values
	 */
	public double getMin() {
		if (!hashCContainers.containsKey(dataRep))
			throw new IllegalStateException("The requested data representation was not produced.");
		return ((INumericalCContainer) hashCContainers.get(dataRep)).getMin();
	}

	/**
	 * Get the maximum of the raw data, respectively the logarithmized data if log was applied
	 * 
	 * @return the maximum - a double since it can contain all values
	 */
	public double getMax() {
		return ((INumericalCContainer) hashCContainers.get(dataRep)).getMax();
	}

	/**
	 * Calculates a raw value based on min and max from a normalized value.
	 * 
	 * @param dNormalized
	 *            a value between 0 and 1
	 * @return a value between min and max
	 */
	public double getRawForNormalized(double dNormalized) {
		return dNormalized * (getMax() - getMin());
	}

	/**
	 * Calculates the log10 of the raw data. Log data can be retrieved by using the get methods with
	 * EDataRepresentation.LOG10. Call normalize after this operation if you want to display the result
	 * Normalize then uses the log data instead of the raw data
	 */
	public void log10() {
		hashCContainers.put(EDataRepresentation.LOG10,
			((INumericalCContainer) hashCContainers.get(EDataRepresentation.RAW)).log(10));
	}

	/**
	 * Calculates the log2 of the raw data. Log data can be retrieved by using the get methods with
	 * EDataRepresentation.LOG10. Call normalize after this operation if you want to display the result
	 * Normalize then uses the log data instead of the raw data
	 */
	public void log2() {
		hashCContainers.put(EDataRepresentation.LOG2,
			((INumericalCContainer) hashCContainers.get(EDataRepresentation.RAW)).log(2));
	}

	/**
	 * Remove log and normalized data. Normalize has to be called again.
	 */
	public void reset() {
		hashCContainers.remove(EDataRepresentation.LOG2);
		hashCContainers.remove(EDataRepresentation.LOG10);
		hashCContainers.remove(EDataRepresentation.NORMALIZED);
	}

	@Override
	public void setExternalDataRepresentation(EExternalDataRepresentation externalDataRep) {
		switch (externalDataRep) {
			case NORMAL:
				dataRep = EDataRepresentation.RAW;
				break;
			case LOG10:
				dataRep = EDataRepresentation.LOG10;
				break;
			case LOG2:
				dataRep = EDataRepresentation.LOG2;
				break;
		}

	}

	/**
	 * Returns a histogram of the values in the storage for all values (not considering VAs). The number of
	 * the bins is sqrt(numberOfElements)
	 * 
	 * @return
	 */
	public Histogram getHistogram() {

		int iNumberOfBuckets = (int) Math.sqrt(size());
		Histogram histogram = new Histogram(iNumberOfBuckets);
		for (int iCount = 0; iCount < iNumberOfBuckets; iCount++) {
			histogram.add(0);
		}

		FloatCContainerIterator iterator =
			((FloatCContainer) hashCContainers.get(EDataRepresentation.NORMALIZED)).iterator();
		while (iterator.hasNext()) {
			// this works because the values in the container are already noramlized
			int iIndex = (int) (iterator.next() * iNumberOfBuckets);
			if (iIndex == iNumberOfBuckets)
				iIndex--;
			Integer iNumOccurences = histogram.get(iIndex);
			histogram.set(iIndex, ++iNumOccurences);
		}

		return histogram;
	}

	/**
	 * Returns a histogram of the values in the storage for all values considering the specified VA. The
	 * number of the bins is sqrt(VA size)
	 * 
	 * @param contentVA
	 *            VA to consider for the histogram
	 * @return
	 */
	public Histogram getHistogram(ContentVirtualArray contentVA) {

		int iNumberOfBuckets = (int) Math.sqrt(contentVA.size());
		Histogram histogram = new Histogram(iNumberOfBuckets);
		for (int iCount = 0; iCount < iNumberOfBuckets; iCount++) {
			histogram.add(0);
		}

		FloatCContainerIterator iterator =
			((FloatCContainer) hashCContainers.get(EDataRepresentation.NORMALIZED)).iterator(contentVA);
		while (iterator.hasNext()) {
			// this works because the values in the container are already noramlized
			int iIndex = (int) (iterator.next() * iNumberOfBuckets);
			if (iIndex == iNumberOfBuckets)
				iIndex--;
			Integer iNumOccurences = histogram.get(iIndex);
			histogram.set(iIndex, ++iNumOccurences);
		}

		return histogram;
	}

	/**
	 * Creates an empty container for the given {@link EDataRepresentation} and stores it
	 * 
	 * @param dataRepresentation
	 */
	public void setNewRepresentation(EDataRepresentation dataRepresentation, float[] representation) {
		if (representation.length != size())
			throw new IllegalArgumentException("The size of the storage (" + size()
				+ ") is not equal the size of the given new representation (" + representation.length + ")");
		if (hashCContainers.containsKey(dataRepresentation))
			throw new IllegalStateException("The data representation " + dataRepresentation
				+ " already exists in " + this);
		ICContainer container = new FloatCContainer(representation);
		hashCContainers.put(dataRepresentation, container);
	}

}
