package org.caleydo.core.data.virtualarray;

import java.util.ArrayList;
import java.util.List;

import org.caleydo.core.data.collection.table.DataTable;
import org.caleydo.core.data.virtualarray.group.Group;
import org.caleydo.core.data.virtualarray.group.RecordGroupList;
import org.caleydo.core.manager.datadomain.ATableBasedDataDomain;
import org.caleydo.core.manager.datadomain.IDataDomain;

public class SetBasedDimensionGroupData extends ADimensionGroupData {

	private ATableBasedDataDomain dataDomain;
	private DataTable table;

	public SetBasedDimensionGroupData(ATableBasedDataDomain dataDomain, DataTable table) {
		this.dataDomain = dataDomain;
		this.table = table;
	}

	@Override
	public RecordVirtualArray getSummaryVA() {
		return table.getRecordData(DataTable.RECORD).getRecordVA();
	}

	@Override
	public ArrayList<RecordVirtualArray> getSegmentVAs() {
		RecordVirtualArray recordVA = table.getRecordData(DataTable.RECORD)
				.getRecordVA();

		if (recordVA.getGroupList() == null)
			return null;

		RecordGroupList groupList = recordVA.getGroupList();
		groupList.updateGroupInfo();

		ArrayList<RecordVirtualArray> segmentBrickVAs = new ArrayList<RecordVirtualArray>();

		for (Group group : groupList) {

			RecordVirtualArray subVA = new RecordVirtualArray("CONTENT",
					recordVA.getVirtualArray().subList(group.getStartIndex(),
							group.getEndIndex() + 1));
			segmentBrickVAs.add(subVA);
		}

		return segmentBrickVAs;
	}

	public void setDataDomain(ATableBasedDataDomain dataDomain) {
		this.dataDomain = dataDomain;
	}

	@Override
	public IDataDomain getDataDomain() {
		return dataDomain;
	}

	public void setTable(DataTable table) {
		this.table = table;
	}

	public DataTable getTable() {
		return table;
	}

	@Override
	public ArrayList<Group> getGroups() {
		RecordVirtualArray recordVA = table.getRecordData(DataTable.RECORD)
				.getRecordVA();

		if (recordVA.getGroupList() == null)
			return null;

		RecordGroupList groupList = recordVA.getGroupList();
		groupList.updateGroupInfo();

		return groupList.getGroups();
	}

	@Override
	public int getID() {
		return table.getID();
	}

	@Override
	public List<ISegmentData> getSegmentData() {

		RecordVirtualArray recordVA = table.getRecordData(DataTable.RECORD)
				.getRecordVA();

		if (recordVA.getGroupList() == null)
			return null;

		RecordGroupList groupList = recordVA.getGroupList();
		groupList.updateGroupInfo();

		List<ISegmentData> segmentBrickData = new ArrayList<ISegmentData>();

		for (Group group : groupList) {

			RecordVirtualArray subVA = new RecordVirtualArray("CONTENT",
					recordVA.getVirtualArray().subList(group.getStartIndex(),
							group.getEndIndex() + 1));
			segmentBrickData.add(new SetBasedSegmentData(dataDomain, table, subVA,
					group, this));
		}

		return segmentBrickData;
	}

	@Override
	public String getLabel() {
		return table.getLabel();
	}

}