package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jettech.EnumOptType;

@Entity
@Table(name = "meta_history_item")
public class MetaHistoryItem extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7996926840947488329L;
	
	private String tableName;
	private EnumOptType optTableType;
	private EnumOptType optFieldType;
	private String orgFiledName;
	private String refFieldName;
	private String info;
	
	private String fieldTypeChangeDesc; 
	private String fieldName;
	private MetaHistory metaHistory;
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public EnumOptType getOptTableType() {
		return optTableType;
	}

	public void setOptTableType(EnumOptType optTableType) {
		this.optTableType = optTableType;
	}

	public EnumOptType getOptFieldType() {
		return optFieldType;
	}

	public void setOptFieldType(EnumOptType optFieldType) {
		this.optFieldType = optFieldType;
	}

	public String getOrgFiledName() {
		return orgFiledName;
	}

	public void setOrgFiledName(String orgFiledName) {
		this.orgFiledName = orgFiledName;
	}

	public String getRefFieldName() {
		return refFieldName;
	}

	public void setRefFieldName(String refFieldName) {
		this.refFieldName = refFieldName;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
	
	@JoinColumn(name = "meta_history_id", nullable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	public MetaHistory getMetaHistory() {
		return metaHistory;
	}

	public void setMetaHistory(MetaHistory metaHistory) {
		this.metaHistory = metaHistory;
	}

	public String getFieldTypeChangeDesc() {
		return fieldTypeChangeDesc;
	}

	public void setFieldTypeChangeDesc(String fieldTypeChangeDesc) {
		this.fieldTypeChangeDesc = fieldTypeChangeDesc;
	}
	

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
