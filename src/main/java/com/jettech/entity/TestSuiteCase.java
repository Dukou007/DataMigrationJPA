package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name="test_suite_case")
@Inheritance(strategy = InheritanceType.JOINED)
public class TestSuiteCase extends BaseEntity {

	private Integer suiteId;
	private Integer caseId;
	public Integer getSuiteId() {
		return suiteId;
	}
	public void setSuiteId(Integer suiteId) {
		this.suiteId = suiteId;
	}
	public Integer getCaseId() {
		return caseId;
	}
	public void setCaseId(Integer caseId) {
		this.caseId = caseId;
	}
	public TestSuiteCase() {
		super();
	}
	
	
	
	
}
