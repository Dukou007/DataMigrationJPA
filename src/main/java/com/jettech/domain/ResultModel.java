package com.jettech.domain;

import java.util.Date;

import com.jettech.EnumExecuteStatus;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.ModelTestResult;
import com.jettech.entity.TestResult;

public class ResultModel extends BaseModel {
	// 明细表名称
	private String secordaryTable;
	// target数据量
	private Integer targetCount;
	// source数据量
	private Integer sourceCount;
	// 执行状态
	private EnumExecuteStatus execState;
	private Date startTime;
	private Date endTime;

	private Integer sameRow;// 相同数据行数
	private Integer notSameData;// 不同数据值数量

	private Integer notSameRow;// 不相同的数据行数

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
	// 案例ID
	private String caseId;

	public ResultModel(TestResult testResult) {
		parseEntity(testResult);
	}
	public ResultModel(ModelTestResult testResult) {
		parseEntity(testResult);
	}

	@Override
	public void parseEntity(BaseEntity entity) {
		TestResult testResult = (TestResult) entity;
		super.parseEntity(testResult);
		this.secordaryTable = testResult.getSecordaryTable();
		this.caseId = testResult.getCaseId();
		this.sourceCol = testResult.getSourceCol();
		this.sourceCount = testResult.getSourceCount();
		this.sourceData = testResult.getSourceData();
		this.sourceKey = testResult.getSourceKey();
		this.sourceSql = testResult.getSourceSql();
		this.targetCol = testResult.getTargetCol();
		this.targetCount = testResult.getSourceCount();
		this.targetData = testResult.getTargetData();
		this.execState = testResult.getExecState();
		this.startTime = testResult.getStartTime();
		this.endTime = testResult.getEndTime();
		this.targetKey = testResult.getTargetKey();
		this.targetSql = testResult.getTargetSql();
		this.notSameRow = testResult.getNotSameRow();
	}

	public Integer getNotSameRow() {
		return notSameRow;
	}

	public void setNotSameRow(Integer notSameRow) {
		this.notSameRow = notSameRow;
	}

	public String getSecordaryTable() {
		return secordaryTable;
	}

	public void setSecordaryTable(String secordaryTable) {
		this.secordaryTable = secordaryTable;
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

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

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

	public void addSourceCount(int num) {
		if (sourceCount == null) {
			sourceCount = num;
		} else {
			sourceCount = sourceCount + num;
		}
	}

	public void addTargetCount(int num) {
		if (targetCount == null) {
			targetCount = num;
		} else {
			targetCount = targetCount + num;
		}
	}

	public void addSameRow(int num) {
		if (sameRow == null) {
			sameRow = num;
		} else {
			sameRow = sameRow + num;
		}
	}

	public void addNotSameData(int num) {
		if (notSameData == null) {
			notSameData = num;
		} else {
			notSameData = notSameData + num;
		}
	}

	public void addNotSameRow(int num) {
		if (notSameRow == null) {
			notSameRow = num;
		} else {
			notSameRow = notSameRow + num;
		}
	}
}
