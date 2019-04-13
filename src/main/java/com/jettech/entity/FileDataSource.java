package com.jettech.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.jettech.EnumFileType;

@Entity
@Table(name = "file_data_source", uniqueConstraints = {
		@UniqueConstraint(name = "ix_file_data_source_name", columnNames = { "name" }) })

public class FileDataSource extends BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5798082473941182942L;

	private String name; // 名称

	private EnumFileType fileType;// 文件类型

	private String characterSet;// 字符集

	private Boolean usePage;// 默认不分页

	private Integer pageSize;// 分页大小

	private String host;// 服务器(IP)

	private Integer connectionType;// 连接协议类型

	private String userName;// 连接用户名

	private String password;// 连接密码

	private String filePath;// 文件路径(相对路径)

	private Integer version;

	private String lineSpeater;// 换行符

	private String columnSpeater;// 分列符

	private String textQualifier;// 文本限定符

	public String getTextQualifier() {
		return textQualifier;
	}
	
	public void setTextQualifier(String textQualifier) {
		this.textQualifier = textQualifier;
	}

	public String getLineSpeater() {
		return lineSpeater;
	}

	public void setLineSpeater(String lineSpeater) {
		this.lineSpeater = lineSpeater;
	}

	public String getColumnSpeater() {
		return columnSpeater;
	}

	public void setColumnSpeater(String columnSpeater) {
		this.columnSpeater = columnSpeater;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Enumerated(EnumType.STRING)
	public EnumFileType getFileType() {
		return fileType;
	}

	public void setFileType(EnumFileType fileType) {
		this.fileType = fileType;
	}

	public String getCharacterSet() {
		return characterSet;
	}

	public void setCharacterSet(String characterSet) {
		this.characterSet = characterSet;
	}

	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(Integer connectionType) {
		this.connectionType = connectionType;
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

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public FileDataSource(String name, EnumFileType fileType, String characterSet, Boolean usePage, Integer pageSize,
			String host, Integer connectionType, String userName, String password, String filePath, Integer version,
			String lineSpeater, String columnSpeater) {
		super();
		this.name = name;
		this.fileType = fileType;
		this.characterSet = characterSet;
		this.usePage = usePage;
		this.pageSize = pageSize;
		this.host = host;
		this.connectionType = connectionType;
		this.userName = userName;
		this.password = password;
		this.filePath = filePath;
		this.version = version;
		this.lineSpeater = lineSpeater;
		this.columnSpeater = columnSpeater;
	}

	public FileDataSource() {
		super();
	}

}