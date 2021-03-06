/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.core.data.perspective.variable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.data.collection.table.Table;
import org.caleydo.core.data.datadomain.ATableBasedDataDomain;
import org.caleydo.core.data.datadomain.IDataDomain;
import org.caleydo.core.data.filter.FilterManager;
import org.caleydo.core.data.graph.tree.ClusterNode;
import org.caleydo.core.data.graph.tree.ClusterTree;
import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.data.virtualarray.VirtualArray;
import org.caleydo.core.data.virtualarray.delta.VirtualArrayDelta;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.GroupList;
import org.caleydo.core.id.IDCreator;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.base.IDefaultLabelHolder;
import org.caleydo.core.util.logging.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * <p>
 * A {@link Perspective} holds all relevant meta data for either records through the {@link Perspective} or dimensions
 * through the {@link Perspective}. For many uses both, a Perspective and a DimsenionPerspective are necessary.
 * {@link TablePerspective} is designed to hold combinations of Record- and AVariablePerspectives.
 * </p>
 * <p>
 * Among the information the DataPerspectives holds are:
 * <ol>
 * <li>The {@link VirtualArray}, which determines which elements of a Dimension or Record should be accessed in which
 * order.</li>
 * <li>The {@link GroupList} (currently as part of the VirtualArray), which holds information on which elements in the
 * VirtualArray are grouped (e.g., because of a clustering).</li>
 * <li>The {@link ClusterTree}, which defines a hierarchy on how similar elements are to each other.
 * <li>The {@link FilterManager}, which holds and manages all filters defined on this perspective.
 * </ol>
 * </p>
 *
 * @author Alexander Lex
 */
@XmlType
@XmlSeeAlso({ VirtualArray.class })
@XmlRootElement
public class Perspective implements IDefaultLabelHolder {

	/** The unique ID of the perspective */
	@XmlElement
	private String perspectiveID;

	/**
	 * ID that is set if this perspective is synchronized across datasets. The perspectives that are kept in-sync across
	 * dataset share the same ID.
	 */
	private String crossDatasetID = null;

	/** A human-readable description of the perspective */
	private String label;

	/**
	 * Flag telling whether the set label is a default (true) and thereby should probably not be displayed or whether
	 * the label is worth displaying
	 */
	@XmlElement
	private boolean isDefaultLabel = true;

	/**
	 * Flag determining whether this perspective is private to a certain view. That means that other views typically
	 * should not use this perspective
	 */
	@XmlElement
	private boolean isPrivate;

	/**
	 * Flag determining whether this perspective is the default perspective . A default perspective is defined for every
	 * {@link Table}. It should reflect "the whole dataset". This is used for instance by the support views.
	 */
	@XmlElement
	private boolean isDefault;

	/**
	 * Flag telling us whether the tree has been automatically generated (i.e., is the default tree), or whether it has
	 * been externally set, e.g., using clustering or importing. It does not make sense to visualize a default tree for
	 * example.
	 */
	@XmlElement
	private boolean isTreeDefaultTree = true;

	/** The dataDomain this perspective belongs to */
	@XmlTransient
	private IDataDomain dataDomain;

	@XmlTransient
	private IDType idType;

	/** The {@link VirtualArray} of this DataPerspective. */
	private VirtualArray virtualArray;

	/**
	 * The root not of the tree from this perspectives view. This enables the perspective to use only part of the tree
	 */
	@XmlElement
	private ClusterNode rootNode;
	/**
	 * The filter manager that manages and holds all filters applied to this prespective.
	 */
	@XmlTransient
	private FilterManager filterManager;

	@XmlTransient
	private ClusterTree tree;

	/**
	 * The number of elements that could not be converted from an original startification/perspective for this dataset.
	 * An example is if a stratification is loaded for a larger dataset and later applied to a smaller dataset. Here
	 * information is lost. How many elements are lost is stored in this integer.
	 */
	private int unmappedElements = 0;

	/** Only for serialization */
	public Perspective() {
	}

