package com.jettech.service;

import com.jettech.EnumExecuteStatus;
import io.swagger.annotations.ApiParam;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.jettech.BizException;
import com.jettech.entity.QualityTestCase;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;

public interface IQualityTestCaseService extends
		IService<QualityTestCase, Integer> {

	String doTest(Integer testCaseId);

	ResultVO readSQLCase(Map<String, String> map) throws BizException;

	QualityTestCase findByNameAndSuite(String testCaseName, String testSuiteName);

	QualityTestCase findByName(String name);

	Page<QualityTestCase> findTestCaseByName(String name, Pageable pageable);

	void batchDelete(String testCaseIDs);

	List<QualityTestCase> findByCaseIDs(String ids);

	// 添加查找方法 20190318
	QualityTestCase getOneById(Integer id);

	Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable);

	QualityTestCase getOne(Integer id);

	List<QualityTestCase> findByQualityCaseIDs(String ids);

	void updateTestQualityCase(QualityTestCaseVO testCaseVO)
			throws BizException;

	void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO)
			throws BizException;

	// void changeTestCasePosition(Integer caseId, Integer suiteId);
	void changeTestCasePosition(List<Integer> caseId, Integer suiteId);

	// void backDisorder(Integer caseId, Integer suiteId);
	void backDisorder(List<Integer> caseId, Integer suiteId);

	List<QualityTestCase> findByTestSuitIdAndRoundId(Integer test_suite_id,
			Integer test_round_id);

	// 测试集id查不在其中的案例
	Page<QualityTestCase> findByNotSuiteId(Integer suiteId, Pageable pageable);

	// 案例名称模糊查询不在其中的测试集
	Page<QualityTestCase> findByNotCaseName(Integer suiteId, String name,
			Pageable pageable);

	// 查询当前测试集下所有的案例
	Page<QualityTestCase> findCaseBySuiteId(Integer suiteId, Pageable pageable);

	// 查询所有案例根据name
	Page<QualityTestCase> findByCaseName(Integer suiteId, String name,
			Pageable pageable);

	// 案例的複製
	public ResultVO copyTestCase(String testCaseIds);

	// 根据轮次Id查询所属案例
	Page<QualityTestCase> findCaseByTestRoundId(Integer suiteId,
			Pageable pageable);

	// 导出案例
	public void downloadQualityCheckedCaseConverToExcel(String ids,
			HttpServletResponse res) throws Exception;
	//导入案例
	public JSONObject uploadSQLCase( MultipartFile file,
			HttpServletRequest request) throws Exception;
    //导出不符合要求的案例明细
	public void exportFalseCase(Integer testCaseId,HttpServletResponse res);

	void insertRow(List<String> fieldNames, SXSSFWorkbook workbook,
			List<String> s);

	public Page<QualityTestCase> findCaseNotSuit(Integer suiteId, String name, EnumExecuteStatus exeState,Pageable pageable);



}
