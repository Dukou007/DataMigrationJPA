package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityCodeMapItem;

public class QualityCodeMapItemVO extends BaseVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 117335237588145810L;
	private String orgValue;
	private String refValue;
	private Integer codeMapId;
	
	public QualityCodeMapItemVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			QualityCodeMapItem e = (QualityCodeMapItem) entity;
			codeMapId = e.getQualityCodeMap().getId();
		}
	}
	public String getOrgValue() {
		return orgValue;
	}
	public void setOrgValue(String orgValue) {
		this.orgValue = orgValue;
	}
	public String getRefValue() {
		return refValue;
	}
	public void setRefValue(String refValue) {
		this.refValue = refValue;
	}
	public Integer getCodeMapId() {
		return codeMapId;
	}
	public void setCodeMapId(Integer codeMapId) {
		this.codeMapId = codeMapId;
	}
	public QualityCodeMapItemVO(String orgValue, String refValue, Integer codeMapId) {
		super();
		this.orgValue = orgValue;
		this.refValue = refValue;
		this.codeMapId = codeMapId;
	}
	public QualityCodeMapItemVO() {
		super();
	}
	
}
