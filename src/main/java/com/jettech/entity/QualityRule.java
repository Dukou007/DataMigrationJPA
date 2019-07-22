package com.jettech.entity;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.jettech.EnumQualityRuleType;

@Entity
@Table(name = "quality_rule")
public class QualityRule extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7131054368370275220L;
	private String name;
	private String des;
	private List<QualitySuite> qualitySuites;
	private EnumQualityRuleType ruleType;//规则类型
	private String andOr;//用于拼接where语句与前面规则是and或者or
	private String defaultValue;//值域
//	private String leftExpression;//左表达式 not in(
//	private String rightExpression;//右表达式 )
//	private String sqlPart;//sql片段

	public String getName() {
		return name;
	}

	@ManyToMany(mappedBy = "qualityRules")
	public List<QualitySuite> getQualitySuites() {
		return qualitySuites;
	}

	public void setQualitySuites(List<QualitySuite> qualitySuites) {
		this.qualitySuites = qualitySuites;
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

	@Enumerated(EnumType.STRING)	
	public EnumQualityRuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(EnumQualityRuleType ruleType) {
//		 List<Map<String,Object>> list = EnumQualityRuleType.toList();
//		 for(Map<String,Object> map:list){
//			 this.ruleType = map.get(ruleType.toString());
//		 }
		this.ruleType = ruleType;
	}
	@Column(columnDefinition="text")
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

//	public String getLeftExpression() {
//		return leftExpression;
//	}
//
//	public void setLeftExpression(String leftExpression) {
//		this.leftExpression = leftExpression;
//	}
//
//	public String getRightExpression() {
//		return rightExpression;
//	}
//
//	public void setRightExpression(String rightExpression) {
//		this.rightExpression = rightExpression;
//	}
//
//	public String getSqlPart() {
//		return sqlPart;
//	}
//
//	public void setSqlPart(String sqlPart) {
//		this.sqlPart = sqlPart;
//	}

	public String getAndOr() {
		return andOr;
	}

	public void setAndOr(String andOr) {
		this.andOr = andOr;
	}

}
