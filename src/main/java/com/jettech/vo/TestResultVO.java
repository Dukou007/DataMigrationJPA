package com.jettech.vo;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jettech.EnumExecuteStatus;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestResult;

public class TestResultVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3501234864039557062L;
	private Integer testRoundId;
	// target数据量
	private Integer targetCount;


	// source数据量
	private Integer sourceCount;
	// target执行状态
	private EnumExecuteStatus ExecState;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;
	private String result;
	private Integer sameRow;// 相同数据行数
	private Integer notSameRow;// 不同数据行数 sourceCount-sameRow

	private Integer notSameData;// 不同数据值数量
	// target查询语句
	private String targetSql;
	// source查询语句
	private String sourceSql;
	// target字段
	private String targetCol;
	// source字段
	private String sourceCol;
	private String sourceKey;
	private String targetKey;
	// target数据源
	private String targetData;
	// source数据源
	private String sourceData;
	// 案例名称
	private String caseName;


	public Integer getNotSameRow() {
		return notSameRow;
	}
	
	public void setNotSameRow(Integer notSameRow) {
		this.notSameRow = notSameRow;
	}
	public String getCaseName() {
		return caseName;
	}

	public void setCaseName(String caseName) {
		this.caseName = caseName;
	}

	// 明细表名称
	private String secordaryTable;

	public Integer getTestRoundId() {
		return testRoundId;
	}

	public void setTestRoundId(Integer testRoundId) {
		this.testRoundId = testRoundId;
	}

	public Integer getTargetCount() {
		return targetCount;
	}

	public void setTargetCount(Integer targetCount) {
		this.targetCount = targetCount;
	}

	public Integer getSourceCount() {
		return sourceCount;
	}

	public void setSourceCount(Integer sourceCount) {
		this.sourceCount = sourceCount;
	}

//	public EnumExecuteStatus getExecState() {
//		return ExecState;
//	}
//
//	public void setExecState(EnumExecuteStatus execState) {
//		ExecState = execState;
//	}

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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Integer getSameRow() {
		return sameRow;
	}

	public void setSameRow(Integer sameRow) {
		this.sameRow = sameRow;
	}

	public Integer getNotSameData() {
		return notSameData;
	}

	public void setNotSameData(Integer notSameData) {
		this.notSameData = notSameData;
	}

	public String getTargetSql() {
		return targetSql;
	}

	public void setTargetSql(String targetSql) {
		this.targetSql = targetSql;
	}

	public String getSourceSql() {
		return sourceSql;
	}

	public void setSourceSql(String sourceSql) {
		this.sourceSql = sourceSql;
	}

	public String getTargetCol() {
		return targetCol;
	}

	public void setTargetCol(String targetCol) {
		this.targetCol = targetCol;
	}

	public String getSourceCol() {
		return sourceCol;
	}

	public void setSourceCol(String sourceCol) {
		this.sourceCol = sourceCol;
	}

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public String getTargetKey() {
		return targetKey;
	}

	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}



	public String getTargetData() {
		return targetData;
	}

	public void setTargetData(String targetData) {
		this.targetData = targetData;
	}

	public String getSourceData() {
		return sourceData;
	}

	public void setSourceData(String sourceData) {
		this.sourceData = sourceData;
	}

	

	public TestResultVO() {
		super();
	}

	public TestResultVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestResult testResult = (TestResult) entity;
			if (testResult.getResult() != null) {
				this.notSameData=testResult.getNotSameData();
				this.testRoundId = testResult.getTestRoundId();
				this.ExecState=testResult.getExecState();
			}
			if (testResult.getSecordaryTable() != null) {
				this.secordaryTable = testResult.getSecordaryTable();
			}
			if(testResult.getSourceCount()!=null&&testResult.getSourceCount()>0) {
				this.notSameRow=testResult.getNotSameRow();
			}else {
				this.notSameRow=0;
				this.sourceCount=0;
				this.sameRow=0;
				this.targetCount=0;
			}
			if(testResult.getNotSameData()!=null&&!testResult.getNotSameData().equals("")) {
				this.notSameData=testResult.getNotSameData();
			}else {
				this.notSameData=0;
			}
			if(testResult.getEditTime()!=null&&!testResult.getEditTime().equals("")) {
				this.endTime=testResult.getEditTime();
			}else {
				this.endTime=null;
			}
			if(testResult.getResult()!=null&&!testResult.getResult().equals("")) {
				this.result=testResult.getResult();
			}else {
				this.result="false";
			}

		}
	}


	public String getSecordaryTable() {
		return secordaryTable;
	}

	public void setSecordaryTable(String secordaryTable) {
		this.secordaryTable = secordaryTable;
	}

	

	public EnumExecuteStatus getExecState() {
		return ExecState;
	}
	
	public void setExecState(EnumExecuteStatus execState) {
		ExecState = execState;
	}
}
