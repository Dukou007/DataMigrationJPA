package com.jettech.vo;



import com.jettech.EnumOptType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.MetaHistory;
import com.jettech.entity.MetaHistoryItem;


public class MetaHistoryItemVO extends BaseVO{

	private static final long serialVersionUID = 4978479365316020678L;
	private String tableName;
	private EnumOptType optTableType;
	private EnumOptType optFieldType;
	private String orgFiledName;
	private String refFieldName;
	private String info;
	private Integer metaHistoryId;
	private String fieldName;
	//private MetaHistory metaHistory;
	private String fieldTypeChangeDesc; 
	public MetaHistoryItemVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			MetaHistoryItem e = (MetaHistoryItem) entity;
			metaHistoryId = e.getMetaHistory().getId();
		}
	}
	
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

	public Integer getMetaHistoryId() {
		return metaHistoryId;
	}

	public void setMetaHistoryId(Integer metaHistoryId) {
		this.metaHistoryId = metaHistoryId;
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
