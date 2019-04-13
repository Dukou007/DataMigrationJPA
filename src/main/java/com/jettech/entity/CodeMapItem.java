package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "code_map_item")
public class CodeMapItem extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6230519510814815417L;

	private String orgValue;
	private String refValue;

	private CodeMap codeMap;
	
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
	@JoinColumn(name = "code_map_id")
	public CodeMap getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(CodeMap codeMap) {
		this.codeMap = codeMap;
	}
}
