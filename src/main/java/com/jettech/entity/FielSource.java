package com.jettech.entity;

import com.jettech.EnumAccesslType;
import com.jettech.EnumFileType;
import com.jettech.ISource;

public class FielSource extends BaseEntity implements ISource {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7741202478317410795L;

	private EnumFileType fileType;
	private String encoding;
	private Boolean usePase;
	private Integer pageSize;

	private String remoteServer;
	private EnumAccesslType accessType;
	private String remoteUser;
	private String remotePass;
	private String remotePath;

	public EnumFileType getFileType() {
		return fileType;
	}

	public void setFileType(EnumFileType fileType) {
		this.fileType = fileType;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Boolean getUsePase() {
		return usePase;
	}

	public void setUsePase(Boolean usePase) {
		this.usePase = usePase;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public String getRemoteServer() {
		return remoteServer;
	}

	public void setRemoteServer(String remoteServer) {
		this.remoteServer = remoteServer;
	}

	public EnumAccesslType getAccessType() {
		return accessType;
	}

	public void setAccessType(EnumAccesslType accessType) {
		this.accessType = accessType;
	}

	public String getRemoteUser() {
		return remoteUser;
	}

	public void setRemoteUser(String remoteUser) {
		this.remoteUser = remoteUser;
	}

	public String getRemotePass() {
		return remotePass;
	}

	public void setRemotePass(String remotePass) {
		this.remotePass = remotePass;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}
}
