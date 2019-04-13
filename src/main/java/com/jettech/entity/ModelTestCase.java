package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jettech.EnumPageType;

@Entity
@Table(name = "model_test_case")
@Inheritance(strategy = InheritanceType.JOINED)
public class ModelTestCase extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -322207843896378526L;
	private String name;
	private String expertValue; // 期望值
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	private Integer pageSize = 0;
	private Boolean usePage = false;// 默认不分页

	private TestSuite testSuite;
	private EnumPageType pageType ; // 分页类型
	
	private Integer maxWaitSecond;//守护线程最大等待时间（如果不填，按照默认值60s）
	

	@Enumerated(EnumType.STRING)
	public EnumPageType getPageType() {
		return pageType;
	}

	public void setPageType(EnumPageType pageType) {
		this.pageType = pageType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpertValue() {
		return expertValue;
	}

	public void setExpertValue(String expertValue) {
		this.expertValue = expertValue;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Boolean getIsSQLCase() {
		return isSQLCase;
	}

	public void setIsSQLCase(Boolean isSQLCase) {
		this.isSQLCase = isSQLCase;
	}

	public Integer getMaxResultRows() {
		return maxResultRows;
	}

	public void setMaxResultRows(Integer maxResultRows) {
		this.maxResultRows = maxResultRows;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	@OneToOne
	@JoinColumn(name = "test_suite_id", nullable = true)
	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}


	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	public Integer getMaxWaitSecond() {
		return maxWaitSecond;
	}

	public void setMaxWaitSecond(Integer maxWaitSecond) {
		this.maxWaitSecond = maxWaitSecond;
	}
	
}
