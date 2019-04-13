package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestResultItem;

public class QualityTestResultItemVO extends BaseVO{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3977402840637466184L;
	
	private Integer testResultId;
	private String selectValue;
	private String result;
	/*public Integer getTestResultId() {
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
	public String getSoruceValue() {
		return soruceValue;
	}
	public void setSoruceValue(String soruceValue) {
		this.soruceValue = soruceValue;
	}
	public String getTragetValue() {
		return tragetValue;
	}
	public void setTragetValue(String tragetValue) {
		this.tragetValue = tragetValue;
	}*/
	public QualityTestResultItemVO(BaseEntity entity) {
		super(entity);
		if(entity!=null) {
			@SuppressWarnings("unused")
			QualityTestResultItem t=(QualityTestResultItem) entity;
		}
	}
	public Integer getTestResultId() {
		return testResultId;
	}
	public void setTestResultId(Integer testResultId) {
		this.testResultId = testResultId;
	}
	public String getSelectValue() {
		return selectValue;
	}
	public void setSelectValue(String selectValue) {
		this.selectValue = selectValue;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public QualityTestResultItemVO() {
		super();
	}
	
	
	
	
}
