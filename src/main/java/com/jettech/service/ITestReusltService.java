package com.jettech.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;


public interface ITestReusltService extends IService<TestResult, Integer> {
	TestResult saveOne(TestResult entity);

	// 分页带条件的查询
	public Page<TestResult> findPage(TestResult testResult, int pageNum, int pageSize);
	// 分页不带条件的查询
	//public Page<TestResult> findPage( int pageNum, int pageSize);
	//
	public Page<TestResult> findResultListByCaseId(String caseId, Pageable pageable);


	Page<TestResult> findTestResultByIdOrderStartTime(Integer testRoundID,Pageable  pageable);

	Page<TestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable);

	Page<TestResultItem> findByKeyValue(Integer keyValue, Pageable pageable);

	Page<TestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable);

	Page<TestResult> findAll(Specification<TestResult> specification, Pageable pageable);

	Page<TestResult> findTestResultByCaseName(String caseName, Pageable pageable);

	Page<TestResult> findAllByExecState(String state, Pageable pageable);

}
