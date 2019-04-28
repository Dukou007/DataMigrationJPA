package com.jettech.domain;

import com.jettech.EnumFileType;
import com.jettech.entity.FileDataSource;

public class FileDataSourceModel extends BaseModel {
	public FileDataSourceModel(FileDataSource entity) {

		this.name = entity.getName();
		this.fileType = entity.getFileType();
		this.characterSet = entity.getCharacterSet();
		this.usePage = entity.getUsePage();
		this.pageSize = entity.getPageSize();
		this.host = entity.getHost();
		this.connectionType = entity.getConnectionType();
		this.userName = entity.getUserName();
		this.password = entity.getPassword();
		this.filePath = entity.getFilePath();
		this.lineSpeater = entity.getLineSpeater();
		this.columnSpeater = entity.getColumnSpeater();
		this.textQualifier = entity.getTextQualifier();
	}

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

	private String lineSpeater;// 换行符

	private String textQualifier;// 文本限定符
	// 其他方法

	public String getPrintInfo() {
		return String.format("fileType:%s,host:%s,connectionType:%s,username:%s,password:%s", this.fileType, this.host,
				this.connectionType, this.userName, this.password);
	}

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

	private String columnSpeater;// 分列符

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

}
