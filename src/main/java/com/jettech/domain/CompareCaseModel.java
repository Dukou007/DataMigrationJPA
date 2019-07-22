package com.jettech.domain;

import com.jettech.EnumPageType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.CompareTestCase;
import com.jettech.entity.TestCase;

public class CompareCaseModel extends CaseModel {

	private static final Integer _DEFAULT_PAGE_SZIE = 10000;
	private QueryModel sourceQuery;
	private QueryModel targetQuery;

	public CompareCaseModel() {
	}


	public CompareCaseModel(TestCase testCase) throws Exception {
		super(testCase);
		// super.parse(testCase);//可能由于延迟加载的问题导致属性不能复制
		// parseEntity(testCase);
		// System.out.println("cast testCase entity to model name:" + name);
		this.sourceQuery = new QueryModel(testCase.getSourceQuery());
		this.sourceQuery.setTestCaseType(testCaseType);// 将案例类型赋值给查询,用于数据构造的处理
		this.targetQuery = new QueryModel(testCase.getTargetQuery());
		this.targetQuery.setTestCaseType(testCaseType);
	}

	@Override
	public void parseEntity(BaseEntity entity) {
		TestCase testCase = (TestCase) entity;
		super.parseEntity(testCase);
		this.setName(testCase.getName());
		this.setIsSQLCase(testCase.getIsSQLCase());
//		this.setExpertValue(testCase.getExpertValue());
		this.setMaxResultRows(testCase.getMaxResultRows());
		this.setUsePage(testCase.getUsePage());
		// 当分页时判断是否有设置分页大小，如果无，则使用默认带下分页
		if (testCase.getUsePage() != null && testCase.getUsePage()) {
			if (testCase.getPageSize() == null || testCase.getPageSize() == 0) {
				this.pageSize = _DEFAULT_PAGE_SZIE;
			} else {
				this.setPageSize(testCase.getPageSize());
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

		this.setMaxWaitSecond(testCase.getMaxWaitSecond());

		// this.getSourceQuery().parseEntity(testCase.getSourceQuery());
		// this.getTargetQuery().parseEntity(testCase.getTargetQuery());

	}

	public QueryModel getSourceQuery() {
		return sourceQuery;
	}

	public void setSourceQuery(QueryModel sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	public QueryModel getTargetQuery() {
		return targetQuery;
	}

	public void setTargetQuery(QueryModel targetQuery) {
		this.targetQuery = targetQuery;
	}

}
