package com.jettech.vo;


import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseModelSetDetailsVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer datumModelSetTableId;
	
	private String  datumModelSetTableName;
	
	public Integer getDatumModelSetTableId() {
		return datumModelSetTableId;
	}

	public void setDatumModelSetTableId(Integer datumModelSetTableId) {
		this.datumModelSetTableId = datumModelSetTableId;
	}

	public String getDatumModelSetTableName() {
		return datumModelSetTableName;
	}

	public void setDatumModelSetTableName(String datumModelSetTableName) {
		this.datumModelSetTableName = datumModelSetTableName;
	}

	public Integer getTestModelSetTableId() {
		return testModelSetTableId;
	}

	public void setTestModelSetTableId(Integer testModelSetTableId) {
		this.testModelSetTableId = testModelSetTableId;
	}

	public String getTestModelSetTableName() {
		return testModelSetTableName;
	}

	public void setTestModelSetTableName(String testModelSetTableName) {
		this.testModelSetTableName = testModelSetTableName;
	}

	private Integer testModelSetTableId;
	
	private String testModelSetTableName;
 
	

}
