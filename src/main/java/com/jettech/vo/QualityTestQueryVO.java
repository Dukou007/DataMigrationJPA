package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataField;
import com.jettech.entity.QualityTestQuery;

public class QualityTestQueryVO extends BaseVO {

	private String name;
	private Integer dataSourceId;
	private String dataSourceName;
	private Integer dataSchemaId;
	private Integer dataTableId;
	private Integer dataFieldId;
	private Integer qualitySuiteId;
	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	private String sqlText;

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
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
			if(e.getDataField() != null){
				this.setDataFieldId(e.getDataField().getId());
			}
			if(e.getDataSchema() != null){
				this.setDataSchemaId(e.getDataSchema().getId());
			}
			if(e.getDataTable() != null){
				this.setDataTableId(e.getDataTable().getId());
			}
			if(e.getQualitySuite()!=null){
				this.setQualitySuiteId(e.getQualitySuite().getId());
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

	public Integer getDataSchemaId() {
		return dataSchemaId;
	}

	public void setDataSchemaId(Integer dataSchemaId) {
		this.dataSchemaId = dataSchemaId;
	}

	public Integer getDataTableId() {
		return dataTableId;
	}

	public void setDataTableId(Integer dataTableId) {
		this.dataTableId = dataTableId;
	}

	public Integer getDataFieldId() {
		return dataFieldId;
	}

	public void setDataFieldId(Integer dataFieldId) {
		this.dataFieldId = dataFieldId;
	}

	public Integer getQualitySuiteId() {
		return qualitySuiteId;
	}

	public void setQualitySuiteId(Integer qualitySuiteId) {
		this.qualitySuiteId = qualitySuiteId;
	}


}
