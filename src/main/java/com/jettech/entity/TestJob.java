package com.jettech.entity;

import java.util.List;

import com.jettech.EnumExecuteStatus;
import com.jettech.domain.CaseModel;

/**
 * 测试中使用的调度作业
 * 
 * @author tan
 *
 */
public class TestJob extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2208218905030860712L;

	CaseModel testCase;
	// QueryModel targetQuery;
	// QueryModel sourceQuery;
	private volatile Integer pageIndex = 0;
	List<TestQueryField> pageFields;
	EnumExecuteStatus satus = EnumExecuteStatus.Ready;
	private Integer maxResultRows;

	public TestJob(CaseModel testCase, Integer pageIndex) {
		this.testCase = testCase;
		// this.sourceQuery=testCase.getSourceQuery();
		// this.targetQuery=testCase.getTargetQuery();
		this.maxResultRows = testCase.getMaxResultRows();
		this.pageIndex = pageIndex;
	}
	
	
}
