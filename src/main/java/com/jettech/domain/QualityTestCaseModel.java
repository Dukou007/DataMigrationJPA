package com.jettech.domain;

import com.jettech.EnumTestCaseType;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.TestCase;

public class QualityTestCaseModel extends CaseModel {
	private QualityQueryModel targetQuery;

	private QualityResultModel qualityTestResult;

	public QualityTestCaseModel() {
		super();
	}

	public QualityTestCaseModel(QualityTestCase testCase) throws Exception {
	//	super(testCase);
		/*this.targetQuery = new QualityQueryModel(testCase.getQualityTestQuery());
		this.targetQuery.setTestCaseType(testCaseType);*/
		super.setId(testCase.getId());
		super.setCreateUser(testCase.getCreateUser());
		super.setEditUser(testCase.getEditUser());
		super.setCreateTime(testCase.getCreateTime());
		super.setEditTime(testCase.getEditTime());
		super.setName(testCase.getName());
		super.setVersion(testCase.getVersion());
		super.setIsSQLCase(testCase.getIsSQLCase());
		super.setMaxResultRows(testCase.getMaxResultRows());
		super.setUsePage(testCase.getUsePage());
		super.setPageSize(0);
		super.setTestCaseType(EnumTestCaseType.QualityCheck);
		/*if(testCase!=null && testCase.getTestSuite()!=null){
			super.setTestSuiteId(testCase.getTestSuite().getId());
		}*/
		//修改为多对对关系
		if(testCase!=null && testCase.getTestSuites().size() > 0 ){
			super.setTestSuiteId(testCase.getTestSuites().get(0).getId());
		}
		this.targetQuery = new QualityQueryModel(testCase.getQualityTestQuery());

	}

	public QualityQueryModel getTargetQuery() {
		return targetQuery;
	}

	public void setTargetQuery(QualityQueryModel targetQuery) {
		this.targetQuery = targetQuery;
	}

	public QualityResultModel getTestQualityResult() {
		return qualityTestResult;
	}

	public void setTestQualityResult(QualityResultModel qualityTestResult) {
		this.qualityTestResult = qualityTestResult;
	}

}
