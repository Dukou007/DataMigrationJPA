package com.jettech.domain;

import com.jettech.EnumPageType;
import com.jettech.EnumTestCaseType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.TestCase;

public class CaseModel extends BaseModel {
	protected String name;
//	protected String expertValue; // 期望值
	protected String version; // 版本
	protected Boolean isSQLCase;
	protected Integer maxResultRows;
	protected Integer pageSize = 0;
	protected Boolean usePage = false;// 默认不分页
	protected EnumPageType pageType = EnumPageType.None; // 分页类型
	protected EnumTestCaseType testCaseType = EnumTestCaseType.None;
	protected Integer testSuiteId;

	private ResultModel testResult;

	private Integer maxWaitSecond;// 守护线程最大等待时间（如果不填，按照默认值60s）
	private static final Integer _DEFAULT_PAGE_SZIE = 10000;

	public CaseModel() {
	}

	public CaseModel(TestCase testCase) throws Exception {
		this();
		if (testCase == null)
			throw new Exception("testCase is null.can't convert to model");
		// super.parse(testCase);//可能由于延迟加载的问题导致属性不能复制
		parseEntity(testCase);
		System.out.println("cast testCase entity to model name:" + name);
	}

	@Override
	public void parseEntity(BaseEntity entity) {
		TestCase testCase = (TestCase) entity;
		super.parseEntity(testCase);
		this.name = testCase.getName();
		this.isSQLCase = testCase.getIsSQLCase();
//		this.expertValue = testCase.getExpertValue();
		this.maxResultRows = testCase.getMaxResultRows();
		this.usePage = testCase.getUsePage();
		// 当分页时判断是否有设置分页大小，如果无，则使用默认带下分页,分页类型默认使用key
		if (testCase.getUsePage() != null && testCase.getUsePage()) {
			if (testCase.getPageSize() == null || testCase.getPageSize() == 0) {
				this.pageSize = _DEFAULT_PAGE_SZIE;
			} else {
				this.pageSize = testCase.getPageSize();
			}
			if (testCase.getPageType() == null || testCase.getPageType() == EnumPageType.None) {
				this.pageType = EnumPageType.QueryKey;
			} else {
				this.pageType = testCase.getPageType();
			}
		}

		this.setVersion(testCase.getVersion());
		// if (testCase.getTestSuite() != null)
		// this.setTestSuiteId(testCase.getTestSuite().getId());
		this.setPageType(testCase.getPageType());
		this.setMaxWaitSecond(testCase.getMaxWaitSecond());
		this.testCaseType = testCase.getCaseType();
		// this.getSourceQuery().parseEntity(testCase.getSourceQuery());
		// this.getTargetQuery().parseEntity(testCase.getTargetQuery());
	}

	public String getName() {
		return name;
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

	public ResultModel getTestResult() {
		return testResult;
	}

	public void setTestResult(ResultModel testResult) {
		this.testResult = testResult;
	}

	public EnumPageType getPageType() {
		return pageType;
	}

	public void setPageType(EnumPageType pageType) {
		this.pageType = pageType;
	}

	public EnumTestCaseType getTestCaseType() {
		return testCaseType;
	}

	public void setTestCaseType(EnumTestCaseType testCaseType) {
		this.testCaseType = testCaseType;
	}

	public Integer getMaxWaitSecond() {
		return maxWaitSecond;
	}

	public void setMaxWaitSecond(Integer maxWaitSecond) {
		this.maxWaitSecond = maxWaitSecond;
	}

}
