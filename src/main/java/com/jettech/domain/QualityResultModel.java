package com.jettech.domain;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestResult;

import java.util.Date;

public class QualityResultModel extends BaseModel {
	// 数据总量
	private Integer dataCount;
	// 明细结果数据量
	private volatile Integer itemCount = 0;
	private Date startTime;
	private Date endTime;
	private Boolean result;
	// source查询语句
	private String sqlText;
	// select字段
	private String selectCols;
	// 数据源
	private String dataSource;
	// 案例ID
	private Integer testCaseId;
	
	private String testCaseName;
	// 明细表名称
	private String secordaryTable;

	public QualityResultModel(QualityTestResult qualityTestResult) {
		parseEntity(qualityTestResult);
	}

	@Override
	public void parseEntity(BaseEntity entity) {
		QualityTestResult qualityTestResult = (QualityTestResult) entity;
		super.parseEntity(qualityTestResult);
		this.secordaryTable = qualityTestResult.getSecordaryTable();
		this.setTestCaseId(qualityTestResult.getTestCaseId());
		this.dataCount = qualityTestResult.getDataCount();
		this.itemCount = qualityTestResult.getItemCount();
		this.dataSource = qualityTestResult.getDataSource();
		this.endTime = qualityTestResult.getEditTime();
		this.startTime = qualityTestResult.getStartTime();
		this.result = qualityTestResult.getResult();
		this.selectCols = qualityTestResult.getSelectCols();
		this.sqlText = qualityTestResult.getSqlText();
	}

	public String getSecordaryTable() {
		return secordaryTable;
	}

	public void setSecordaryTable(String secordaryTable) {
		this.secordaryTable = secordaryTable;
	}

	public Integer getDataCount() {
		return dataCount;
	}

	public void setDataCount(Integer dataCount) {
		this.dataCount = dataCount;
	}

	public Integer getItemCount() {
		return itemCount;
	}

	public void setItemCount(Integer itemCount) {
		this.itemCount = itemCount;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public String getSelectCols() {
		return selectCols;
	}

	public void setSelectCols(String selectCols) {
		this.selectCols = selectCols;
	}

	public String getDataSource() {
		return dataSource;
	}

	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	 

	public void addItemCount() {
		addItemCount(1);
	}

	public void addItemCount(int i) {
		if (itemCount == null)
			itemCount = 0;
		itemCount = itemCount + i;
	}

	public Integer getTestCaseId() {
		return testCaseId;
	}

	public void setTestCaseId(Integer testCaseId) {
		this.testCaseId = testCaseId;
	}

	public String getTestCaseName() {
		return testCaseName;
	}

	public void setTestCaseName(String testCaseName) {
		this.testCaseName = testCaseName;
	}
}
