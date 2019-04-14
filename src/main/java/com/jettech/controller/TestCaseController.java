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
import com.jettech.entity.TestCase;
import com.jettech.entity.TestSuite;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IKeyMapperService;
import com.jettech.service.ProductService;
import com.jettech.service.ITestCaseService;
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
	private TestSuiteCaseService testSuiteCaseService;

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

	@SuppressWarnings("unused")
	@Autowired
	private IDataSourceService dataSourceService;

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

	// private final static String filePath = "D://tmp/";
	/*
	 * private final static String filePath = File.separator + "wls" +
	 * File.separator + "wls81" + File.separator + "tmp" + File.separator;
	 */
	@Value("${file.filePath}")
	private String filePath;
	// private final static String filePath = "D://tmp/";
	// private final static String filePath =
	// File.separator+"wls"+File.separator+"wls81"+File.separator+"tmp"+File.separator;

	@ResponseBody
	@RequestMapping(value = "/uploadMapper", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "上传文件")
	@ApiImplicitParams({ @ApiImplicitParam(name = "file", dataType = "String", required = true, value = "文件"),
			@ApiImplicitParam(name = "request", dataType = "String", required = true, paramType = "query") })
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

			// TestCase testCase =
			// mapperServiceImpl.loadMapper(dest.getAbsolutePath(), sheetName,
			// MapperEntity.class);
			// String fileName,String dataBaseName,String testSuiteName
			// mapperServiceImpl.loadMappers(dest.getAbsolutePath(),
			// productName, "demo1");
			result.put("success", true);
			result.put("data", map);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (JSONObject) result.put("success", false);
	}

	/**
	 * 实现对案例的下载
	 * 
	 * @param file
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/downloadSQLCase", method = RequestMethod.POST, produces = {
			"application/json;charset=utf-8" })
	@ApiOperation(value = "下载案例")
	@ApiImplicitParam(name = "testCaseId", value = "案例ID", paramType = "query", required = true, dataType = "Long")
	public JSONObject downloadSQLCase(@RequestParam Integer testCaseId) {
		JSONObject result = new JSONObject();
		StringBuffer buffer = testCaseService.exportTestCase(testCaseId);
		return result;
	}

	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/uploadSQLCase", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "上传SQLCase")
	@ApiImplicitParams({ @ApiImplicitParam(name = "file", dataType = "String", required = true, value = "上传案例"),
			@ApiImplicitParam(name = "request", dataType = "String", required = false, paramType = "query") })
	public JSONObject uploadSQLCase(@ApiParam(value = "上传案例", required = true) @RequestParam("file") MultipartFile file,
			HttpServletRequest request) {

		JSONObject result = new JSONObject();

		// 数据库
		String testSuiteId = request.getParameter("testSuiteId");
		System.out.println("测试集Id：" + testSuiteId);
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
		map.put("testSuiteId", testSuiteId);
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				log.info("临时文件:" + dest.getAbsolutePath());
				result.put("success", true);
				result.put("data", map);
				ResultVO resultVo = testCaseService.readSQLCase(map);
				result.put("message", resultVo.getMessage());
			} else {
				String msg = "临时文件不存在:" + dest.getAbsolutePath();
				log.info(msg);
				result.put("success", false);
				result.put("data", map);
				result.put("message", msg);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return (JSONObject) result.put("success", false);
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
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Map<String, Object> resultmap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			long beginTime = (new Date()).getTime();
			Page<TestCase> testCaseList = testCaseService.getAllTestCaseByPage(name, pageable);// findAllPage(pageNum,pageSize);
			List<TestCaseVO> testCaseVOList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : testCaseList) {
				TestCaseVO testCaseVO = new TestCaseVO(testCase);
				testCaseVOList.add(testCaseVO);
			}
			resultmap.put("totalElements", testCaseList.getTotalElements());
			resultmap.put("totalPages", testCaseList.getTotalPages());
			resultmap.put("list", testCaseVOList);
			log.info("getAllTestCaseByPage use:" + DateUtil.getEclapsedTimesStr(beginTime) + " name:" + name
					+ " pageNum:" + pageNum + " pageSize:" + pageSize);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
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
			return new ResultVO(true, StatusCode.OK, "查询失败");
		}
	}


	/**查询所有的案例，不再指定案例集中根据
	 * @param testSuiteID
	 * @param name
	 * @param pageNum
	 * @param pageSize
	 * @return 左侧
	 * findAllNotInSuite
	 */
	@ResponseBody
	@RequestMapping(value = "/findAllNotInSuite", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
//	@ApiOperation(value = "根据测试集ID查询所有并分页", notes = "根据测试集的ID查看测试案例")
//	@ApiImplicitParams({
//			@ApiImplicitParam(paramType = "query", name = "testSuiteID", value = "测试集ID", required = false, dataType = "String"),
//			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "第几页", required = false, dataType = "Long"),
//			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findAllNotInSuite(
			@RequestParam(value = "testSuiteID") Integer testSuiteID,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestCase> list = testCaseService.findALLBySuiteId(testSuiteID, name, pageable);
			List<TestCaseVO> voList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : list) {
				TestCaseVO vo = new TestCaseVO(testCase);
				/*vo.setTestSuiteID(testSuiteID);
				vo.setSourceQueryID(testCase.getSourceQuery().getId());
				vo.setTargetQueryID(testCase.getTargetQuery().getId());
				vo.setSourceDataSourceName(testCase.getSourceQuery().getDataSource().getName());
				vo.setTargetDataSourceName(testCase.getTargetQuery().getDataSource().getName());*/
				voList.add(vo);
			}
			resultMap.put("totalPages", list.getTotalPages());
			resultMap.put("totalElements", list.getTotalElements());
			resultMap.put("list", voList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}
	
	
	
	/** suiteid，案例名称查询指定的案例集中的案例，未在指定的集合中
	 * @param suiteId
	 * @param name
	 * @param pageNum
	 * @param pageSize
	 * @return 右侧
	 * getTestCaseListByTestSuiteID
	 */
	@RequestMapping(value = "/getTestCaseListByTestSuiteID",method=RequestMethod.GET)
	public ResultVO getTestCaseListByTestSuiteID(
			@RequestParam(value = "testSuiteID") Integer testSuiteID,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Page<TestCase> list=testCaseService.findBySuiteId(testSuiteID,name,pageable);
			ArrayList<TestCaseVO> voList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : list) {
				TestCaseVO vo = new TestCaseVO(testCase);
				voList.add(vo);
			}
			resultMap.put("totalPages", list.getTotalPages());
			resultMap.put("totalElements", list.getTotalElements());
			resultMap.put("list", voList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("查询报错：",e);
			return new ResultVO(false, StatusCode.ERROR, "查询成功");
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
			return new ResultVO(true, StatusCode.OK, "复制失败");
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
		if (testCaseIDS != null) {
			try {
				String[] list = testCaseIDS.split(",");
				System.out.println("list.size()");
				for (int i = 0; i < list.length; i++) {
					int id = Integer.parseInt(list[i]);
					System.out.println(id);
					testCaseService.delete(id);
				}
			} catch (Exception e) {
				log.error("删除失败", e);
				return new ResultVO(false, StatusCode.ERROR, "删除失败" + e.getLocalizedMessage());
			}
			return new ResultVO(true, StatusCode.OK, "删除成功");
		} else {
			return new ResultVO(false, StatusCode.ERROR, "删除失败");
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
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

		return new ResultVO(true, StatusCode.OK, "查询成功", testCase);

	}

	@RequestMapping(value = "findTestCaseBysuiteId", method = RequestMethod.GET)
	public ResultVO findTestCaseBysuiteId(
			@RequestParam(value = "suiteId", defaultValue = "", required = false) Integer suiteId/*,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize*/) {
		ArrayList<TestCaseVO> voList = new ArrayList<TestCaseVO>();
		/*Pageable pageable = PageRequest.of(pageNum-1, pageSize);*/
		try {
			List<TestCase> list = testCaseService.findTestCaseBysuiteId(suiteId);
			for (TestCase testCase : list) {
				TestCaseVO testCaseVO = new TestCaseVO(testCase);
				voList.add(testCaseVO);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", voList);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("查询报错为："+suiteId,e);
			return new ResultVO(false, StatusCode.ERROR, "查询出错");
		}
	}

}
