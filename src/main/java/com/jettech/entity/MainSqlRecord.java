package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
@Entity
@Table(name = "mainrecord")
public class MainSqlRecord  extends BaseEntity {
	

	
	private String  createUser;
	
	private String  editUser;
	
	//target数据量
	private String  targetCount;
	//source数据量
	private String  sourceCount;
	//target执行状态
	private String  targetExecState;
	//source执行状态
	private String  sourceExecState;
	//target查询语句
	private String  targetSql;
	//source查询语句
	private String  sourceSql;
	//target字段
	private String  targetCol;
	//source字段
	private String  sourceCol;
	//target数据源
	private String  targetData;
	//source数据源
	private String  sourceData;
	//案例ID
	private String  caseId;
	//明细表名称
	private String  secordaryTable;
	
	private String beiyong1;
	
	private String beiyong2;
	
	private String beiyong3;
	
	private String beiyong4;



	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getEditUser() {
		return editUser;
	}

	public void setEditUser(String editUser) {
		this.editUser = editUser;
	}



	public String getTargetCount() {
		return targetCount;
	}

	public void setTargetCount(String targetCount) {
		this.targetCount = targetCount;
	}

	public String getSourceCount() {
		return sourceCount;
	}

	public void setSourceCount(String sourceCount) {
		this.sourceCount = sourceCount;
	}

	public String getTargetExecState() {
		return targetExecState;
	}

	public void setTargetExecState(String targetExecState) {
		this.targetExecState = targetExecState;
	}

	public String getSourceExecState() {
		return sourceExecState;
	}

	public void setSourceExecState(String sourceExecState) {
		this.sourceExecState = sourceExecState;
	}
	@Lob
	public String getTargetSql() {
		return targetSql;
	}

	public void setTargetSql(String targetSql) {
		this.targetSql = targetSql;
	}
	@Lob
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

	
	



	public String getBeiyong1() {
		return beiyong1;
	}

	public void setBeiyong1(String beiyong1) {
		this.beiyong1 = beiyong1;
	}

	public String getBeiyong2() {
		return beiyong2;
	}

	public void setBeiyong2(String beiyong2) {
		this.beiyong2 = beiyong2;
	}

	public String getBeiyong3() {
		return beiyong3;
	}

	public void setBeiyong3(String beiyong3) {
		this.beiyong3 = beiyong3;
	}

	public String getBeiyong4() {
		return beiyong4;
	}

	public void setBeiyong4(String beiyong4) {
		this.beiyong4 = beiyong4;
	}

	
	

}
