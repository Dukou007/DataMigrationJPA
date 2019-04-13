package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityRule;
import com.jettech.entity.QualitySuite;

import java.util.List;

public class QualityRuleVO extends BaseVO {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3351708970870046802L;
	
	private String name;
	private String des;
	private List<Integer> qualitySuiteIds;
	
	public QualityRuleVO(BaseEntity entity) {
		super(entity);
		QualityRule e = (QualityRule) entity;
		for(int i=0;i<e.getQualitySuites().size();i++){
			QualitySuite suite=e.getQualitySuites().get(i);
			qualitySuiteIds.add(suite.getId());
		}
	}

	public QualityRuleVO() {
		super();
	}

	public QualityRuleVO(String name, String des, List<Integer> qualitySuiteIds) {
		super();
		this.name = name;
		this.des = des;
		this.qualitySuiteIds = qualitySuiteIds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public List<Integer> getQualitySuiteIds() {
		return qualitySuiteIds;
	}

	public void setQualitySuiteIds(List<Integer> qualitySuiteIds) {
		this.qualitySuiteIds = qualitySuiteIds;
	}
	
	
}
