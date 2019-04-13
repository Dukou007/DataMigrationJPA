package com.jettech.entity;

/**
 * 规则集和规则表是多对多的关系
 */

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "quality_suite")
public class QualitySuite extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4196139070106925218L;
	private List<QualityRule> qualityRules;
	private List<QualityTestPoint> qualityTestPoints;
	private Integer leftValue;
	private Integer rightValue;
	private String AndOr;
	private String name;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "quality_rule_suite", joinColumns = @JoinColumn(name = "quality_suite_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "quality_rule_id", referencedColumnName = "id"))
	public List<QualityRule> getQualityRules() {
		return qualityRules;
	}

	public void setQualityRules(List<QualityRule> qualityRules) {
		this.qualityRules = qualityRules;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	//@JoinColumn(name = "quality_test_suite_id", referencedColumnName = "id")
	@JoinColumn(name = "quality_suite_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<QualityTestPoint> getQualityTestPoints() {
		return qualityTestPoints;
	}

	public void setQualityTestPoints(List<QualityTestPoint> qualityTestPoints) {
		this.qualityTestPoints = qualityTestPoints;
	}

	public String getAndOr() {
		return AndOr;
	}

	public void setAndOr(String andOr) {
		AndOr = andOr;
	}

}
