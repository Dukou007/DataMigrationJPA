package com.jettech.vo.sql;

import java.util.List;

public class DeleteSqlVo {
	String dbType;
	String dbname;
    String host;
    String username;
    String pwd;
    String table;
    List<Wherelist> wherelist;
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public String getDbname() {
		return dbname;
	}
	public void setDbname(String dbname) {
		this.dbname = dbname;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public List<Wherelist> getWherelist() {
		return wherelist;
	}
	public void setWherelist(List<Wherelist> wherelist) {
		this.wherelist = wherelist;
	}

   
}