	public Perspective(IDataDomain dataDomain, IDType idType) {
		this.idType = idType;
		this.dataDomain = dataDomain;
		init();
	}

	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
		init();
	}

	// /** Get the human readable label of the element specified by the ID */
	// protected String getElementLabel(Integer id)
	// {
	// dataDomain.getDimensionLabel(id)
	// }

	private void init() {
		// if this perspective is de-serialized the perspectiveID is already set.
		if (perspectiveID == null)
			perspectiveID = "Perspective_" + IDCreator.createPersistentID(Perspective.class);
		filterManager = new FilterManager(dataDomain, this);

	}

	/**
	 * @return the dataDomain, see {@link #dataDomain}
	 */
	@XmlTransient
	public IDataDomain getDataDomain() {
		return dataDomain;
	}

	/**
	 * @param label
	 *            setter, see {@link #label}
	 */
	@Override
	public void setLabel(String label, boolean isDefaultLabel) {
		if (label == null)
			throw new IllegalArgumentException("Label was null");
		this.label = label;
		this.isDefaultLabel = isDefaultLabel;
	}

	/**
	 * @return the label, see {@link #label}, or if the label is null, the perspectiveID is returned.
	 */
	@Override
	public String getLabel() {
		if (label == null || label.isEmpty())
			return perspectiveID;
		return label;
	}

	/**
	 * @return the isDefaultLabel, see {@link #isDefaultLabel}
	 */
	@Override
	public boolean isLabelDefault() {
		return isDefaultLabel;
	}

	/**
	 * @param unMappedElements
	 *            setter, see {@link unmappedElements}
	 */
	public void setUnmappedElements(int unmappedElements) {
		this.unmappedElements = unmappedElements;
	}

	/**
	 * @return the unmappedElements, see {@link #unmappedElements}
	 */
	public int getUnmappedElements() {
		return unmappedElements;
	}

	/**
	 * @param isPrivate
	 *            setter, see {@link #isPrivate}
	 */
	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	/**
	 * @return the isPrivate, see {@link #isPrivate}
	 */
	public boolean isPrivate() {
		return isPrivate;
	}

	/**
	 * @param isDefault
	 *            setter, see {@link #isDefault}
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @return the isDefault, see {@link #isDefault}
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * Returns the automatically generated globally unique {@link #perspectiveID} of this perspective
	 *
	 * @return
	 */
	public String getPerspectiveID() {
		return perspectiveID;
	}

	/**
	 * @param crossDatasetID
	 *            setter, see {@link crossDatasetID}
	 */
	public void setCrossDatasetID(String crossDatasetID) {
		this.crossDatasetID = crossDatasetID;
	}

	/**
	 * @return the crossDatasetID, see {@link #crossDatasetID}
	 */
	public String getCrossDatasetID() {
		return crossDatasetID;
	}

	/**
	 * @param idType
	 *            setter, see {@link #idType}
	 */
	public void setIDType(IDType idType) {
		this.idType = idType;
		if (virtualArray != null)
			virtualArray.setIdType(idType);
	}

	/**
	 * @return the idType, see {@link #idType}
	 */
	public IDType getIdType() {
		return idType;
	}

	/**
	 * Creates a new virtual array for this perspective and resets all other associated parameters (clusterSizes,
	 * contentTree, etc.)
	 *
	 * @param indices
	 *            can be a filled ArrayList, an empty ArrayList for empty initialization or null for empty
	 *            initialization
	 */
	private void createVirtualArray(List<Integer> indices) {
		virtualArray = new VirtualArray(idType, indices);
	}

	/**
	 * Returns a reference to the virtual array of this perspective
	 *
	 * @return
	 */
	@XmlElementRef
	public VirtualArray getVirtualArray() {
		return virtualArray;
	}

	/**
	 * <p>
	 * Returns the cluster tree showing the relations between the elements indexed by the virtual array.
	 * </p>
	 * <p>
	 * Depending on whether or not a cluster tree has been set, this method either returns a tree with real relations,
	 * or a tree with one root and all leafs as children of the root (the default tree). You can check whether the tree
	 * is the default tree using {@link #isTreeDefaultTree()}.
	 * </p>
	 *
	 * @return
	 */
	@XmlTransient
	public ClusterTree getTree() {
		// this should only happen when we de-serialize with a default tree.
		// if (tree == null)
		// createDefaultTreeAndGroupList();
		return tree;
	}

	/**
	 * Set a cluster tree for the virtual array.
	 *
	 * @param tree
	 */
	@Deprecated
	public void setTree(ClusterTree tree) {
		isTreeDefaultTree = false;
		this.tree = tree;
	}

	/**
	 * Sets a new virtual array to this perspective.
	 *
	 * @param virtualArray
	 */
	@Deprecated
	public void setVirtualArray(VirtualArray virtualArray) {
		if (virtualArray == null) {
			Logger.log(new Status(IStatus.ERROR, "org.caleydo.core", "Virtual array to be set was null"));
			return;
		}
		this.virtualArray = virtualArray;
	}

	/**
	 * @return the isTreeDefaultTree, see {@link #isTreeDefaultTree}
	 */
	public boolean isTreeDefaultTree() {
		return isTreeDefaultTree;
	}

	/**
	 * Returns the {@link #rootNode} of the tree, which can be different from {@link ClusterTree#getRoot()} - it can be
	 * a sub-tree only.
	 *
	 * @return
	 */
	public ClusterNode getTreeRoot() {
		if (rootNode != null)
			return rootNode;
		else if (tree != null)
			return tree.getRoot();
		else
			return null;
	}

	/**
	 * Returns the {@link FilterManager} associated with this perspective. Every perspective has it's own unique filter
	 * manager.
	 *
	 * @return
	 */
	public FilterManager getFilterManager() {
		return filterManager;
	}

	/**
	 * Sets the delta to the virtual array and resets other related data (groups, trees) accordingly.
	 *
	 * @param delta
	 */
	public void setVirtualArrayDelta(VirtualArrayDelta delta) {
		virtualArray.setDelta(delta);
		createDefaultGroupList();
		// virtualArray.setGroupList(null);
	}

	public void init(PerspectiveInitializationData data, boolean createDefaultTree) {
		if (data != null && data.getLabel() != null) {
			setLabel(data.getLabel(), false);
		}

		// Case 1: we want an empty perspective
		if (data == null) {
			createVirtualArray(null);
			return;
		}
		// Case 2: we only have a virtual array
		else if (data.getIndices() != null && data.getGroupSizes() == null && data.getTree() == null) {
			createVirtualArray(data.getIndices());
			createDefaultGroupList();
		}
		// Case 3: we have a virtual array and grouping
		else if (data.getIndices() != null && data.getGroupSizes() != null) {
			createVirtualArray(data.getIndices());
			createGroupListAndDefaultTreeFromClusterSizes(data);
		}
		// Case 4: we have a tree and nothing else, with either the default root
		// or a specific node as root
		else if (data.getTree() != null && data.getIndices() == null) {
			createEverythingFromTree(data);
		}
		// Case 5: we initialize from a given virtual array
		else if (data.getVirtualArray() != null) {
			virtualArray = data.getVirtualArray();
			virtualArray.setIdType(idType);
			if (createDefaultTree)
				createDefaultTreeFromGroupList();
		} else {
			throw new IllegalStateException("Cannot initialize from " + data
					+ " either redundant or to little information.");
		}
	}

	/**
	 * <p>
	 * {@link Perspective}s are initialized by providing a {@link PerspectiveInitializationData} object, which can
	 * contain a number of different combinations of information to initialize the perspective.
	 * </p>
	 * <p>
	 * If null is passed, an empty perspective is created.
	 * </p>
	 *
	 * @param data
	 */
	public void init(PerspectiveInitializationData data) {
		init(data, true);
	}

	public void setVADelta(VirtualArrayDelta delta) {
		virtualArray.setDelta(delta);
	}

	// -------------------------- Initialization Methods
	// -------------------------------------

	/** Creates a default tree of depth 1 and a {@link GroupList} with one group */
	private void createDefaultGroupList() {
		// isTreeDefaultTree = true;
		// tree = new ClusterTree(idType, virtualArray.size());
		// ClusterNode root = new ClusterNode(tree, "root", 0, true, -1);
		// tree.setRootNode(root);
		// for (Integer id : virtualArray) {
		// tree.addChild(root, new ClusterNode(tree, getElementLabel(id), id,
		// false, id));
		// }
		GroupList groupList = new GroupList();
		Group group = new Group(virtualArray.size(), 0);
		group.setLabel("All");
		groupList.append(group);
		virtualArray.setGroupList(groupList);

	}

	private void createGroupListAndDefaultTreeFromClusterSizes(PerspectiveInitializationData data) {
		GroupList groupList = new GroupList();
		int groupCounter = 0;
		isTreeDefaultTree = true;
		// tree = new ClusterTree(idType, data.getIndices().size());
		// int clusterNr = 0;
		// ClusterNode root = new ClusterNode(tree, "Root", clusterNr++, true,
		// -1);
		// tree.setRootNode(root);
		// ClusterNode node;
		int from = 0;
		int to = 0;
		List<Integer> groupSizes = data.getGroupSizes();
		List<String> groupNames = data.getGroupNames();
		for (int groupCount = 0; groupCount < groupSizes.size(); groupCount++) {
			Integer groupSize = groupSizes.get(groupCount);
			String groupName;
			boolean isDefaultLabel = true;
			if (groupNames != null) {
				groupName = groupNames.get(groupCount);
				isDefaultLabel = false;
			} else
				groupName = "Group: " + groupCount;

			// node = new ClusterNode(tree, groupName, clusterNr++, false, -1);
			// node.setDefaultLabel(isDefaultLabel);
			Group tempGroup = new Group(groupSize, data.getSampleElements().get(groupCounter));
			tempGroup.setLabel(groupName, isDefaultLabel);
			// tree.addChild(root, node);
			groupList.append(tempGroup);
			groupCounter++;
			tempGroup.setStartIndex(from);
			to += groupSize;
			// ClusterNode leaf;
			// for (int vaIndex = from; vaIndex < to; vaIndex++) {
			// Integer id = virtualArray.get(vaIndex);
			// leaf = new ClusterNode(tree, "Leaf: " + id, clusterNr++, false,
			// id);
			// tree.addChild(node, leaf);
			// }
			from = to;
		}
		virtualArray.setGroupList(groupList);
	}

	private void createDefaultTreeFromGroupList() {
		isTreeDefaultTree = true;
		tree = new ClusterTree(idType, virtualArray.size());
		int clusterNr = 0;
		ClusterNode root = new ClusterNode(tree, "Root", clusterNr++, true, -1);
		tree.setRootNode(root);
		ClusterNode node;
		int from = 0;
		int to = 0;
		for (Group group : virtualArray.getGroupList()) {
			node = new ClusterNode(tree, "Group: " + group.getGroupIndex(), clusterNr++, false, -1);
			tree.addChild(root, node);
			from = group.getStartIndex();
			to += group.getSize();
			ClusterNode leaf;
			for (int vaIndex = from; vaIndex < to; vaIndex++) {
				Integer id = virtualArray.get(vaIndex);
				leaf = new ClusterNode(tree, "Leaf: " + id, clusterNr++, false, id);
				tree.addChild(node, leaf);
			}

		}
	}

	/**
	 * Creates a virtual array for this perspective by extracting the leaves from the tree. Respects the artificial
	 * {@link #rootNode}, or uses the trees default root if no artificial root is set.
	 */
	private void createEverythingFromTree(PerspectiveInitializationData data) {
		tree = data.getTree();

		if (tree.getLeaveIDType() == null)
			tree.initializeIDTypes(idType);
		if (tree.getLeaveIDType() != idType)
			throw new IllegalStateException("IDType of tree and perspective do not match. Tree:  "
					+ tree.getLeaveIDType() + " perspective: " + idType);

		isTreeDefaultTree = false;
		if (data.getRootNode() == null)
			rootNode = tree.getRoot();
		else
			rootNode = data.getRootNode();

		virtualArray = new VirtualArray(idType, rootNode.getLeaveIds());
		ArrayList<ClusterNode> listOfNodesForGroups = new ArrayList<ClusterNode>(1);
		listOfNodesForGroups.add(rootNode);
		virtualArray.buildNewGroupList(listOfNodesForGroups);

	}

	public void sort(List<Float> values) {
		virtualArray.sort(values);
	}

	@Override
	public String toString() {
		return "[P for " + idType + ", size: " + (virtualArray == null ? -1 : virtualArray.size()) + "]";
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getProviderName() {
		return "Perspective";
	}
}
