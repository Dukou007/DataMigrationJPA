package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.List;

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

}
