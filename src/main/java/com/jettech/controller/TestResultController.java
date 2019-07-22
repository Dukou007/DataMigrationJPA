package com.jettech.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.EnumExecuteStatus;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ProductService;
import com.jettech.service.TestRoundService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestResultVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/testResult")
@Api(value = "Test--Result--Controller|测试结果的controller")
public class TestResultController {

	@Autowired
	TestSuiteService testSuiteService;

	@Autowired
	ITestReusltService testRuleService;

	@Autowired
	ProductService productService;

	@Autowired
	ITestReusltService testReSultService;

	@Autowired
	ITestCaseService testCaseService;

	@Autowired
	ITestResultItemService testResultItemService;

	@SuppressWarnings("unused")
	@Autowired
	private TestRoundService testRoundService;

	private static Logger log = LoggerFactory.getLogger(TestResultController.class);

	/**
	 * @description:根据caseID来查询
	 * @tips:null
	 * @param caseID
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午1:35:29
	 */
	@ApiOperation(value = "testCaseList页---点击轮次记录----根据案例的ID查找轮次记录。已作废，见findTestResultByCaseAndStartTimeAndEndTime", notes = "参数:"
			+ "测试案例名称:caseName;" + "测试集名称:suiteName;" + "测试状态:state;" + "开始时间:startTime;" + "结束时间:endTime;"
			+ "案例数量:caseCount;" + "失败数量:failCount;" + "通过数量:successCount;")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "caseID", value = "案例ID", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	@RequestMapping(value = "findAllTestResultByTestCaseID", method = RequestMethod.GET)
	public ResultVO findAllTestResultByCaseID(@RequestParam Integer caseID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

		try {
			Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			Page<TestResult> testResultList = testReSultService.findTestResultByTestCaseID(caseID, pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				testResultVO.setCaseName(testCaseService.findById(caseID).getName());
				testResultVOList.add(testResultVO);

			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("findAllTestResultByTestCaseID error", e);
			return new ResultVO(false, StatusCode.ERROR, "通过案例查询结果异常");
		}

	}

	/**
	 * @description:根据casename和testRoundID来查找testResult
	 * @tips:null
	 * @return
	 * @author:zhou_xiaolong in 2019年3月4日下午5:38:26
	 */
	@SuppressWarnings("serial")
	@ApiOperation(value = "根据casename和testRoundID来查找testResult")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "name", value = "案例name", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "testRoundId", value = "轮次的ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	@RequestMapping(value = "findTestResultByCasenameAndRoundID", method = RequestMethod.GET)
	public ResultVO findTestResultByCasenameAndRoundID(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam String testRoundId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize

	) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<TestResult> testResultList = null;
		try {
			TestCase testCase = testCaseService.findByCaseName(name);
			String CaseId = testCase.getId().toString();
			Specification<TestResult> specification = new Specification<TestResult>() {
				@Override
				public Predicate toPredicate(Root<TestResult> root, CriteriaQuery<?> query,
						CriteriaBuilder criteriaBuilder) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					if (org.apache.commons.lang.StringUtils.isNotBlank(CaseId)) {
						predicateList.add(criteriaBuilder.equal(root.get("caseId"), CaseId));
					}
					if (org.apache.commons.lang.StringUtils.isNotBlank(testRoundId)) {
						predicateList.add(criteriaBuilder.equal(root.get("testRoundId"), testRoundId));
					}
					return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
				}
			};
			testResultList = this.testReSultService.findAll(specification, pageable);
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				testResultVO.setCaseName(name);
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("findTestResultByCasenameAndRoundID error", e);
			return new ResultVO(false, StatusCode.ERROR, "请输入准确的case名称");
		}
	}

	/**
	 * @description:根据轮次的ID查询所有的结果并分页
	 * @tips:null
	 * @param testRoundId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午12:24:46
	 */
	@RequestMapping(value = "/findTestResultByTestRoundId", method = RequestMethod.GET)
	@ApiOperation(value = "根据轮次的ID查询所有的结果并分页", notes = "参数:" + "案例名称:caseName;" + "source数据量:sourceCount;"
			+ "target数据量:targetCount;" + "测试状态:execState;" + "开始时间:startTime;" + "结束时间:endTime;" + "相同行数:sameRow;"
			+ "不同行数:notSameRow;" + "不同数据值数量:notSameData;" + "结果:result")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testRoundId", value = "测试结果的ID", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findTestResultByTestRoundId(@RequestParam Integer testRoundId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageSize - 1, pageSize);
		try {
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			Page<TestResult> testResultList = testReSultService.findTestResultByIdOrderStartTime(testRoundId, pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				TestCase findById = testCaseService.findById(Integer.parseInt(testResult.getCaseId()));
				if (findById != null) {
					testResultVO.setCaseName(findById.getName());
				} else {
					testResultVO.setCaseName(null);
				}

				testResultVOList.add(testResultVO);
			}
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("findTestResultByTestRoundId error", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 * @description:查询所有的结果并分页
	 * @tips:null
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午9:13:07
	 */
	@ApiOperation(value = "查询所有的结果并分页")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	@RequestMapping(value = "findAllTestResult", method = RequestMethod.GET)
	public ResultVO findAllTestResult(
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		try {
			Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			Page<TestResult> testResultList = testReSultService.findAllByPage(pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);

				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("findAllTestResult error", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	@SuppressWarnings("serial")
	@RequestMapping(value = "findTestResultByCaseAndStartTimeAndEndTime", method = RequestMethod.GET)
	public ResultVO findTestResultByCaseAndStartTimeAndEndTime(@RequestParam String caseId,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "execState", required = false) EnumExecuteStatus execState,
			@RequestParam(value = "testRoundId", required = false) String testRoundId,
			@RequestParam(value = "targetData", required = false) String targetData,
			@RequestParam(value = "sourceData", required = false) String sourceData,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) throws ParseException {
		//转化日期格式
		if(startTime.length()!=0&&endTime.length()!=0) {
			startTime = startTime.replace("Z", " UTC");
			endTime = endTime.replace("Z", " UTC");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date ds = sdf.parse(startTime);
			Date de = sdf.parse(endTime);
			startTime = sdf1.format(ds);
			endTime = sdf1.format(de).substring(0, 11)+"23:59:59";	
		}
			
		try {
			Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
			Page<TestResult> testResultList = testReSultService.findTestResultByCaseAndStartTimeAndEndTime(caseId,
					startTime, endTime, execState, testRoundId, targetData, sourceData, pageable);
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				Integer id = Integer.valueOf(caseId);
				TestCase testCase = testCaseService.findById(id);
				if (testCase == null || testCase.equals("")) {
					testResultVO.setCaseName(null);
				} else {
					testResultVO.setCaseName(testCase.getName());
				}
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (NumberFormatException e) {
			log.error("findTestResultByCaseAndStartTimeAndEndTime error", e);
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		} catch (Exception e) {
			log.error("findTestResultByCaseAndStartTimeAndEndTime error", e);
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		}

	}

	@RequestMapping(value = "/findTestResultByCaseName", method = RequestMethod.GET)
	public ResultVO findTestResultByCaseName(
			@RequestParam(value = "caseName", defaultValue = "", required = false) String caseName,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestResult> testResultList = testReSultService.findTestResultByCaseName(caseName, pageable);
			ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				TestCase findById = testCaseService.findById(Integer.parseInt(testResult.getCaseId()));
				if (findById != null) {
					testResultVO.setCaseName(findById.getName());
				} else {
					testResultVO.setCaseName(null);
				}
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("根据案例名称查询报错" + caseName, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		}
	}

	/**
	 * 根据执行状态来查找对应的执行结果
	 * 
	 * @param state
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "findAllByExecState", method = RequestMethod.GET)
	public ResultVO findAllByExecState(@RequestParam(value = "state", defaultValue = "", required = false) String state,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
		try {
			Page<TestResult> testResultList = testReSultService.findAllByExecState(state, pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO testResultVO = new TestResultVO(testResult);
				TestCase findById = testCaseService.findById(Integer.parseInt(testResult.getCaseId()));
				if (findById != null) {
					testResultVO.setCaseName(findById.getName());
				} else {
					testResultVO.setCaseName(null);
				}
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (NumberFormatException e) {
			log.error("根据状态查询报错" + state, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		}
	}

	/**
	 * 根据案例id和源数据源查询
	 * 
	 * @param caseId
	 * @param dataSource
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "findByCaseIdAndSourceDataSource", method = RequestMethod.GET)
	public ResultVO findByCaseIdAndSourceDataSource(
			@RequestParam(value = "caseId", defaultValue = "", required = false) String caseId,
			@RequestParam(value = "sourceData", defaultValue = "", required = false) String sourceData,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
		try {
			Page<TestResult> testResultList = testReSultService.findByCaseIdAndSourceDataSource(caseId, sourceData,
					pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO vo = new TestResultVO(testResult);
				testResultVOList.add(vo);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("根据源数据源查询报错" + sourceData, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		}
	}

	/**
	 * 根据案例的id和目标数据源查询执行结果
	 * 
	 * @param caseId
	 * @param dataSource
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "findByCaseIdAndTargetDataSource", method = RequestMethod.GET)
	public ResultVO findByCaseIdAndTargetDataSource(
			@RequestParam(value = "caseId", defaultValue = "", required = false) String caseId,
			@RequestParam(value = "targetData", defaultValue = "", required = false) String targetData,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		ArrayList<TestResultVO> testResultVOList = new ArrayList<TestResultVO>();
		try {
			Page<TestResult> testResultList = testReSultService.findByCaseIdAndTargetDataSource(caseId, targetData,
					pageable);
			for (TestResult testResult : testResultList) {
				TestResultVO vo = new TestResultVO(testResult);
				testResultVOList.add(vo);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("根据目标数据源查询报错" + targetData, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询异常");
		}
	}

}
