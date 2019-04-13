
package com.jettech.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.model.loadbalancer.RandomLoadBalancerDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.jettech.BizException;
import com.jettech.entity.DataField;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.QualityTestResult;
import com.jettech.entity.QualityTestResultItem;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IMapperService;
import com.jettech.service.IQualityTestCaseService;
import com.jettech.service.ProductService;
import com.jettech.service.QualityTestResultItemService;
import com.jettech.service.TestQueryService;
import com.jettech.service.TestSuiteService;
import com.jettech.util.DateUtil;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.QualityTestQueryVO;
import com.jettech.vo.QualityTestResultItemVO;
import com.jettech.vo.QualityTestResultVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author zhou_xiaolong
 * @Description: testCase的curd，testcase来源于testSuite
 * @date: 2019年2月18日 下午10:12:53
 */
@RestController
@RequestMapping(value = "/qualityTestCase")
@Api(value = "数据质量的Quality---=Test--Case--Controller|用于testCase模块")
public class QualityTestCaseController {

	private static Logger log = LoggerFactory.getLogger(QualityTestCaseController.class);

//	@Autowired
//	IQualityTestCaseService testCaseService;
	/*
	 * @Autowired private IMapperService mapperServiceImpl;
	 */
	@SuppressWarnings("unused")
	@Autowired
	private IMapperService keyMapperServiceImpl;
	@SuppressWarnings("unused")
	@Autowired
	private ProductService productService;
	@Autowired
	private TestSuiteService testSuiteService;
	@SuppressWarnings("unused")
	@Autowired
	private TestQueryService testQueryService;

	@SuppressWarnings("unused")
	@Autowired
	private IDataSourceService dataSourceService;
	@Autowired
	QualityTestQueryRepository qualityTestQueryRepository;
	@Autowired
	private QualityTestCaseRepository qualityTestCaseRepository;
	@Autowired
	QualityTestResultRepository qualityTestResultRepository;
	@Autowired
	QualityTestResultItemService qualityTestResultItemService;
	@Autowired
	private IQualityTestCaseService qualityTestCaseService;
	@Autowired
	private QualityTestResultRepository repository;
	@Value("${file.filePath}")
	private String filePath;

	@ResponseBody
	@RequestMapping(value = "/doQualityTestCase", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "根据testCaseId来执行单条案例", notes = "根据testCaseId来执行单条案例")
	@ApiImplicitParam(paramType = "query", name = "testCaseId", dataType = "String", value = "测试案例ID")

	public ResultVO doQualityTestCase(@RequestParam Integer testCaseId) {
		try {
			qualityTestCaseService.doTest(testCaseId);
			return new ResultVO(true, StatusCode.OK, "开始执行");
		} catch (Exception e) {
			log.error("执行案例:" + testCaseId + " 测试异常", e);
			return new ResultVO(true, StatusCode.OK, "执行失败");
		}

	}

	@ResponseBody
	@RequestMapping(value = "/uploadMapper", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "上传文件")
	@ApiImplicitParams({

			@ApiImplicitParam(name = "file", dataType = "String", required = true, value = "文件"),
			@ApiImplicitParam(name = "request", dataType = "String", required = true, paramType = "query")

	})

