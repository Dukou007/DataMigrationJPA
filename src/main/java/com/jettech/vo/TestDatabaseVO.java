package com.jettech.vo;


import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataSchema;

public class TestDatabaseVO extends BaseVO {

	private static final long serialVersionUID = 448578813205726674L;

	private String name;

	// private DataSource dataSource;
	//
	// private List<TestTable> testTables;

	private Integer dataSourceId;

	
	private String dataSourceName;
	
	public TestDatabaseVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			DataSchema e = (DataSchema) entity;
			if(e.getDataSource() != null){
				dataSourceId = e.getDataSource().getId();
				dataSourceName=e.getDataSource().getName();
			}
		}
	}

	private Integer version;
    private Boolean isDict;
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	public Integer getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	public Boolean getIsDict() {
		return isDict;
	}

	public void setIsDict(Boolean isDict) {
		this.isDict = isDict;
	}

	public TestDatabaseVO(String name, Integer dataSourceId,
			String dataSourceName, Integer version, Boolean isDict) {
		super();
		this.name = name;
		this.dataSourceId = dataSourceId;
		this.dataSourceName = dataSourceName;
		this.version = version;
		this.isDict = isDict;
	}

	public TestDatabaseVO() {
		super();
	}
	
}