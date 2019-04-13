package com.jettech.domain;

import com.jettech.EnumDatabaseType;
import com.jettech.entity.DataSource;

public class DbModel extends BaseModel {

	private String name;

	private String url;

	private String username;

	private String password;

	private EnumDatabaseType dbtype;

	private String driver;

	private Boolean autoCommit;
	
	public DbModel() {

	}

	public DbModel(DataSource ds) {
		this.name = ds.getName();
		this.url = ds.getUrl();
		this.username = ds.getUserName();
		this.password = ds.getPassword();
		this.dbtype = ds.getDatabaseType();
		this.driver = ds.getDriver();
		this.autoCommit=ds.getAutoCommit();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public EnumDatabaseType getDbtype() {
		return dbtype;
	}

	public void setDbtype(EnumDatabaseType dbtype) {
		this.dbtype = dbtype;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	// 其他方法
	public String getPrintInfo() {
		return String.format("dbtype:%s,driver:%s,url:%s,username:%s,password:%s", this.dbtype, this.driver, this.url,
		        this.username, this.password);
	}

	public EnumDatabaseType getDatabaseType() {
		return this.dbtype;
	}

	public Boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

}
