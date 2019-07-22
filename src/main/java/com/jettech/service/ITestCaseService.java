package com.jettech.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.jettech.BizException;
import com.jettech.EnumCompareDirection;
import com.jettech.EnumDatabaseType;
import com.jettech.EnumExecuteStatus;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.TestCase;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.SycData;
import com.jettech.vo.TestCaseVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface ITestCaseService extends IService<TestCase, Integer> {

	String doTest(Integer testCaseId);

	ResultVO readSQLCase(Map<String, String> map);

	TestCase findByNameAndSuite(String testCaseName, String testSuiteName);

	List<TestCase> findByName(String name);

	StringBuffer exportTestCase(Integer testCaseId);

	TestCaseVO getTestCaseDetail(Integer testCaseID);

	List<TestCase> findAllTestCase(Integer testSuiteID);

	// 根据测试集id查询案例 20190121
	Page<TestCase> findBySuiteId(Integer testSuiteID, String name, Pageable pageable);

	Page<TestCase> findBySuiteId(Integer testSuiteID, Pageable pageable);

	// 根据测试集name查询所有案例 20190121
	Page<TestCase> findBySuiteName(String suiteName, Pageable pageable);

	
	Page<TestCase> getAllTestCaseByPage(String name, EnumCompareDirection enumCompareDirection,Integer suiteId, Pageable pageable);

	/**
	 * @Description: 将测试案例规制到特定的测试集中
	 * @Tips: 将testSuiteID为null的案例付testSuiteID;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月24日下午7:52:56
	 * @throws Exception
	 */
	void changeTestCasePosition(Integer testSuiteID, String testCaseIDS) throws Exception;

	/**
	 * @Description: 将测试集中的案例重新规制到无序待选状态
	 * @Tips: testSuiteID设为null;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月25日下午11:56:36
	 */
	void backDisorder(String testCaseIDS, Integer suiteId);

	/**
	 * 新增迁移测试案例的接口
	 * 
	 * @param testCaseVO
	 * @throws BizException
	 */
	TestCase saveTestCaseVo(TestCaseVO testCaseVO) throws BizException;

	void updateTestCase(TestCaseVO testCaseVO) throws BizException;

	List<TestCase> findByCaseIDs(String ids);

	TestCase findByCaseName(String name);

	Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable);

	void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO) throws BizException;

	void updateTestQualityCase(QualityTestCaseVO testCaseVO, Integer testCaseId);

	void deleteQuanlityTestCaseBatch(String testCaseIDS);

	List<QualityTestCase> findByQualityCaseIDs(String ids);

	List<TestCase> findAllById(Integer id) throws BizException;

	Integer countBySuiteId(Integer suiteId);

	List<TestCase> findByTestSuiteId(Integer testSuiteID);

	// Page<TestCase> findByTestSuiteIdNotInAndNameContaining(Integer
	// testSuiteID, String name, Pageable pageable);

	Page<TestCase> findALLBySuiteId(Integer testSuiteID, String name, EnumExecuteStatus exeState, Pageable pageable);

	void exportCheckedCase(String ids, HttpServletResponse res);

	ResultVO uploadTestCase(Map<String, String> map) throws BizException;

	String getDataTableName(String dataSourceName);

	Integer getTableCount(Integer id, String tableName) throws Exception;

	SycData getAdapterAndConnection(EnumDatabaseType dbType, String driver, String url, String port, String host,
	        String username, String pwd, String sid);

	ResultVO autoCreateCase(DataSource sourceDataSource, DataSource targetDataSource, String sourceTableName,
	        String targetTableName);

//	String createCaseByDataSource(String productName,String testSuiteName, Integer sorDataSourceId, Integer tarDataSourceId,
//	        List<DataTable> sourDataTable, List<DataTable> tarDataTable) throws Exception;

	String createCaseByDataSource(String productName, String testSuiteName, DataSchema sdataSchema,
	        DataSchema tdataSchema) throws BizException;

	ResultVO getAllTestCaseByPage(String name,String caseStatus,Integer pageNum, Integer pageSize,Integer testSuiteId, EnumCompareDirection enumCompareDirection);
		
}
