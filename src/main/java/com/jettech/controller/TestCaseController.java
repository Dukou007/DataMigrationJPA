package com.jettech.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.jettech.BizException;
import com.jettech.EnumCompareDirection;
import com.jettech.EnumExecuteStatus;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.Product;
import com.jettech.entity.TestCase;
import com.jettech.service.DataSchemaService;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IKeyMapperService;
import com.jettech.service.ProductService;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ITestTableService;
import com.jettech.service.TestQueryService;
import com.jettech.service.TestSuiteCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.util.DateUtil;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestQueryVO;
import com.jettech.vo.TestRuleVO;

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
@RequestMapping(value = "/testCase")
@Api(value = "Test--Case--Controller|用于testCase模块")
public class TestCaseController {

	private static Logger log = LoggerFactory.getLogger(TestCaseController.class);

	@Autowired
	ITestCaseService testCaseService;
	/*
	 * @Autowired private IMapperService mapperServiceImpl;
	 */
	@SuppressWarnings("unused")
	@Autowired
	private IKeyMapperService keyMapperServiceImpl;
	@SuppressWarnings("unused")
	@Autowired
	private ProductService productService;
	@Autowired
	private TestSuiteService testSuiteService;
	@SuppressWarnings("unused")
	@Autowired
	private TestQueryService testQueryService;
	@Autowired
	private TestSuiteCaseService testSuiteCaseService;
	@SuppressWarnings("unused")
	@Autowired
	private IDataSourceService dataSourceService;
	@Autowired
	private ITestReusltService testRusltService;
	@Autowired
	private ITestTableService testTableService;
	@Autowired
	private DataSchemaService dataSchemaService;

	/*
	 * @Autowired private TestQueryService testQueryService;
	 */

	@ResponseBody
	@RequestMapping(value = "/doTestCase", produces = { "application/json;charset=UTF-8" }, method = {
	        RequestMethod.POST, RequestMethod.GET })
	@ApiOperation(value = "根据testCaseId来执行单条案例", notes = "根据testCaseId来执行单条案例")
	@ApiImplicitParam(paramType = "query", name = "testCaseId", dataType = "String", value = "测试案例ID")
	public ResultVO doTestCase(@RequestParam Integer testCaseId) {
		try {
			testCaseService.doTest(testCaseId);
			return new ResultVO(true, StatusCode.OK, "开始执行");
		} catch (Exception e) {
			log.error("执行案例:" + testCaseId + " 测试异常", e);
			return new ResultVO(true, StatusCode.OK, "执行失败");
		}

	}

	@Value("${file.filePath}")
	private String filePath;


