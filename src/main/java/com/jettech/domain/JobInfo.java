package com.jettech.domain;

public class JobInfo {
	
    /**
     * 任务名称
     */
	String name;
	
	/**
	 * 计划任务
	 */
	String cron;
	
	/**
	 * 源表sql
	 */
	String srcSql;
	
	/**
	 * 目标表
	 */
	String destTable;
	
	/**
	 * 目标表字段
	 */
	String destTableFields;
	
	/**
	 * 目标表主键
	 */
	String destTableKey;
	
	/**
	 * 目标表更新sql
	 */
	String destTableUpdate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getSrcSql() {
		return srcSql;
	}

	public void setSrcSql(String srcSql) {
		this.srcSql = srcSql;
	}

	public String getDestTable() {
		return destTable;
	}

	public void setDestTable(String destTable) {
		this.destTable = destTable;
	}

	public String getDestTableFields() {
		return destTableFields;
	}

	public void setDestTableFields(String destTableFields) {
		this.destTableFields = destTableFields;
	}

	public String getDestTableKey() {
		return destTableKey;
	}

	public void setDestTableKey(String destTableKey) {
		this.destTableKey = destTableKey;
	}

	public String getDestTableUpdate() {
		return destTableUpdate;
	}

	public void setDestTableUpdate(String destTableUpdate) {
		this.destTableUpdate = destTableUpdate;
	}

	public String getPrintInfo() {
		String info = String.format("name:%s,srcSql:%s,destTable:%s", name, srcSql, destTable);
		
		return info;
	}
	
}
