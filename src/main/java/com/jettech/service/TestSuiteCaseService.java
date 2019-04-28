package com.jettech.service;

import com.jettech.entity.TestSuiteCase;

public interface TestSuiteCaseService extends IService<TestSuiteCase, Integer>{

	Integer[] findCaseIdsBysuiteId(Integer suiteId);
	
	Integer CountCase(Integer suiteId);

	TestSuiteCase findByCaseIdAndSuiteId(Integer caseId, Integer suiteId);

}
