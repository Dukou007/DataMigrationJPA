package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataSource;

public class DataSourceVO extends BaseVO {

	public DataSourceVO() {
	}

	public DataSourceVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			DataSource e = (DataSource) entity;
			if (e.getDatabaseType() != null)
				databaseType = e.getDatabaseType().name();
		}
	}
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 6215885559226367976L;

	private String name;

	private String databaseType;

	private String databaseVersion;

	private String host;

	private String userName;

	private String password;

	private String characterSet;

	private String defaultSchema;

	private String driver;

	private String url;

	private String port;
	private String sid;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getDatabaseVersion() {
		return databaseVersion;
	}

	public void setDatabaseVersion(String databaseVersion) {
		this.databaseVersion = databaseVersion;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

}