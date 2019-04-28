package com.jettech.vo;

import com.jettech.entity.FileDataSource;

public class FileDataSourceVO extends BaseVO{
    /**
	 * 
	 */
	private static final long serialVersionUID = 5924059662143302538L;


	private String name; //名称
	
	private String fileType;//文件类型
	private String fileName;//文件名称
	private String characterSet;//字符集
	
	private Boolean usePage;//默认不分页
	
	private Integer pageSize;//分页大小
	private Integer pageNum;//页数
	private String host;//服务器(IP)
	
	private Integer connectionType;//连接协议类型 1:ftp,2:sftp,3:ssh
	
	private String userName;//连接用户名
	
	private String password;//连接密码
	
	private String filePath;//文件路径(相对路径)

	private Integer version;
	private Integer row;
	public FileDataSourceVO(FileDataSource fileDataSource) {
		super(fileDataSource);
		}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
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


	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getRow() {
		return row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public Integer getPageNum() {
		return pageNum;
	}

	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}

	

	public FileDataSourceVO(String name, String fileType, String fileName,
			String characterSet, Boolean usePage, Integer pageSize,
			Integer pageNum, String host, Integer connectionType,
			String userName, String password, String filePath, Integer version,
			Integer row) {
		super();
		this.name = name;
		this.fileType = fileType;
		this.fileName = fileName;
		this.characterSet = characterSet;
		this.usePage = usePage;
		this.pageSize = pageSize;
		this.pageNum = pageNum;
		this.host = host;
		this.connectionType = connectionType;
		this.userName = userName;
		this.password = password;
		this.filePath = filePath;
		this.version = version;
		this.row = row;
	}

	public FileDataSourceVO() {
		super();
	}

}
