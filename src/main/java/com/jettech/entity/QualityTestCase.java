package com.jettech.entity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="quality_test_case")
@Inheritance(strategy = InheritanceType.JOINED)
public class QualityTestCase extends BaseEntity {

	private static final long serialVersionUID = -322207843896378526L;
	
	private String name;
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	//private Integer pageSize = 0;
	private Boolean usePage = false;// 默认不分页
//	private TestSuite testSuite;
	private QualityTestQuery qualityTestQuery;
	//多对多案例对测试集
	private List<TestSuite> testSuites;
	
	private String caseCode;//案例编号

//	@ManyToOne
	//@JoinColumn(name="quality_test_suite_id")
//	@JoinColumn(name="test_suite_id")
//	public TestSuite getTestSuite() {
//		return testSuite;
//	}


//	public void setTestSuite(TestSuite testSuite) {
//		this.testSuite = testSuite;
//	}


	public QualityTestCase() {
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	//public Integer getPageSize() {
	//	return pageSize;
	//}
	
	//public void setPageSize(Integer pageSize) {
	//	this.pageSize = pageSize;
	//}


	public Boolean getUsePage() {
		return usePage;
	}

	

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	@OneToOne(fetch = FetchType.EAGER,cascade=CascadeType.PERSIST)
	@JoinColumn(name = "quality_test_query_id")
	public QualityTestQuery getQualityTestQuery() {
		return qualityTestQuery;
	}

	public void setQualityTestQuery(QualityTestQuery qualityTestQuery) {
		this.qualityTestQuery = qualityTestQuery;
	}

	@ManyToMany(mappedBy = "qualityTestCases")
	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	public void setTestSuites(List<TestSuite> testSuites) {
		this.testSuites = testSuites;
	}


	public String getCaseCode() {
		return caseCode;
	}


	public void setCaseCode(String caseCode) {
		this.caseCode = caseCode;
	}
}
