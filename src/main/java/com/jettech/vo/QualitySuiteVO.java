package com.jettech.vo;

import java.util.ArrayList;
import java.util.List;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityRule;
import com.jettech.entity.QualitySuite;

public class QualitySuiteVO extends BaseVO {

	private static final long serialVersionUID = 7613554165882689491L;
	
	private Integer leftValue;
	
	private Integer rightValue;
	
	private String AndOr;
	
	private String name;
	
	private String qualityRuleIds ;

	private List<String> qualityRuleDes;

	public QualitySuiteVO(BaseEntity entity) {
		super(entity);
		QualitySuite e = (QualitySuite) entity;
		StringBuilder s=new StringBuilder();
		for (int i = 0; i < e.getQualityRules().size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			QualityRule rule=e.getQualityRules().get(i);
			s.append(rule.getId());
			if (i != e.getQualityRules().size() - 1) {
				s.append(",");
			} 
		}
		qualityRuleIds=s.toString();
		List<String> des = new ArrayList<String>();
		for (QualityRule r : e.getQualityRules()){
			des.add(r.getDes());
		}
		qualityRuleDes = des;

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
	
	public String getQualityRuleIds() {
		return qualityRuleIds;
	}

	public void setQualityRuleIds(String qualityRuleIds) {
		this.qualityRuleIds = qualityRuleIds;
	}

	public QualitySuiteVO(Integer leftValue, Integer rightValue, String andOr,
                          String name, String qualityRuleIds) {
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


	public List<String> getQualityRuleDes() {
		return qualityRuleDes;
	}

	public void setQualityRuleDes(List<String> qualityRuleDes) {
		this.qualityRuleDes = qualityRuleDes;
	}
}