	public JSONObject upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {

		JSONObject result = new JSONObject();

		// 数据库
		String productName = request.getParameter("productName");
		System.out.println("产品：" + productName);
		String testSuiteName = request.getParameter("testSuiteName");
		System.out.println("测试集：" + testSuiteName);

		// 文件名
		String fileName = file.getOriginalFilename();
		System.out.println("文件名： " + fileName);

		// 文件后缀
		String suffixName = fileName.substring(fileName.lastIndexOf("."));
		System.out.println("文件后缀名： " + suffixName);

		// 重新生成唯一文件名，用于存储数据库
		String newFileName = UUID.randomUUID().toString() + suffixName;
		System.out.println("新的文件名： " + newFileName);

		// 创建文件
		File dest = new File(filePath + newFileName);

		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		map.put("productName", productName);
		map.put("testSuiteName", testSuiteName);
		try {
			file.transferTo(dest);
			result.put("success", true);
			result.put("data", map);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (JSONObject) result.put("success", false);
	}

	/**
	 * @Description: 根据id查询
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月15日下午5:30:06
	 * @param id
	 * @return
	 * @throws BizException
	 */
	@RequestMapping(value = "findCaseByID", method = RequestMethod.GET)
	public ResultVO findCaseByID(@RequestParam Integer id) throws BizException {
		try {
			if (id == null) {
				throw new BizException("查询的ID不存在");
			}
			QualityTestCase qualityTestCase = qualityTestCaseService.findById(id);
			QualityTestCaseVO qualityTestCaseVO = new QualityTestCaseVO(qualityTestCase);
			return new ResultVO(true, StatusCode.OK, "查询成功", qualityTestCaseVO);
		} catch (Exception e) {
			log.error("根据ID查询失败报错：" + id, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/uploadSQLCase", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "上传SQLCase")
	@ApiImplicitParams({ @ApiImplicitParam(name = "file", dataType = "String", required = true, value = "上传案例"),
			@ApiImplicitParam(name = "request", dataType = "String", required = false, paramType = "query") })
	public JSONObject uploadSQLCase(@ApiParam(value = "上传案例", required = true) @RequestParam("file") MultipartFile file,
			HttpServletRequest request) throws BizException {

		JSONObject result = new JSONObject();

		// 数据库
		String productName = request.getParameter("productName");
		System.out.println("产品：" + productName);

		String testSuiteName = request.getParameter("testSuiteName");
		System.out.println("测试集：" + testSuiteName);
		TestSuite testSuite = testSuiteService.getByName(testSuiteName, productName);

		// 文件名
		String fileName = file.getOriginalFilename();
		System.out.println("文件名： " + fileName);

		// 文件后缀
		String suffixName = fileName.substring(fileName.lastIndexOf("."));
		System.out.println("文件后缀名： " + suffixName);

		// 重新生成唯一文件名，用于存储数据库
		String newFileName = UUID.randomUUID().toString() + suffixName;
		System.out.println("新的文件名： " + newFileName);

		// 创建文件
		File dest = new File(filePath + fileName);

		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		map.put("productName", productName);
		map.put("testSuiteName", testSuiteName);
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				log.info("临时文件:" + dest.getAbsolutePath());
				result.put("success", true);
				result.put("data", map);
				qualityTestCaseService.readSQLCase(map);
			} else {
				log.info("临时文件不存在:" + dest.getAbsolutePath());
				result.put("success", false);
				result.put("data", map);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return (JSONObject) result.put("success", false);
		}

	}

	/**
	 * 根据测试案例名称查找并分页
	 * 
	 * @param name
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/findAllQualityTestCaseByPage", method = RequestMethod.GET)
	@ApiOperation(value = "根据测试案例名称查找并分页", notes = "需要输入测试案例名称,不输入默认查询所有")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "name", value = "产品名称", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findAllQualityTestCaseByPage(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

		Map<String, Object> resultmap = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		long beginTime = (new Date()).getTime();
		try {
			Page<QualityTestCase> testCaseList = qualityTestCaseService.findAllTestCaseByPage(name, pageable);// findAllPage(pageNum,pageSize);
			List<QualityTestCaseVO> testCaseVOList = new ArrayList<QualityTestCaseVO>();
			for (QualityTestCase testCase : testCaseList) {
				QualityTestCaseVO testCaseVO = new QualityTestCaseVO(testCase);
				testCaseVOList.add(testCaseVO);
			}
			resultmap.put("totalElements", testCaseList.getTotalElements());
			resultmap.put("totalPages", testCaseList.getTotalPages());
			resultmap.put("list", testCaseVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		} finally {
			log.info("findAllQualityTestCaseByPage use:" + DateUtil.getEclapsedTimesStr(beginTime) + " name:" + name
					+ " pageNum:" + pageNum + " pageSize:" + pageSize);
		}
	}

	/**
	 * @Description:根据testSuiteID查询testCase集合
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年1月30日下午9:11:42
	 */

	/**
	 * 新增TestCase
	 * 
	 * @param TestSuite
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addTestCase", method = RequestMethod.POST)
	@ApiOperation(value = "新增一条testCase", notes = "级联操作")
	@ApiImplicitParam(name = "testCaseVO", value = "testCaseVO实体", required = true, dataType = "QualityTestCaseVO")
	public ResultVO addTestCase(@RequestBody QualityTestCaseVO testCaseVO) {
		try {
			qualityTestCaseService.saveQualityTestCaseVo(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "新增成功");
		} catch (Exception e) {
			log.error("新增异常："+testCaseVO, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败"+e.getLocalizedMessage() );
		}

	}

	/**
	 * 修改案例
	 * 
	 * @param TestCase
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateTestCase", produces = {
			"application/json;character=utf-8" }, method = RequestMethod.POST)
	public ResultVO updateTestCase(@RequestBody QualityTestCaseVO testCaseVO) {
		try {
			QualityTestCase qualityTestCase = qualityTestCaseService.findById(testCaseVO.getId());
			if(qualityTestCase==null) {
				return new ResultVO(false, StatusCode.ERROR, "要修改的案例不存在");
			}
			qualityTestCaseService.updateTestQualityCase(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "修改成功");
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "修改失败");
		}

	}

	/**
	 * 通过testCaseID删除testCase
	 * 
	 * @param testCaseIDS
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteQuanlityTestCaseBatch", method = RequestMethod.GET)
	@ApiOperation(value = "根据ID删除testCase", notes = "可批量删除")
	@ApiImplicitParam(paramType = "query", name = "testCaseIDS", value = "案例ID", required = true, dataType = "String")
	public ResultVO deleteQuanlityTestCaseBatch(String testCaseIDS) {
		qualityTestCaseService.batchDelete(testCaseIDS);
		return new ResultVO(true, StatusCode.OK, "删除成功");
	}

	/**
	 * @Description: 根据caseid导出当前的/全部testcase数据到Excel可以根据生成的时间来判断
	 * @tips:
	 * @author: zhou_xiaolong in 2019年3月5日下午10:27:26
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "resource" })
	@ApiOperation(value = "testcaselist页面下根据caseid导出当前的/全部testcase数据到Excel，可以根据生成的时间来判断")
	@ApiImplicitParam(paramType = "query", name = "ids", value = "id的值,String类型,可单可数组", dataType = "String", required = false)
	@ResponseBody
	@RequestMapping(value = "/downloadQualityCheckedCaseConverToExcel", method = RequestMethod.GET)
	public ResultVO downloadQualityCheckedCaseConverToExcel(
			@RequestParam(value = "ids", required = false, defaultValue = "") String ids, HttpServletResponse res)
			throws Exception {
		List<QualityTestCase> list = qualityTestCaseService.findByQualityCaseIDs(ids);
		ArrayList<QualityTestCaseVO> testCaseVOList = new ArrayList<QualityTestCaseVO>();
		for (QualityTestCase testCase : list) {
			QualityTestCaseVO testCaseVO = new QualityTestCaseVO(testCase);
			testCaseVOList.add(testCaseVO);
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		String fileName = "testCase" + ".xls";
		for (QualityTestCaseVO testCaseVO : testCaseVOList) {
			map.put("id", testCaseVO.getId());
			map.put("name", testCaseVO.getName());
			map.put("isSQLCase", testCaseVO.getIsSQLCase());
			map.put("version", testCaseVO.getVersion());
			map.put("usePage",testCaseVO.getUsePage());
			map.put("maxResultRows", testCaseVO.getMaxResultRows());
			map.put("createUser", testCaseVO.getCreateUser());
			map.put("createTime", testCaseVO.getCreateTime());
			map.put("editUser", testCaseVO.getEditUser());
			map.put("editTime", testCaseVO.getEditTime());

		}

		// 创建表格文件
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "TestResultItemDetial";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		HSSFRow header = sheet.createRow(0);
		// 设置表头名称
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("名称");
		header.createCell(2).setCellValue("是否SQL");
		header.createCell(3).setCellValue("最大结果数");
		header.createCell(4).setCellValue("版本");
		header.createCell(5).setCellValue("创建人");
		header.createCell(6).setCellValue("修改人");
		header.createCell(7).setCellValue("创建时间");
		header.createCell(8).setCellValue("修改时间");
		header.createCell(9).setCellValue("是否分页");
		// 设置表内容
		int rowIndex = 1;
		for (QualityTestCaseVO testCaseVO : testCaseVOList) {
			HSSFRow rowItem = sheet.createRow(rowIndex++);
			rowItem.createCell(0).setCellValue(testCaseVO.getId());
			rowItem.createCell(1).setCellValue(testCaseVO.getName());
			rowItem.createCell(2).setCellValue(testCaseVO.getIsSQLCase());
			rowItem.createCell(3).setCellValue(testCaseVO.getMaxResultRows());
			rowItem.createCell(4).setCellValue(testCaseVO.getVersion());
			rowItem.createCell(5).setCellValue(testCaseVO.getCreateUser());
			rowItem.createCell(6).setCellValue(testCaseVO.getEditUser());
			rowItem.createCell(7).setCellValue(testCaseVO.getCreateTime());
			rowItem.createCell(8).setCellValue(testCaseVO.getEditTime());
			rowItem.createCell(9).setCellValue(testCaseVO.getUsePage());
		}
		String filename = sheetName;
		res.reset(); // 非常重要
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		try {
			OutputStream out = res.getOutputStream();
			res.addHeader("Content-Disposition",
					"attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
			workbook.write(out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("……………………………………………………");
		return new ResultVO(true, StatusCode.OK, "下载成功");

	}

	@SuppressWarnings("serial")
	@RequestMapping(value = "findTestResultByQualityCaseIDAndStartTimeAndEndTime", method = RequestMethod.GET)
	@ApiOperation(value = "testCaseList页---点击执行记录----根据案例的ID查找轮次记录,跳转到testCaseResultList的界面，根据案例ID、开始时间，结束时间来查询测试结果,。", notes = "不输入开始时间和结束时间默认查找所有。")
	@ApiImplicitParams({

			@ApiImplicitParam(paramType = "query", name = "testCaseId", value = "案例集ID", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findTestResultByQualityCaseIDAndStartTimeAndEndTime(@RequestParam Integer testCaseId,
			@RequestParam(value = "startTime", required = false) String startTime,
			@RequestParam(value = "endTime", required = false) String endTime,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<QualityTestResult> testResultList = null;
		Specification<QualityTestResult> specification = new Specification<QualityTestResult>() {
			@Override
			public Predicate toPredicate(Root<QualityTestResult> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(startTime)) {
					predicateList.add(
							criteriaBuilder.greaterThanOrEqualTo(root.get("startTime").as(String.class), startTime));
				}
				if (StringUtils.isNotBlank(endTime)) {
					predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime").as(String.class), endTime));
				}
				if (StringUtils.isNotBlank(testCaseId.toString())) {
					predicateList.add(criteriaBuilder.equal(root.get("testCaseId").as(Integer.class), testCaseId));
				}
				return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
			}

		};

		testResultList = this.repository.findAll(specification, pageable);
		ArrayList<QualityTestResultVO> testResultVOList = new ArrayList<QualityTestResultVO>();
		for (QualityTestResult testResult : testResultList) {
			QualityTestResultVO testResultVO = new QualityTestResultVO(testResult);
			testResultVO.setTestCaseName(qualityTestCaseRepository.getOne(testCaseId).getName());
			testResultVOList.add(testResultVO);
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("totalElements", testResultList.getTotalElements());
		map.put("totalPages", testResultList.getTotalPages());
		map.put("list", testResultVOList);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

	/**
	 * @description:根据TestResultID来查询TestResultItem并分页
	 * @tips:null
	 * @param testResultID
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午5:53:12
	 */
	@RequestMapping(value = "/findQualityTestResultItemByQualityTestResultID", method = RequestMethod.GET)
	@ApiOperation(value = "在testCaseResultList界面，点击结果明细，到最终的界面。（根据TestResultID来查询TestResultItem）", notes = "参数:"
			+ "keyValue:keyValue;" + "result:result;" + "columnName:columnName;" + "sourceValue:sourceValue;"
			+ "targetValue:targetValue")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testResultID", value = "测试结果ID", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findQualityTestResultItemByQualityTestResultID(@RequestParam Integer testResultID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
		    @RequestParam(value = "result", defaultValue = "", required = false) String result) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<QualityTestResultItem> testResultItemList = qualityTestResultItemService
					.findTestResultItemByTestResultID(testResultID,result, pageable);
			ArrayList<QualityTestResultItemVO> testResultItemVOList = new ArrayList<QualityTestResultItemVO>();
			for (QualityTestResultItem testResultItem : testResultItemList) {
				QualityTestResultItemVO testResultItemVO = new QualityTestResultItemVO(testResultItem);
				testResultItemVOList.add(testResultItemVO);
			}
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("totalElements", testResultItemList.getTotalElements());
			map.put("totalPages", testResultItemList.getTotalPages());
			map.put("list", testResultItemVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("报错为:", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 * 批量复制
	 * 
	 * @param testCaseId
	 * @return
	 */
	@RequestMapping(value = "copyTestCase", method = RequestMethod.GET)
	public ResultVO copyTestCase(@RequestParam String testCaseIds) {
		try {
			if (StringUtils.isNotBlank(testCaseIds)) {
				String[] idS = testCaseIds.split(",");
				for (String testCaseId : idS) {
					QualityTestCase qualityTestCase = qualityTestCaseRepository.findById(Integer.parseInt(testCaseId)).get();
					QualityTestCaseVO qualityTestCaseVO = new QualityTestCaseVO();
					BeanUtils.copyProperties(qualityTestCase, qualityTestCaseVO);
					QualityTestQuery qualityTestQuery = qualityTestQueryRepository
							.findById(qualityTestCase.getQualityTestQuery().getId()).get();
					QualityTestQueryVO qualityTestQueryVO = new QualityTestQueryVO(qualityTestQuery);
					qualityTestQueryVO.setId(null);
					qualityTestQueryVO.setName(qualityTestCase.getName()+"_copy");
					qualityTestQueryVO.setCreateTime(new Date());
					qualityTestCaseVO.setQualityTestQueryVo(qualityTestQueryVO);
					qualityTestCaseVO.setId(null);
					qualityTestCaseVO.setName(qualityTestCase.getName()+"_copy");
					qualityTestCaseVO.setCreateTime(new Date());
					qualityTestCaseService.saveQualityTestCaseVo(qualityTestCaseVO);
				}
			}
			return new ResultVO(true, StatusCode.OK, "复制成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("复制失败报错为：", e);
			return new ResultVO(false, StatusCode.ERROR, "复制失败");
		}
	}

}
