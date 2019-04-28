package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.entity.Product;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.entity.TestRound;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.TestRoundRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ProductService;
import com.jettech.service.TestRoundService;
import com.jettech.service.TestSuiteCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.PageResult;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestRoundVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/testRound")
@Api(value = "Test--Round--Controller|执行轮次结果")
public class TestRoundController {

	@Autowired
	TestRoundService testRoundService;

	@Autowired
	TestSuiteService testSuiteService;

	@Autowired
	ITestReusltService testRuleService;

	@Autowired
	ProductService productService;

	@Autowired
	ITestReusltService testReusltService;

	@Autowired
	ITestCaseService testCaseService;

	@Autowired
	ITestResultItemService testResultItemService;

	@Autowired
	private TestRoundRepository repository;

	@Autowired
	private TestSuiteCaseService testSuiteCaseService;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(TestRoundController.class);

	/**
	 * @description:根据测试集ID查询轮次结果，并按照时间desc排列
	 * @tips:null
	 * @param testSuiteName
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年2月28日上午10:51:54
	 */
	@ResponseBody
	@RequestMapping(value = "findAllRoundBytestSuiteID", method = RequestMethod.GET)
	@ApiOperation(value = "根据测试集ID查询轮次结果，并按照时间desc排列", notes = "参数:" + "测试集名称:suiteName;" + "测试状态:state;"
			+ "开始时间:startTime;" + "结束时间:endTime;" + "案例数量:caseCount;" + "失败数量:failCount;" + "通过数量:successCount;")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testSuiteID", value = "测试集名称", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findAllRoundBytestSuiteID(@RequestParam Integer testSuiteID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestRound> roundList = testRoundService.findAllRoundBytestSuiteID(testSuiteID, pageable);
			ArrayList<TestRoundVO> testRoundVOList = new ArrayList<TestRoundVO>();
			for (TestRound testRound : roundList) {
				TestRoundVO testRoundVO = new TestRoundVO(testRound);
				testRoundVOList.add(testRoundVO);
			}
			map.put("totalElements", roundList.getTotalElements());
			map.put("totalPages", roundList.getTotalPages());
			map.put("list", testRoundVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("根据suiteID查询错误为：", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}

	/**
	 * @description:根据开始时间和结束时间查找轮次结果
	 * @tips:null
	 * @param startTime
	 * @param endTime
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午12:18:21
	 */
	@ResponseBody
	@RequestMapping(value = "/findTestRoundOrderByTestSuiteIDAndStarTimeAndEndTime", method = RequestMethod.GET, produces = {
			"application/json;charset=utf-8" })
	@ApiOperation(value = "根据测试集ID、开始时间和结束时间查找轮次结果", notes = "参数:" + "测试集名称:suiteName;" + "测试状态:state;"
			+ "开始时间:startTime;" + "结束时间:endTime;" + "案例数量:caseCount;" + "失败数量:failCount;" + "通过数量:successCount;")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testSuiteID", value = "案例集ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findTestRoundOrderByTestSuiteIDAndStarTimeAndEndTime(
			@RequestParam String testSuiteID,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<TestRound>testRoundList=null;
		try {
			if(startTime==endTime) {
				testRoundList=testRoundService.findBySuiteIdAndStartTimeAndEndTime(testSuiteID,startTime,pageable);
			}else {
				testRoundList=testRoundService.findBySuiteIdAndStartTimeAndEndTime(testSuiteID,startTime,endTime,pageable);
				
			}
		ArrayList<TestRoundVO> testRoundVOList = new ArrayList<TestRoundVO>();	
		for (TestRound testRound : testRoundList) {
				TestRoundVO testRoundVO = new TestRoundVO(testRound);
				testRoundVOList.add(testRoundVO);
			}
			map.put("totalElements", testRoundList.getTotalElements());
			map.put("totalPages", testRoundList.getTotalPages());
			map.put("list", testRoundVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("根据roundID查询错误信息为：", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	// =============================

	/**
	 * 根据产品id查询案例集合 2019018
	 * 
	 * @param parentId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/CaseAmount/{parentId}", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO getParentCaseAmountByParentId(@PathVariable("parentId") Integer parentId) {
		Product product = productService.findById(parentId);
		List<TestSuite> testSuite = product.getTestSuites();
		int Amount = 0;
		for (TestSuite s : testSuite) {
			Integer[] caseIds = testSuiteCaseService.findCaseIdsBysuiteId(s.getId());
			ArrayList<TestCase> list = new ArrayList<TestCase>();
			for (Integer caseId : caseIds) {
				TestCase testCase = testCaseService.findById(caseId);
				list.add(testCase);
			}
			List<TestCase> aa = list;
			Amount += aa.size();
		}
		List<TestRoundVO> arryList = new ArrayList<TestRoundVO>();
		for (TestSuite s : testSuite) {
			Integer testSuiteId = s.getId();
			List<TestRound> rounds = testRoundService.findBySuiteId(testSuiteId);
			for (TestRound testRound : rounds) {
				TestRoundVO a = new TestRoundVO(testRound);
				arryList.add(a);
			}
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("arryList", arryList);
		map.put("Amount", Amount);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

	/*
	 * @ResponseBody
	 * 
	 * @RequestMapping(method = RequestMethod.POST, value =
	 * "selectResultList/{caseId}") public ResultVO
	 * selectResultListById(@PathVariable("caseId") int caseId) { List<TestResult>
	 * testResultList = testRuleService.findResultListByCaseId(caseId); return new
	 * ResultVO(true, StatusCode.OK, "查询成功", testResultList); }
	 */

	/**
	 * 根据test_round_id查询所有结果集并进行分页
	 * 
	 * @param testRoundId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "selectResultLi/{testRoundId}/{pageNum}/{pageSize}")
	public ResultVO selectResultLi(@PathVariable("testRoundId") int testRoundId, @PathVariable("pageNum") int pageNum,
			@PathVariable("pageSize") int pageSize) {
		TestResult testR = new TestResult();
		testR.setTestRoundId(testRoundId);
		// 1.根据条件查询标签
		Page<TestResult> labels = testReusltService.findPage(testR, pageNum, pageSize);
		// 2.创建分页的封装结果集
		PageResult<TestResult> pageResult = new PageResult<>(labels.getTotalElements(), labels.getContent());
		// 2.创建返回值并返回
		return new ResultVO(true, StatusCode.OK, "查询成功", pageResult);
	}

	/**
	 * =============================新改的逻辑部分 20190121============= 根据测试集的名称查询轮次
	 * 20190121
	 * 
	 * @param suiteName
	 * @return
	 */
	@RequestMapping(value = "/TestRound/suiteName", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO selectTestRoundVO(
			@RequestParam(value = "suiteName", defaultValue = "", required = false) String suiteName,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestRound> testRounds = testRoundService.findTestRoundBySuiteName(suiteName, pageable);
			ArrayList<TestRoundVO> roundList = new ArrayList<TestRoundVO>();
			for (TestRound testRound : testRounds) {
				TestRoundVO testRoundVO = new TestRoundVO(testRound);
				roundList.add(testRoundVO);
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testRounds.getTotalElements());
			map.put("totalPages", testRounds.getTotalPages());
			map.put("list", roundList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("根据测试集名称查询轮次报错" + suiteName, e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 * 根据测试集id查询案例 20190121
	 * 
	 * @param suiteId
	 * @return
	 */
	@RequestMapping(value = "/TestCase/{suiteId}", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO selectTestCaseBySuiteId(@PathVariable("suiteId") Integer suiteId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

		// 添加分页方法 20190122
		// Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<TestCase> testCaseList = testCaseService.findBySuiteId(suiteId, pageable);
		List<TestCaseVO> testCaseVOList = new ArrayList<TestCaseVO>();
		for (TestCase testCase : testCaseList) {
			TestCaseVO testCaseVO = new TestCaseVO(testCase);
			testCaseVOList.add(testCaseVO);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("totalElements", testCaseList.getTotalElements());
		map.put("totalPages", testCaseList.getTotalPages());
		map.put("list", testCaseVOList);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);

	}

	/**
	 * 根据测试集名称查询案例 20190121
	 * 
	 * @param suiteName
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/TestCase/suiteName", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO selectTestCaseBySuiteName(
			@RequestParam(value = "suiteName", defaultValue = "", required = false) String suiteName,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
		if (suiteName.equals("")) {
			PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
			Page<TestCase> testCase = testCaseService.findAllByPage(pageRequest);
			List<TestCaseVO> caseList = new ArrayList<TestCaseVO>();
			for (TestCase testCases : testCase) {
				TestCaseVO testCaseVO = new TestCaseVO(testCases);
				caseList.add(testCaseVO);
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testCase.getTotalElements());
			map.put("totalPages", testCase.getTotalPages());
			map.put("list", caseList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		}
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<TestCase> testCaseList = testCaseService.findBySuiteName(suiteName, pageable);
		List<TestCaseVO> testCaseVOList = new ArrayList<TestCaseVO>();
		for (TestCase testCase : testCaseList) {
			TestCaseVO testCaseVO = new TestCaseVO(testCase);
			testCaseVOList.add(testCaseVO);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("totalElements", testCaseList.getTotalElements());
		map.put("totalPages", testCaseList.getTotalPages());
		map.put("list", testCaseVOList);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

	/**
	 * 根据案例id查询结果集表 20190121 =====
	 * 
	 * @param caseId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/TestQuery/{caseId}", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO selectTestQueryByCaseId(@PathVariable("caseId") String caseId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<TestResult> result = testRuleService.findResultListByCaseId(caseId, pageable);
		return new ResultVO(true, StatusCode.OK, "查询成功", result);
	}

	/**
	 * 根据结果集ID查询子表 20190123
	 * 
	 * @param testResultId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/TestResultItem/{testResultId}", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO selectTestResultItemBytestResultId(@PathVariable("testResultId") Integer testResultId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		// Page<TestResultItem> TestResultItemList =
		// testResultItemService.getTestResultItemByTestResultId(testResultId,pageable);
		Page<TestResultItem> TestResultItemList = testResultItemService.findTestResultItemByTestResultID(testResultId,
				"", pageable);
		return new ResultVO(true, StatusCode.OK, "查询成功", TestResultItemList);
	}

}
