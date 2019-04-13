package com.jettech.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.stereotype.Component;

import com.jettech.EnumExecuteStatus;

@Entity
@Component
public class ModelTestResult extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3077514765015384401L;
	
	private Integer testRoundId;
	// target数据量
	private Integer targetCount;
	// source数据量
	private Integer sourceCount;
	// target执行状态
	private EnumExecuteStatus ExecState;
	private Date startTime;
	private Date endTime;
	private String result;
	private Integer sameRow;//相同数据行数
	

	private Integer notSameData;//不同数据值数量
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
	// 明细表名称
	private String secordaryTable;

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

	@Column(columnDefinition = "text")
	public String getTargetSql() {
		return targetSql;
	}

	public void setTargetSql(String targetSql) {
		this.targetSql = targetSql;
	}

	@Column(columnDefinition = "text")
	public String getSourceSql() {
		return sourceSql;
	}

	public void setSourceSql(String sourceSql) {
		this.sourceSql = sourceSql;
	}

	@Column(columnDefinition = "text")
	public String getTargetCol() {
		return targetCol;
	}

	public void setTargetCol(String targetCol) {
		this.targetCol = targetCol;
	}

	@Column(columnDefinition = "text")
	public String getSourceCol() {
		return sourceCol;
	}

	public void setSourceCol(String sourceCol) {
		this.sourceCol = sourceCol;
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

	public String getSecordaryTable() {
		return secordaryTable;
	}

	public void setSecordaryTable(String secordaryTable) {
		this.secordaryTable = secordaryTable;
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

	@Enumerated(EnumType.ORDINAL)
	public EnumExecuteStatus getExecState() {
		return ExecState;
	}

	public void setExecState(EnumExecuteStatus execState) {
		ExecState = execState;
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

	public Integer getTestRoundId() {
		return testRoundId;
	}

	public void setTestRoundId(Integer testRoundId) {
		this.testRoundId = testRoundId;
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
}
