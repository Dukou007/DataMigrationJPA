package com.jettech.service;

import com.jettech.entity.TestRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public interface TestRoundService extends IService<TestRound, Integer> {

    List<TestRound> findBySuiteId(int SuiteId);

    Page<TestRound> findByTestSuiteName(String suiteName, int pageNum, int pageSize );

	Page<TestRound> findAllRoundBytestResultID(Integer testResultID, PageRequest pageable);

	Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, Pageable pageable);

    TestRound selectTestRoundByTestSuiteId(Integer testSuiteId);

	Page<TestRound> findTestRoundBySuiteName(String suiteName, Pageable pageable);

    int updateWithVersion(int id, int successCount, Date endTime , int version);

	Page<TestRound> findBySuiteIdAndStartTimeAndEndTime(String testSuiteID, String startTime, String endTime,
			Pageable pageable);


	String exportQualityReport(Integer testRoundId, Integer testSuiteId,HttpServletResponse response);

}
