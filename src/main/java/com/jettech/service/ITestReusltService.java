package com.jettech.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.jettech.BizException;
import com.jettech.EnumExecuteStatus;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;


public interface ITestReusltService extends IService<TestResult, Integer> {
	TestResult saveOne(TestResult entity);

	// 分页带条件的查询
	public Page<TestResult> findPage(TestResult testResult, int pageNum, int pageSize);
	// 分页不带条件的查询
	//public Page<TestResult> findPage( int pageNum, int pageSize);
	//
	public Page<TestResult> findResultListByCaseId(String caseId, Pageable pageable);


	/**根据轮次id查询并分页
	 * @param testRoundID
	 * @param pageable
	 * @return
	 */
	Page<TestResult> findTestResultByIdOrderStartTime(Integer testRoundID,Pageable  pageable);

	Page<TestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable);

	/**根据keyvalue结果查询并分页
	 * @param keyValue
	 * @param pageable
	 * @return
	 */
	Page<TestResultItem> findByKeyValue(Integer keyValue, Pageable pageable);

	/**根据案例的id查询并分页
	 * @param caseID
	 * @param pageable
	 * @return
	 */
	Page<TestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable);

	Page<TestResult> findAll(Specification<TestResult> specification, Pageable pageable);

	Page<TestResult> findTestResultByCaseName(String caseName, Pageable pageable);

	/**根据执行状态查询并分页
	 * @param state
	 * @param pageable
	 * @return
	 */
	Page<TestResult> findAllByExecState(String state, Pageable pageable);

	Page<TestResult> findByCaseIdAndSourceDataSource(String caseId,String sourceData, Pageable pageable);
	Page<TestResult> findByCaseIdAndTargetDataSource(String caseId,String targetData, Pageable pageable);

	/**多条件混合查询并分页
	 * @param caseId
	 * @param startTime
	 * @param endTime
	 * @param execState
	 * @param testRoundId
	 * @param targetData
	 * @param sourceData
	 * @param pageable
	 * @return
	 */
	Page<TestResult> findTestResultByCaseAndStartTimeAndEndTime(String caseId, String startTime,
			String endTime, EnumExecuteStatus execState, String testRoundId, String targetData, String sourceData, Pageable pageable);

	List<TestResult> findByTestRoundId(Integer testRoundId);

	void exportMigrationResult(String testResultIds, HttpServletResponse res) throws BizException;

	
	TestResult findEndTimeByCaseId(Integer caseId);

	List<Map<String, Object>> findTestCaseStatus(Integer caseId);
}
