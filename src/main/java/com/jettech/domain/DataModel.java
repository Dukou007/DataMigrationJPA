package com.jettech.domain;

import java.util.ArrayList;
import java.util.List;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataTable;

/**
 * 一个数据模型，可以是对应一个表，也可以是对应一组字段组成的结构
 * 
 * @author tan
 *
 */
public class DataModel extends BaseModel {
	private String name;

	private String des;

	private Boolean isView;
	// 是否是数据字典
	private Boolean isDict;

	private List<DataField> columns = new ArrayList<>();

	public DataModel(BaseEntity entity) {
		DataTable table = (DataTable) entity;
		this.name = table.getName();
		this.isDict = table.getIsDict();
		this.isView = table.getIsView();
		this.des = table.getDes();

		if (table.getDataFields() != null && table.getDataFields().size() > 0) {
			for (com.jettech.entity.DataField field : table.getDataFields()) {
				columns.add(new DataField(field));
			}
		}

	}

	public String getName() {
		return name;
	}

	public String getDes() {
		return des;
	}

	public Boolean getIsView() {
		return isView;
	}

	public Boolean getIsDict() {
		return isDict;
	}

	public List<DataField> getColumns() {
		return columns;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public void setIsView(Boolean isView) {
		this.isView = isView;
	}

	public void setIsDict(Boolean isDict) {
		this.isDict = isDict;
	}

	public void setColumns(List<DataField> columns) {
		this.columns = columns;
	}

}
