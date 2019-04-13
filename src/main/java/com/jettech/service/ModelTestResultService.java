package com.jettech.service;

import org.springframework.data.domain.Page;

import com.jettech.entity.ModelTestResult;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;

import org.springframework.data.domain.Pageable;


public interface ModelTestResultService extends IService<ModelTestResult, Integer> {
	ModelTestResult saveOne(ModelTestResult entity);

	// 分页带条件的查询
	public Page<ModelTestResult> findPage(ModelTestResult testResult, int pageNum, int pageSize);
	// 分页不带条件的查询
	//public Page<TestResult> findPage( int pageNum, int pageSize);
	//
	public Page<ModelTestResult> findResultListByCaseId(String caseId, Pageable pageable);


	Page<ModelTestResult> findTestResultByIdOrderStartTime(Integer testRoundID,Pageable  pageable);

	Page<ModelTestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable);

	Page<ModelTestResultItem> findByKeyValue(Integer keyValue, Pageable pageable);

	Page<ModelTestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable);

}
