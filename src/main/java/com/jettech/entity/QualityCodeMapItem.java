package com.jettech.entity;

import javax.persistence.*;

@Entity
@Table(name = "quality_code_map_item")
public class QualityCodeMapItem extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6230519510814815417L;

	private String orgValue;
	private String refValue;

	private QualityCodeMap qualityCodeMap;
	
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "quality_code_map_id")
	public QualityCodeMap getQualityCodeMap() {
		return qualityCodeMap;
	}

	public void setQualityCodeMap(QualityCodeMap qualityCodeMap) {
		this.qualityCodeMap = qualityCodeMap;
	}
}
