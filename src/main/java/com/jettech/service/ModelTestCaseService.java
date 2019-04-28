package com.jettech.service;

import java.util.List;
import java.util.Map;

import com.jettech.BizException;
import com.jettech.entity.ModelTestCase;
import com.jettech.vo.ModelTestCaseVO;
import com.jettech.vo.ResultVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ModelTestCaseService extends IService<ModelTestCase, Integer> {

	String doTest(Integer testCaseId);

	ResultVO readSQLCase(Map<String, String> map) throws BizException;

	ModelTestCase findByNameAndSuite(String testCaseName, String testSuiteName);

	List<ModelTestCase> findByName(String name);

	String exportTestCase(String testCaseId);

	ModelTestCaseVO  getTestCaseDetail(Integer testCaseID);

	List<ModelTestCase> findAllTestCase(Integer testSuiteID);
	
	
	//根据测试集id查询案例 20190121
	Page<ModelTestCase> findBySuiteId(Integer testSuiteID, Pageable pageable);

	//根据测试集name查询所有案例 20190121
	Page<ModelTestCase> findBySuiteName(String suiteName,Pageable pageable);

	/**
	 * @Description: 根据测试集name查询所有案例分页
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年2月22日下午4:56:07
	 */
	Page<ModelTestCase> getAllTestCaseByPage(String name, Pageable pageable);

	/**
	 * @Description: 将测试案例规制到特定的测试集中
	 * @Tips: 将testSuiteID为null的案例付testSuiteID;
	 * @State: being used 
	 * @author:zhou_xiaolong in 2019年2月24日下午7:52:56
	 */
	void changeTestCasePosition(Integer testSuiteID, String testCaseIDS);



	/**
	 * @Description: 将测试集中的案例重新规制到无序待选状态
	 * @Tips: testSuiteID设为null;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月25日下午11:56:36
	 */
	void backDisorder(String testCaseIDS);

	void saveTestCaseVo(ModelTestCaseVO testCaseVO) throws BizException;

	void updateTestCase(ModelTestCaseVO testCaseVO) throws BizException;


}

