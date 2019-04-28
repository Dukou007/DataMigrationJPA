package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataField;
import com.jettech.entity.QualityTestQuery;

public class QualityTestQueryVO extends BaseVO {

	private String name;
	private Integer dataSourceId;
	private String dataSourceName;
	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	private String testFieldNames;
	private String sqlText;

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public String getTestFieldNames() {
		return testFieldNames;
	}

	public void setTestFieldNames(String testFieldNames) {
		this.testFieldNames = testFieldNames;
	}

	public Integer getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public QualityTestQueryVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			QualityTestQuery e = (QualityTestQuery) entity;
			this.dataSourceId = e.getDataSource().getId();
			this.dataSourceName=e.getDataSource().getName();
			this.setId(e.getId());
			if (e.getDataFields() != null) {
				this.testFieldNames = "";
				for (DataField field : e.getDataFields()) {
					if (field != null && field.getName() != null)
						this.testFieldNames += "," + field.getName();
				}
				if (this.testFieldNames.length() > 0) {
					this.testFieldNames = this.testFieldNames.substring(1);
				}
			}
		}
	}

	public QualityTestQueryVO() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


}
