package com.jettech.domain;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestCase;

public class QualityCaseModel extends BaseModel {
	private String name;
	private String expertValue; // 期望值
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	private Integer pageSize = 0;
	private Boolean usePage = false;// 默认不分页

	private Integer testSuiteId;

	public QualityQueryModel getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(QualityQueryModel testQuery) {
		this.testQuery = testQuery;
	}

	private QualityQueryModel testQuery;
//	private QueryModel sourceQuery;
//	private QueryModel targetQuery;

	public QualityResultModel getTestResult() {
		return testResult;
	}

	public void setTestResult(QualityResultModel testResult) {
		this.testResult = testResult;
	}

	private QualityResultModel testResult;

	public QualityCaseModel() {
	}

	public QualityCaseModel(QualityTestCase qualityTestCase) throws Exception {
		this();
		if (qualityTestCase == null)
			throw new Exception("qualityTestCase is null.can't convert to model");
		// super.parse(qualityTestCase);//可能由于延迟加载的问题导致属性不能复制
		parseEntity(qualityTestCase);
		System.out.println("cast qualityTestCase entity to model name:" + name);
		this.testQuery=new QualityQueryModel(qualityTestCase.getQualityTestQuery());
		/*this.sourceQuery = new QueryModel(qualityTestCase.getSourceQuery());
		this.targetQuery = new QueryModel(qualityTestCase.getTargetQuery());*/

	}

	@Override
	public void parseEntity(BaseEntity entity) {
		QualityTestCase qualityTestCase = (QualityTestCase) entity;
		super.parseEntity(qualityTestCase);
		this.setName(qualityTestCase.getName());
		this.setIsSQLCase(qualityTestCase.getIsSQLCase());
//		this.setExpertValue(qualityTestCase.getExpertValue());
		this.setMaxResultRows(qualityTestCase.getMaxResultRows());
//		this.setPageSize(qualityTestCase.getPageSize());
		this.setUsePage(qualityTestCase.getUsePage());
		this.setVersion(qualityTestCase.getVersion());
//		this.setTestSuiteId(qualityTestCase.getTestSuite().getId());

		// this.getSourceQuery().parseEntity(qualityTestCase.getSourceQuery());
		// this.getTargetQuery().parseEntity(qualityTestCase.getTargetQuery());

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

	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	public Integer getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(Integer testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

//	public QueryModel getSourceQuery() {
//		return sourceQuery;
//	}
//
//	public void setSourceQuery(QueryModel sourceQuery) {
//		this.sourceQuery = sourceQuery;
//	}
//
//	public QueryModel getTargetQuery() {
//		return targetQuery;
//	}
//
//	public void setTargetQuery(QueryModel targetQuery) {
//		this.targetQuery = targetQuery;
//	}

/*	public ResultModel getTestResult() {
		return testResult;
	}

	public void setTestResult(ResultModel testResult) {
		this.testResult = testResult;
	}*/

	/*public QueryModel getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(QueryModel testQuery) {
		this.testQuery = testQuery;
	}*/
}
