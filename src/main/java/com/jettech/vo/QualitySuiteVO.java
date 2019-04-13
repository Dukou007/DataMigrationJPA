package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityRule;
import com.jettech.entity.QualitySuite;

import java.util.List;

public class QualitySuiteVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7613554165882689491L;
	private Integer leftValue;
	private Integer rightValue;
	private String AndOr;
	private String name;
	private List<Integer> qualityRuleIds;
	public QualitySuiteVO(BaseEntity entity) {
		super(entity);
		QualitySuite e = (QualitySuite) entity;
		for(int i=0;i<e.getQualityRules().size();i++){
			QualityRule rule=e.getQualityRules().get(i);
			qualityRuleIds.add(rule.getId());
		}
	}
	public Integer getLeftValue() {
		return leftValue;
	}
	public void setLeftValue(Integer leftValue) {
		this.leftValue = leftValue;
	}
	public Integer getRightValue() {
		return rightValue;
	}
	public void setRightValue(Integer rightValue) {
		this.rightValue = rightValue;
	}
	public String getAndOr() {
		return AndOr;
	}
	public void setAndOr(String andOr) {
		AndOr = andOr;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Integer> getQualityRuleIds() {
		return qualityRuleIds;
	}
	public void setQualityRuleIds(List<Integer> qualityRuleIds) {
		this.qualityRuleIds = qualityRuleIds;
	}
	public QualitySuiteVO(Integer leftValue, Integer rightValue, String andOr,
                          String name, List<Integer> qualityRuleIds) {
		super();
		this.leftValue = leftValue;
		this.rightValue = rightValue;
		AndOr = andOr;
		this.name = name;
		this.qualityRuleIds = qualityRuleIds;
	}
	public QualitySuiteVO() {
		super();
	}
	
	
}
