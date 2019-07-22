package com.jettech.service;

import com.jettech.entity.QualityTestResult;
import com.jettech.entity.TestResult;
import com.jettech.vo.ResultVO;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IQualityTestResultService extends IService<QualityTestResult, Integer> {
	QualityTestResult saveOne(QualityTestResult entity);

	// 分页查询，不带条件
	public Page<QualityTestResult> findPage(int pageNum, int pageSize);

	// 分页查询，带条件,必须实现JpaSpecificationExecutor<TestResult>接口
	public Page<QualityTestResult> findPage(QualityTestResult qualityTestResult, int pageNum, int pageSize);

	//迁移过来新加字段  20190318
	QualityTestResult getOneById(Integer id);

	/**根据result和testcaseid查询，分页
	 * @param testCaseId
	 * @param result
	 * @param pageable
	 * @return
	 */
	Page<QualityTestResult> findByCaseIdAndResult(Integer testCaseId, Boolean result, Pageable pageable);

	void exportEvidence(String testResultIds,HttpServletResponse res);

	ResultVO findByTestRIdAndName(Integer testRoundId,String name,int pageNum,int pageSize);

	Page<QualityTestResult> findTestResultByQualityCaseIDAndStartTimeAndEndTime(Integer testCaseId, String startTime,
			String endTime, Pageable pageable);



}
