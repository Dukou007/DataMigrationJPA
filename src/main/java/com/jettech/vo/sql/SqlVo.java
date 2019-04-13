package com.jettech.vo.sql;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class SqlVo {
	String caseid;
	private String  createUser;	
	private String  editUser;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private String  editTime;	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private String  createTime;
	String sourcedbType;	
    List<Sourceselectsql> sourceselectsql;
    String targetdbType;
    List<Sourceselectsql> targetselectsql;
	public String getCaseid() {
		return caseid;
	}
	public void setCaseid(String caseid) {
		this.caseid = caseid;
	}
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
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getSourcedbType() {
		return sourcedbType;
	}
	public void setSourcedbType(String sourcedbType) {
		this.sourcedbType = sourcedbType;
	}
	public List<Sourceselectsql> getSourceselectsql() {
		return sourceselectsql;
	}
	public void setSourceselectsql(List<Sourceselectsql> sourceselectsql) {
		this.sourceselectsql = sourceselectsql;
	}
	public String getTargetdbType() {
		return targetdbType;
	}
	public void setTargetdbType(String targetdbType) {
		this.targetdbType = targetdbType;
	}
	public List<Sourceselectsql> getTargetselectsql() {
		return targetselectsql;
	}
	public void setTargetselectsql(List<Sourceselectsql> targetselectsql) {
		this.targetselectsql = targetselectsql;
	}
	public String getEditTime() {
		return editTime;
	}
	public void setEditTime(String editTime) {
		this.editTime = editTime;
	}


  
}
