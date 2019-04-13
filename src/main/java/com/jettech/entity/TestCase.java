package com.jettech.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jettech.EnumPageType;
import com.jettech.EnumTestCaseType;

@Entity
@Table(name = "test_case")
@Inheritance(strategy = InheritanceType.JOINED)
public class TestCase extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -322207843896378526L;
	private String name;
//	private String expertValue; // 期望值
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	// private String sourceDataSourceName;//源查询数据源名称
	// private String targetDataSourceName;//目标查询数据源名称
	// private Integer sourceDataSourceID;// 源查询数据源ID
	// private Integer targetDataSourceID;//目标查询数据源ID
	private Integer pageSize = 0;
	private Boolean usePage ;// 默认不分页
	// @ManyToMany(fetch = FetchType.LAZY)
	// // @Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
	// @JoinTable(name = "test_suite_case", joinColumns = @JoinColumn(name =
	// "test_suite_id", referencedColumnName = "id"), inverseJoinColumns =
	// @JoinColumn(name = "test_case_id", referencedColumnName = "id"))
	// private List<TestSuite> testSuites = new ArrayList<>();
	private EnumTestCaseType caseType = EnumTestCaseType.DataCompare;
/*
	private TestSuite testSuite;*/
	private TestQuery sourceQuery;// 源查询
	private TestQuery targetQuery;// 目标查询
	private EnumPageType pageType = EnumPageType.None; // 分页类型

	private Integer maxWaitSecond;// 守护线程最大等待时间（如果不填，按照默认值60s）

	@Enumerated(EnumType.STRING)
	public EnumTestCaseType getCaseType() {
		return caseType;
	}

	public void setCaseType(EnumTestCaseType caseType) {
		this.caseType = caseType;
	}

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

	public TestCase() {
		super();
	}

	public void setName(String name) {
		this.name = name;
	}

//	public String getExpertValue() {
//		return expertValue;
//	}
//
//	public void setExpertValue(String expertValue) {
//		this.expertValue = expertValue;
//	}

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

/*	@ManyToOne
	@JoinColumn(name = "test_suite_id", nullable = true)
	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}*/

	@OneToOne(optional = true)
	@JoinColumn(name = "source_query_id")
	public TestQuery getSourceQuery() {
		return sourceQuery;
	}

	public void setSourceQuery(TestQuery sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	@OneToOne(optional = true)
	@JoinColumn(name = "target_query_id")
	public TestQuery getTargetQuery() {
		return targetQuery;
	}

	public void setTargetQuery(TestQuery targetQuery) {
		this.targetQuery = targetQuery;
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

	// public String getSourceDataSourceName() {
	// return sourceDataSourceName;
	// }
	//
	//
	//
	// public void setSourceDataSourceName(String sourceDataSourceName) {
	// this.sourceDataSourceName = sourceDataSourceName;
	// }
	//
	//
	//
	// public String getTargetDataSourceName() {
	// return targetDataSourceName;
	// }
	//
	//
	//
	// public void setTargetDataSourceName(String targetDataSourceName) {
	// this.targetDataSourceName = targetDataSourceName;
	// }
	//
	//
	//
	//
	//
	//
	// public Integer getSourceDataSourceID() {
	// return sourceDataSourceID;
	// }
	//
	//
	//
	//
	//
	//
	// public void setSourceDataSourceID(Integer sourceDataSourceID) {
	// this.sourceDataSourceID = sourceDataSourceID;
	// }
	//
	//
	//
	//
	//
	//
	// public Integer getTargetDataSourceID() {
	// return targetDataSourceID;
	// }
	//
	//
	//
	//
	//
	//
	// public void setTargetDataSourceID(Integer targetDataSourceID) {
	// this.targetDataSourceID = targetDataSourceID;
	// }

	// public List<TestSuite> getTestSuites() {
	// return testSuites;
	// }
	//
	// public void setTestSuites(List<TestSuite> testSuites) {
	// this.testSuites = testSuites;
	// }

}
