package org.caleydo.core.data.collection.table;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.id.IDType;
import org.caleydo.core.manager.datadomain.ATableBasedDataDomain;

/**
 * Parameters to load the initial data-{@link DataTable}.
 * 
 * @author Werner Puff
 * @author Marc Streit
 */
@XmlType
public class LoadDataParameters {

	/** The data domain associated with the loading process */
	@XmlTransient
	private ATableBasedDataDomain dataDomain;

	/** TODO doc */
	private ArrayList<Integer> dimensionIds;

	/** Specifies the IDType that is used in the main data file */
	@XmlElement
	private String fileIDTypeName;

	/** path to main data file */
	private String fileName;

	/** path to gene-cluster-tree file */
	private String geneTreeFileName;

	/** path to experiment-cluster-tree file */
	private String experimentsFileName;

	/** TODO doc */
	private String inputPattern;

	/** labels of the dimensions */
	private List<String> dimensionLabels;

	/** csv-delimiter between to values */
	private String delimiter;

	/** line number to start the parsing of the main-data file */
	private int startParseFileAtLine;

	/** line number to stop the parsing of the main-data file, <code>-1</code> for parsing until end of file */
	private int stopParseFileAtLine;

	/** <code>true</code> if a min-value was set, false otherwise */
	private boolean minDefined = false;

	/** TODO doc */
	private float min = Float.MIN_VALUE;

	/** <code>true</code> if a max-value was set, false otherwise */
	private boolean maxDefined = false;

	/** TODO doc */
	private float max = Float.MAX_VALUE;

	/** TODO doc */
	private String mathFilterMode;

	/** TODO doc */
	private boolean useExperimentClusterInfo;

	@XmlElement
	private boolean isDataHomogeneous = false;

	public LoadDataParameters() {
		this.fileName = null;
		this.geneTreeFileName = null;
		this.experimentsFileName = null;
		this.inputPattern = "";
		this.dimensionLabels = new ArrayList<String>();
		this.delimiter = "";
		this.startParseFileAtLine = 1;
		this.stopParseFileAtLine = -1;
		this.dataDomain = null;
	}

	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
	}

	@XmlTransient
	public ATableBasedDataDomain getDataDomain() {
		return dataDomain;
	}

	public ArrayList<Integer> getDimensionIds() {
		return dimensionIds;
	}

	public void setDimensionIds(ArrayList<Integer> dimensionIds) {
		this.dimensionIds = dimensionIds;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getGeneTreeFileName() {
		return geneTreeFileName;
	}

	public void setGeneTreeFileName(String geneTreeFileName) {
		this.geneTreeFileName = geneTreeFileName;
	}

	public String getExperimentsFileName() {
		return experimentsFileName;
	}

	public void setExperimentsFileName(String experimentsFileName) {
		this.experimentsFileName = experimentsFileName;
	}

	public String getInputPattern() {
		return inputPattern;
	}

	public void setInputPattern(String inputPattern) {
		this.inputPattern = inputPattern;
	}

	public List<String> getDimensionLabels() {
		return dimensionLabels;
	}

	public void setDimensionLabels(List<String> dimensionLabels) {
		this.dimensionLabels = dimensionLabels;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public int getStartParseFileAtLine() {
		return startParseFileAtLine;
	}

	public void setStartParseFileAtLine(int startParseFileAtLine) {
		this.startParseFileAtLine = startParseFileAtLine;
	}

	public int getStopParseFileAtLine() {
		return stopParseFileAtLine;
	}

	public void setStopParseFileAtLine(int stopParseFileAtLine) {
		this.stopParseFileAtLine = stopParseFileAtLine;
	}

	public boolean isMinDefined() {
		return minDefined;
	}

	public void setMinDefined(boolean minDefined) {
		this.minDefined = minDefined;
	}

	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public boolean isMaxDefined() {
		return maxDefined;
	}

	public void setMaxDefined(boolean maxDefined) {
		this.maxDefined = maxDefined;
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public String getMathFilterMode() {
		return mathFilterMode;
	}

	public void setMathFilterMode(String mathFilterMode) {
		this.mathFilterMode = mathFilterMode;
	}

	public boolean isUseExperimentClusterInfo() {
		return useExperimentClusterInfo;
	}

	public void setUseExperimentClusterInfo(boolean useExperimentClusterInfo) {
		this.useExperimentClusterInfo = useExperimentClusterInfo;
	}

	public void setFileIDType(IDType fileIDType) {
		this.fileIDTypeName = fileIDType.getTypeName();
	}

	public String getFileIDTypeName() {
		return fileIDTypeName;
	}

	/**
	 * Set whether the data you want to load is homogeneous (i.e.: there is a global minimum and maximum)
	 * 
	 * @param isDataHomogeneous
	 *            true if your data has a global min and max, else false
	 */
	public void setIsDataHomogeneous(boolean isDataHomogeneous) {
		this.isDataHomogeneous = isDataHomogeneous;
	}

	/**
	 * Tells you whether the data to be processed is homogeneous (i.e.: there is a global minimum and maximum)
	 * 
	 * @return true if data is homogeneous, else false
	 */
	public boolean isDataHomogeneous() {
		return isDataHomogeneous;
	}
}