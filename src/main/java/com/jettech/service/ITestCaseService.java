package com.jettech.service;

import java.util.List;
import java.util.Map;

import com.jettech.BizException;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.TestCase;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.TestCaseVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITestCaseService extends IService<TestCase, Integer> {

	String doTest(Integer testCaseId);

	ResultVO readSQLCase(Map<String, String> map);

	TestCase findByNameAndSuite(String testCaseName, String testSuiteName);

	List<TestCase> findByName(String name);

	StringBuffer exportTestCase(Integer testCaseId);

	TestCaseVO  getTestCaseDetail(Integer testCaseID);

	List<TestCase> findAllTestCase(Integer testSuiteID) throws BizException;
	
	
	//根据测试集id查询案例 20190121
	Page<TestCase> findBySuiteId(Integer testSuiteID,String name, Pageable pageable);
	Page<TestCase> findBySuiteId(Integer testSuiteID,Pageable pageable);

	//根据测试集name查询所有案例 20190121
	Page<TestCase> findBySuiteName(String suiteName,Pageable pageable);

	/**
	 * @Description: 根据测试集name查询所有案例分页
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年2月22日下午4:56:07
	 */
	Page<TestCase> getAllTestCaseByPage(String name, Pageable pageable);

	/**
	 * @Description: 将测试案例规制到特定的测试集中
	 * @Tips: 将testSuiteID为null的案例付testSuiteID;
	 * @State: being used 
	 * @author:zhou_xiaolong in 2019年2月24日下午7:52:56
	 * @throws BizException 
	 */
	void changeTestCasePosition(Integer testSuiteID, String testCaseIDS) throws BizException;



	/**
	 * @Description: 将测试集中的案例重新规制到无序待选状态
	 * @Tips: testSuiteID设为null;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月25日下午11:56:36
	 * @param suiteId 
	 * @throws BizException 
	 */
	void backDisorder(String testCaseIDS, Integer suiteId) throws BizException;

	/**
	 * 新增迁移测试案例的接口
	 * @param testCaseVO
	 * @throws BizException
	 */
	void saveTestCaseVo(TestCaseVO testCaseVO) throws BizException;

	void updateTestCase(TestCaseVO testCaseVO) throws BizException;
	
	List<TestCase> findByCaseIDs(String ids);

	TestCase findByCaseName(String name);

	Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable);

	void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO) throws BizException;

	void updateTestQualityCase(QualityTestCaseVO testCaseVO, Integer testCaseId);

	void deleteQuanlityTestCaseBatch(String testCaseIDS);

	List<QualityTestCase> findByQualityCaseIDs(String ids);

	List<TestCase> findAllById(Integer id) throws BizException;

	List<TestCase> findTestCaseBysuiteId(Integer suiteId);

	Page<TestCase> findALLBySuiteId(Integer testSuiteID, String name, Pageable pageable);


}