	// 上传案例uploadExcelCase
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/uploadExcelCase", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "导入迁移案例")
	@ApiImplicitParams({ @ApiImplicitParam(name = "file", dataType = "String", required = true, value = "导入迁移案例"),
	        @ApiImplicitParam(name = "request", dataType = "String", required = false, paramType = "query") })
	public JSONObject uploadTestCase(
	        @ApiParam(value = "导入迁移案例", required = true) @RequestParam("file") MultipartFile file,
	        HttpServletRequest request) throws BizException {

		JSONObject result = new JSONObject();
		// 数据库
		String productId = request.getParameter("productId");
		System.out.println("产品：" + productId);

		String testSuiteId = request.getParameter("testSuiteId");
		System.out.println("测试集：" + testSuiteId);

		// 文件名
		String fileName = file.getOriginalFilename();
		System.out.println("文件名： " + fileName);

		// 文件后缀
		String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
		System.out.println("文件后缀名： " + suffixName);

		// 重新生成唯一文件名，用于存储数据库
		String filePath = System.getProperty("user.dir");
		File dest = new File(filePath, fileName);

		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		map.put("productId", productId);
		map.put("testSuiteId", testSuiteId);
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				log.info("临时文件:" + dest.getAbsolutePath());

				if (suffixName.equals("txt")) {
					result.put("data", map);
					ResultVO resultVo = testCaseService.readSQLCase(map);
					result.put("success", resultVo.isFlag());
					result.put("message", resultVo.getMessage());
				} else if (suffixName.equals("xls") || suffixName.equals("xlsx")) {
					ResultVO resultVo = testCaseService.uploadTestCase(map);
					result.put("success", resultVo.isFlag());
					result.put("message", resultVo.getMessage());
				}
			} else {
				String msg = "临时文件不存在:" + dest.getAbsolutePath();
				log.info(msg);
				result.put("success", false);
				result.put("message", msg);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			String msg = e.getLocalizedMessage();
			result.put("success", false);
			result.put("message", msg);
			return (JSONObject) result;
		}

	}

	@ResponseBody
	@RequestMapping(value = "/getAllTestCaseByPage", method = RequestMethod.GET)
	@ApiOperation(value = "根据测试案例名称查找并分页", notes = "需要输入测试案例名称,不输入默认查询所有")
	@ApiImplicitParams({
	        @ApiImplicitParam(paramType = "query", name = "name", value = "产品名称", required = false, dataType = "String"),
	        @ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
	        @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO getAllTestCaseByPage(@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "caseStatus", defaultValue = "", required = false) String caseStatus,
	        @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
	        @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
	        @RequestParam(value = "testSuiteId", defaultValue = "", required = false) Integer testSuiteId,
	        @RequestParam(value = "enumCompareDirection", defaultValue = "", required = false) EnumCompareDirection enumCompareDirection) {
		
		String name1 = name.trim();
		name = name1;
		try {
			 return testCaseService.getAllTestCaseByPage(name1,caseStatus, pageNum, pageSize, testSuiteId, enumCompareDirection);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 * @Description: 查询所有的testCase
	 * @tips:
	 * 
	 * @author:zhou_xiaolong in 2019年2月21日下午3:08:50
	 */
	@ResponseBody
	@RequestMapping(value = "/findAllTestCase", method = RequestMethod.GET)
	@ApiOperation(value = "查询所有的案例并分页", notes = "查询所有的案例并分页")
	@ApiImplicitParams({
	        @ApiImplicitParam(paramType = "query", name = "pageNum", value = "第几页", required = false, dataType = "Long"),
	        @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findAllTestCase(
	        @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
	        @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestCase> testCaseList = testCaseService.findAllByPage(pageable);
			ArrayList<TestCaseVO> voList = new ArrayList<TestCaseVO>();
			if (testCaseList.getSize() > 0) {
				for (TestCase testCase : testCaseList) {
					if (testCase.getId() > 0 && testCase.getTargetQuery() != null
					        && testCase.getSourceQuery() != null) {
						TestCaseVO vo = new TestCaseVO();
						BeanUtils.copyProperties(testCase, vo);
						vo.setId(testCase.getId());
						vo.setSourceDataSourceID(testCase.getSourceQuery().getDataSource().getId());
						vo.setSourceDataSourceName(testCase.getSourceQuery().getDataSource().getName());
						vo.setSourceQueryID(testCase.getSourceQuery().getId());
						vo.setTargetDataSourceID(testCase.getTargetQuery().getDataSource().getId());
						vo.setTargetDataSourceName(testCase.getTargetQuery().getDataSource().getName());
						vo.setTargetQueryID(testCase.getTargetQuery().getId());
						voList.add(vo);
					}
				}
				resultMap.put("totalPages", testCaseList.getTotalPages());
				resultMap.put("totalElements", testCaseList.getTotalElements());
				resultMap.put("list", voList);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (BeansException e) {
			e.printStackTrace();
			log.error("", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}


	/**
	 * 新增TestCase
	 * 
	 * @param TestSuite
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addTestCase", method = RequestMethod.POST)
	@ApiOperation(value = "新增一条testCase", notes = "级联操作")
	@ApiImplicitParam(name = "testCaseVO", value = "testCaseVO实体", required = true, dataType = "TestCaseVO")
	public ResultVO addTestCase(@RequestBody TestCaseVO testCaseVO) {
		try {

			testCaseService.saveTestCaseVo(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "新增成功");
		} catch (Exception e) {
			log.error("新增案例异常", e);
			// e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败" + e.getLocalizedMessage());
		}

	}

	/**
	 * @Description: 复制一条testcase案例
	 * @tips:处理好testCaseID，设为null
	 * 
	 * @author:zhou_xiaolong in 2019年1月29日下午4:40:34
	 */
	@ResponseBody
	@RequestMapping(value = "/copyTestCase", produces = {
	        "application/json;charset=utf-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "复制一条案例", notes = "testCaseID设为null")
	@ApiImplicitParam(paramType = "Integer", name = "testCaseID", value = "案例ID", required = true, dataType = "Integer")
	public ResultVO copyTestCase(@RequestParam(value = "testCaseID", required = true) Integer testCaseID) {
		try {
			TestCaseVO testCaseVO = testCaseService.getTestCaseDetail(testCaseID);
			testCaseVO.setId(null);
			testCaseVO.setCreateTime(new Date());
			testCaseVO.setName(testCaseVO.getName() + "_copy");
			TestQueryVO sourceQuery = testCaseVO.getSourceQuery();
			if (sourceQuery != null) {
				sourceQuery.setId(null);
				sourceQuery.setCreateTime(new Date());
				List<TestRuleVO> testRules = sourceQuery.getTestRules();
				for (TestRuleVO testRuleVO : testRules) {
					testRuleVO.setId(null);
					testRuleVO.setCreateTime(new Date());

				}
			}
			TestQueryVO targetQuery = testCaseVO.getTargetQuery();
			if (targetQuery != null) {
				targetQuery.setId(null);
				targetQuery.setCreateTime(new Date());
				List<TestRuleVO> testRules = targetQuery.getTestRules();
				for (TestRuleVO testRuleVO : testRules) {
					testRuleVO.setId(null);
					testRuleVO.setCreateTime(new Date());
				}
			}
			testCaseService.saveTestCaseVo(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "复制成功");
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "复制失败");
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
	@ApiOperation(value = "修改案例", notes = "根据testCaseID更改案例")
	@ApiImplicitParams({
	        @ApiImplicitParam(paramType = "TestCaseVO", name = "testCaseVO", value = "修改testCaseVO", required = true, dataType = "TestCaseVO") })
	public ResultVO updateTestCase(@RequestBody TestCaseVO testCaseVO) {
		try {
			TestCase testCase = testCaseService.findById(testCaseVO.getId());
			if (testCase == null) {
				return new ResultVO(false, StatusCode.ERROR, "要修改的案例不存在");
			}
			testCaseService.updateTestCase(testCaseVO);
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
	@RequestMapping(value = "/deleteCase", method = RequestMethod.GET)
	@ApiOperation(value = "根据ID删除testCase", notes = "可批量删除")
	@ApiImplicitParam(paramType = "query", name = "testCaseIDS", value = "案例ID", required = true, dataType = "String")
	public ResultVO deleteCase(String testCaseIDS) {
		try {
			if (StringUtils.isNotBlank(testCaseIDS)) {
				String[] list = testCaseIDS.split(",");
				for (int i = 0; i < list.length; i++) {
					int id = Integer.parseInt(list[i]);
					testCaseService.delete(id);
					testSuiteCaseService.deleteByCaseId(id);
				}
				return new ResultVO(true, StatusCode.OK, "删除成功");
			} else {
				return new ResultVO(false, StatusCode.ERROR, "删除失败");
			}
		} catch (Exception e) {
			log.error("删除失败", e);
			return new ResultVO(false, StatusCode.ERROR, "删除失败" + e.getLocalizedMessage());
		}

	}

	/**
	 * 查看案例详情列表
	 */
	@ResponseBody
	@RequestMapping(value = "getTestCaseDetail", produces = {
	        "application/json;charset=utf-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "查看案例详情", notes = "根据ID查看案例详情")
	@ApiImplicitParam(paramType = "Integer", name = "testCaseID", value = "案例ID", required = true, dataType = "Integer")
	public ResultVO getTestCaseDetail(@RequestParam(value = "testCaseID", required = true) Integer testCaseID) {
		TestCaseVO testCase = new TestCaseVO();
		try {
			testCase = testCaseService.getTestCaseDetail(testCaseID);
			return new ResultVO(true, StatusCode.OK, "查询成功", testCase);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}

	/**
	 * 查询所有的案例，不再指定案例集中根据
	 * 
	 * @param testSuiteID
	 * @param name
	 * @param pageNum
	 * @param pageSize
	 * @return 左侧 findAllNotInSuite
	 */
	@ResponseBody
	@RequestMapping(value = "/findAllNotInSuite", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public ResultVO findAllNotInSuite(@RequestParam(value = "testSuiteID") Integer testSuiteID,
	        @RequestParam(value = "name", required = false) String name,//exeState
            @RequestParam(value = "exeState",defaultValue = "", required = false) EnumExecuteStatus exeState,
	        @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
	        @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestCase> list = testCaseService.findALLBySuiteId(testSuiteID, name,exeState, pageable);
			List<TestCaseVO> voList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : list) {
				TestCaseVO vo = new TestCaseVO(testCase);
				voList.add(vo);
			}
			resultMap.put("totalPages", list.getTotalPages());
			resultMap.put("totalElements", list.getTotalElements());
			resultMap.put("list", voList);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
		return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
	}

	/**
	 * suiteid，案例名称查询指定的案例集中的案例，未在指定的集合中
	 * 
	 * @param suiteId
	 * @param name
	 * @param pageNum
	 * @param pageSize
	 * @return 右侧 getTestCaseListByTestSuiteID
	 */
	@RequestMapping(value = "/getTestCaseListByTestSuiteID", method = RequestMethod.GET)
	public ResultVO getTestCaseListByTestSuiteID(@RequestParam(value = "testSuiteID") Integer testSuiteID,
	        @RequestParam(value = "name", required = false) String name,
	        @RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
	        @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Page<TestCase> list = testCaseService.findBySuiteId(testSuiteID, name, pageable);
			ArrayList<TestCaseVO> voList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : list) {
				TestCaseVO vo = new TestCaseVO(testCase);
				voList.add(vo);
			}
			resultMap.put("totalPages", list.getTotalPages());
			resultMap.put("totalElements", list.getTotalElements());
			resultMap.put("list", voList);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("查询报错：", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
		return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
	}

	/**
	 * <h1>以下为自动生成案例代码 1、调用metaData/getAllDataSource(源和目标) 2、通过选择源，展示该源下所有的表
	 * 3、选择表进行比较,生成案例 4、生成案例
	 * 
	 * @return
	 */
	// 2、查询源下面默认的库的表
	@ResponseBody
	@RequestMapping(value = "/getTableByDateSource", produces = { "application/json;charset=UTF-8" })
	public String getTableByDateSource(@RequestParam("dataSourceName") String dataSourceName) {
		JSONObject JsonObject = new JSONObject();

		String dataTableName = testCaseService.getDataTableName(dataSourceName);
		JsonObject.put("tableName", dataTableName);
		return JsonObject.toJSONString();
	}

	// 3.1、选择表进行比较,生成总数案例
	@ResponseBody
	@RequestMapping(value = "/compareTwoTable", produces = { "application/json;charset=UTF-8" })
	public String choiceTwoTable(String sourceTableName, String targetTableName) throws Exception {
		JSONObject json = new JSONObject();
		DataTable sourceDataTable = testTableService.findByName(sourceTableName);
		DataTable targetDataTable = testTableService.findByName(targetTableName);
		// 处理源表
		DataSource sourceDataSource = sourceDataTable.getDataSchema().getDataSource();
		Integer sourceCount = testCaseService.getTableCount(sourceDataSource.getId(), sourceTableName);
		// 处理目标表
		DataSource targetDataSource = targetDataTable.getDataSchema().getDataSource();
		Integer targetCount = testCaseService.getTableCount(targetDataSource.getId(), targetTableName);
		json.put("sourceCount", sourceCount);
		json.put("targetCount", targetCount);
		return json.toJSONString();
	}

	// 3.2、选择表进行比较,生成明细案例
	// 3.3（临时修改选择源下的默认库，所有的表生成案例）
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/choiceTwoTableDetail", produces = { "application/json;charset=UTF-8" })
	public String choiceTwoTableDetail(@RequestParam("sourceTableName") String sourceTableName,
	        @RequestParam("targetTableName") String targetTableName) throws Exception {
		JSONObject json = new JSONObject();
		DataTable sourceDataTable = testTableService.findByName(sourceTableName);
		DataTable targetDataTable = testTableService.findByName(targetTableName);
		// 处理源表
		DataSource sourceDataSource = sourceDataTable.getDataSchema().getDataSource();
		// 处理目标表
		DataSource targetDataSource = targetDataTable.getDataSchema().getDataSource();
		// 自动生成案例
		ResultVO result = testCaseService.autoCreateCase(sourceDataSource, targetDataSource, sourceTableName,
		        targetTableName);
		json.put("status", "success");
		return json.toJSONString();
	}

	@ResponseBody
	@RequestMapping(value = "/createCaseByTwoSchema", produces = { "application/json;charset=UTF-8" })
	public String createTestCaseByTwoDataschema(@RequestParam("sourceDataSchemaName") String sourceDataSchemaName,
	        @RequestParam("targetDataSchemaName") String targetDataSchemaName,
	        @RequestParam("productName") String productName, @RequestParam("testSuiteName") String testSuiteName) {
		JSONObject json = new JSONObject();
		try {
			log.info("开始自动生成迁移测试案例,源库[" + sourceDataSchemaName + "],目标库[" + targetDataSchemaName + "]");

			// 源库模型
			DataSchema sdataSchema = dataSchemaService.findByName(sourceDataSchemaName);
			// 目标库模型
			DataSchema tdataSchema = dataSchemaService.findByName(targetDataSchemaName);

			// 产品
			String product = null;
			if (productName == null || productName.trim().isEmpty()) {
				List<Product> allProducts = productService.findAll();
				if (allProducts != null && allProducts.size() > 0) {
					product = allProducts.get(0).getName();// 产品名称为空,使用随机产品名称
				} else {
					throw new BizException("未找到任何产品");
				}
			}

			// 测试集
			String suiteName = null;
			if (testSuiteName == null || testSuiteName.trim().isEmpty()) {
				// 测试集名称为空,使用来源数据库名称+时间戳
				suiteName = sourceDataSchemaName + "_"
				        + DateUtil.getDateStr(DateUtil.getNow(), DateUtil.TIME_PATTREN_COMPACTED2);
			} else {
				suiteName = testSuiteName;
			}

			String result = testCaseService.createCaseByDataSource(productName, testSuiteName, sdataSchema,
			        tdataSchema);
			
			json.put("status", result);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json.toJSONString();
	}

	// 3.3（临时修改选择源下的默认库，所有的表生成案例）
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/choiceSourceToCase", produces = { "application/json;charset=UTF-8" })
	public String choiceDataSourceCreateCase(@RequestParam("sourceDataSchemaName") String sourceDataSchemaName,
	        @RequestParam("targetDataSchemaName") String targetDataSchemaName,
	        @RequestParam("productName") String productName, @RequestParam("testSuiteName") String testSuiteName)
	        throws Exception {
		JSONObject json = new JSONObject();
		try {
			log.info("开始自动生成迁移测试案例,源库[" + sourceDataSchemaName + "],目标库[" + targetDataSchemaName + "]");
			// 源操作：
			DataSchema sdataSchema = dataSchemaService.findByName(sourceDataSchemaName);
			System.out.println(sdataSchema.getId());
			Integer sourceDataSourceId = sdataSchema.getDataSource().getId();
			System.out.println(sdataSchema.getDataSource().getId());
			List<DataTable> sourDataTableList = testTableService.findBySchemaId(sdataSchema.getId());

			// 目标操作:
			DataSchema tdataSchema = dataSchemaService.findByName(targetDataSchemaName);
			System.out.println(tdataSchema.getId());
			Integer targetDataSourceId = tdataSchema.getDataSource().getId();
			System.out.println(targetDataSourceId);
			List<DataTable> tarDataTableList = testTableService.findBySchemaId(tdataSchema.getId());

			log.info("自动生成迁移测试案例,源库数据源[" + sourceDataSourceId + "]名称[" + sdataSchema.getDataSource().getName()
			        + "],目标库数据源[" + targetDataSchemaName + "]名称[" + tdataSchema.getDataSource().getName() + "]");
			// 生成案例
			String product = null;
			if (productName == null || productName.trim().isEmpty()) {
				List<Product> allProducts = productService.findAll();
				if (allProducts != null && allProducts.size() > 0) {
					product = allProducts.get(0).getName();// 产品名称为空,使用随机产品名称
				} else {
					throw new BizException("未找到任何产品");
				}
			}

			String suiteName = null;
			if (testSuiteName == null || testSuiteName.trim().isEmpty()) {
				// 测试集名称为空,使用来源数据库名称+时间戳
				suiteName = sourceDataSchemaName + "_"
				        + DateUtil.getDateStr(DateUtil.getNow(), DateUtil.TIME_PATTREN_COMPACTED2);
			} else {
				suiteName = testSuiteName;
			}

			String message = testCaseService.createCaseByDataSource(productName, testSuiteName, sdataSchema,
			        tdataSchema);
			
			json.put("status", message);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json.toJSONString();
	}

	/**
	 * 查询所有的案例状态类型
	 * @return List<String>
	 */
	@RequestMapping(value = "/getCaseStatus", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public EnumExecuteStatus[] getCaseStatus() {
		return EnumExecuteStatus.values();
	}

}
