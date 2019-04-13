package com.jettech.service;

import com.jettech.entity.TestRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestRoundService extends IService<TestRound, Integer> {

    List<TestRound> findBySuiteId(int SuiteId);

    Page<TestRound> findByTestSuiteName(String suiteName, int pageNum, int pageSize );

	Page<TestRound> findAllRoundBytestResultID(Integer testResultID, PageRequest pageable);

	Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, PageRequest pageable);

    TestRound selectTestRoundByTestSuiteId(Integer testSuiteId);

	Page<TestRound> findTestRoundBySuiteName(String suiteName, Pageable pageable);
	

}
