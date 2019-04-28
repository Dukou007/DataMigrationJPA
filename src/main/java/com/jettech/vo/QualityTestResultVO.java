package com.jettech.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.jettech.EnumExecuteStatus;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestResult;

public class QualityTestResultVO extends BaseVO{

	private static final long serialVersionUID = -3077514765015384401L;
	// 数据总量
	private Integer dataCount;
	// 明细结果数据量
	private Integer itemCount;
	// target执行状态
	private EnumExecuteStatus execState = EnumExecuteStatus.Ready;
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

	//测试意图
	private String testPurpose;
	//期望值
	private String purposeValue = "0";

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
	
	@Enumerated(EnumType.STRING)
	public EnumExecuteStatus getExecState() {
		return execState;
	}

	public void setExecState(EnumExecuteStatus execState) {
		this.execState = execState;
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

	 @Column(columnDefinition="text")
	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}
	
	@Column(columnDefinition = "text")
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

	public String getSecordaryTable() {
		return secordaryTable;
	}

	public void setSecordaryTable(String secordaryTable) {
		this.secordaryTable = secordaryTable;
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

	public QualityTestResultVO(BaseEntity entity) {
		super(entity);
		if(entity!=null) {
			QualityTestResult q = (QualityTestResult) entity;
		//	this.execState=EnumExecuteStatus.Ready;
			this.execState = q.getExecState();
		}
	}


	public String getTestPurpose() {
		return testPurpose;
	}

	public void setTestPurpose(String testPurpose) {
		this.testPurpose = testPurpose;
	}

	public String getPurposeValue() {
		return purposeValue;
	}

	public void setPurposeValue(String purposeValue) {
		this.purposeValue = purposeValue;
	}
}
