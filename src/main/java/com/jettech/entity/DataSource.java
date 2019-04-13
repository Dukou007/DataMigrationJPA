package com.jettech.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.alibaba.fastjson.annotation.JSONField;
import com.jettech.EnumDatabaseType;

@Entity
@Table(name = "data_source")

public class DataSource extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -956641880869067295L;

	private String name;

	private EnumDatabaseType databaseType;

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

	private Boolean autoCommit = false;

	private List<DataSchema> dataSchemas = new ArrayList<>();

	private List<TestQuery> testQuerys = new ArrayList<>();

	
	private List<QualityTestQuery> qualityTestQuerys = new ArrayList<QualityTestQuery>();
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "data_source_id", referencedColumnName = "id")
	public List<QualityTestQuery> getQualityTestQuerys() {
		return qualityTestQuerys;
	}

	public void setQualityTestQuerys(List<QualityTestQuery> qualityTestQuerys) {
		this.qualityTestQuerys = qualityTestQuerys;
	}

	@Column(unique = true, nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Enumerated(EnumType.STRING)
	public EnumDatabaseType getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(EnumDatabaseType databaseType) {
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

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "data_source_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<DataSchema> getDataSchemas() {
		return dataSchemas;
	}

	public void setDataSchemas(List<DataSchema> dataSchemas) {
		this.dataSchemas = dataSchemas;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "data_source_id", referencedColumnName = "id")
	public List<TestQuery> getTestQuerys() {
		return testQuerys;
	}

	public void setTestQuerys(List<TestQuery> testQuerys) {
		this.testQuerys = testQuerys;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Boolean getAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(Boolean autoCommit) {
		this.autoCommit = autoCommit;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

}