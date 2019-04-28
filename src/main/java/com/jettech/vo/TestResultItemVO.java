package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestResultItem;

/**
 * 	@author zhou_xiaolong
 *	执行结果明细表，该表的名称不是固定的，根据案例和时间戳来定义
 */
public class TestResultItemVO extends BaseVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3977402840637466184L;
	
	private Integer testResultId;
	private String keyValue;
	private String result;
	private String columnName;
	private String sourceValue;
	private String targetValue;
	public Integer getTestResultId() {
		return testResultId;
	}
	public void setTestResultId(Integer testResultId) {
		this.testResultId = testResultId;
	}
	public String getKeyValue() {
		return keyValue;
	}
	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getSourceValue() {
		return sourceValue;
	}
	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}
	public String getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}
	public TestResultItemVO(BaseEntity entity) {
		super(entity);
		if(entity!=null) {
			@SuppressWarnings("unused")
			TestResultItem e=(TestResultItem) entity;
			
		}
	}
	public TestResultItemVO() {
		super();
	}
	
	
}
