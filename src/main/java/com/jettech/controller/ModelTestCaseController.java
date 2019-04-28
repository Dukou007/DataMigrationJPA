package com.jettech.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.jettech.entity.ModelTestCase;
import com.jettech.entity.TestSuite;
import com.jettech.service.ModelTestCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.CaseModelSetDetailsVO;
import com.jettech.vo.CaseModelSetVO;
import com.jettech.util.DateUtil;
import com.jettech.vo.ModelTestCaseVO;
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
@RequestMapping(value = "/modelTestCase")
@Api(value = "Model_Test--Case--Controller|用于testCase模块")
public class ModelTestCaseController {

	private static Logger log = LoggerFactory.getLogger(ModelTestCaseController.class);

	@Autowired
	ModelTestCaseService modelTestCaseService;
	@Autowired
	private TestSuiteService testSuiteService;

	/*
	 * @Autowired private TestQueryService testQueryService;
	 */

	@ResponseBody
	@RequestMapping(value = "/doTestCase", produces = { "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "根据testCaseId来执行单条案例", notes = "根据testCaseId来执行单条案例")
	@ApiImplicitParam(paramType = "query", name = "testCaseId", dataType = "String", value = "测试案例ID")
	public ResultVO doTestCase(@RequestParam Integer testCaseId) {
		try {
			modelTestCaseService.doTest(testCaseId);
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
	@ResponseBody
	@RequestMapping(value = "/downloadSQLCase", method = RequestMethod.GET, produces = {
	        "application/json;charset=utf-8" })
	@ApiOperation(value = "下载案例")
	@ApiImplicitParam(name = "testCaseIds", value = "案例ID", paramType = "query", required = true, dataType = "String")
	public void downloadSQLCase(@RequestParam String testCaseIds, HttpServletResponse response) {
		try {
			OutputStream outputStream = response.getOutputStream();
			String buffer = modelTestCaseService.exportTestCase(testCaseIds);
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/msexcel;charset=utf-8");// 设置contentType为excel格式
			response.setHeader("Content-Disposition",
			        "Attachment;Filename=" + genAttachmentFileName("所选案列", "JSON_FOR_UCC_")// 设置名称格式，没有这个中文名称无法显示
			                + ".txt");
			outputStream.write(buffer.getBytes());
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// TODO: handle finally clause
		}

	}

	@ResponseBody
	@RequestMapping(value = "/uploadSQLCase", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	@ApiOperation(value = "上传SQLCase")
	@ApiImplicitParams({ @ApiImplicitParam(name = "file", dataType = "String", required = true, value = "上传案例"),
	        @ApiImplicitParam(name = "request", dataType = "String", required = false, paramType = "query") })
	public ResultVO uploadSQLCase(@ApiParam(value = "上传案例", required = true) @RequestParam("file") MultipartFile file,
	        HttpServletRequest request) {

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
		File dest = new File(filePath + newFileName);

		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		// map.put("productName", productName);
		// map.put("testSuiteName", testSuiteName);
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				log.info("临时文件:" + dest.getAbsolutePath());
				result.put("success", true);
				result.put("data", map);
				modelTestCaseService.readSQLCase(map);
			} else {
				log.info("临时文件不存在:" + dest.getAbsolutePath());
				result.put("success", false);
				result.put("data", map);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败"+e.getLocalizedMessage());
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
		long beginTime = (new Date()).getTime();
		try {
			Page<ModelTestCase> testCaseList = modelTestCaseService.getAllTestCaseByPage(name, pageable);// findAllPage(pageNum,pageSize);
			List<ModelTestCaseVO> testCaseVOList = new ArrayList<ModelTestCaseVO>();
			for (ModelTestCase testCase : testCaseList) {
				ModelTestCaseVO testCaseVO = new ModelTestCaseVO(testCase);
				testCaseVOList.add(testCaseVO);
			}
			resultmap.put("totalElements", testCaseList.getTotalElements());
			resultmap.put("totalPages", testCaseList.getTotalPages());
			resultmap.put("list", testCaseVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		} catch (Exception e) {
			log.error("查询模型案例:" + name + "异常", e);
			return new ResultVO(true, StatusCode.ERROR, "查询失败");
		} finally {
			log.info("getAllModelTestCaseByPage use:" + DateUtil.getEclapsedTimesStr(beginTime) + " name:" + name
			        + " pageNum:" + pageNum + " pageSize:" + pageSize);
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
	public ResultVO addTestCase(@RequestBody ModelTestCaseVO testCaseVO) {
		try {

			modelTestCaseService.saveTestCaseVo(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "新增成功");
		} catch (Exception e) {
			log.error("新增模型案例:" + JSONObject.toJSONString(testCaseVO).toString() + "异常", e);
			return new ResultVO(false, StatusCode.ERROR, "新增失败"+e.getLocalizedMessage());
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
	public ResultVO updateTestCase(@RequestBody ModelTestCaseVO testCaseVO) {
		try {
			modelTestCaseService.updateTestCase(testCaseVO);
			return new ResultVO(true, StatusCode.OK, "修改成功");
		} catch (Exception e) {
			log.error("修改模型案例:" + JSONObject.toJSONString(testCaseVO).toString() + "异常", e);
			return new ResultVO(false, StatusCode.ERROR, "修改失败"+e.getLocalizedMessage());
		}

	}
	/**
	 * @Description: 复制一条testcase案例
	 * @tips:处理好testCaseID，设为null
	 * 
	 * @author:zhou_xiaolong in 2019年1月29日下午4:40:34
	 */
	@ResponseBody
	@RequestMapping(value = "/copyModelTestCase", produces = {
	        "application/json;charset=utf-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "复制一条案例", notes = "testCaseID设为null")
	@ApiImplicitParam(paramType = "Integer", name = "testCaseID", value = "案例ID", required = true, dataType = "Integer")
	public ResultVO copyModelTestCase(@RequestParam(value = "testCaseID", required = true) Integer testCaseID) {
		try {
			//查询案例详情
			ModelTestCaseVO testCase = modelTestCaseService.getTestCaseDetail(testCaseID);
			testCase.setId(null);
			testCase.setCreateTime(new Date());
			testCase.setName(testCase.getName()+"_copy");
			CaseModelSetVO caseModelSetVO = testCase.getCaseModelSetVO();
			if(caseModelSetVO!=null) {
				caseModelSetVO.setId(null);
				caseModelSetVO.setCreateTime(new Date());
				List<CaseModelSetDetailsVO> detailsList = caseModelSetVO.getDetailsList();
				for (CaseModelSetDetailsVO caseModelSetDetailsVO : detailsList) {
					caseModelSetDetailsVO.setId(null);
					caseModelSetDetailsVO.setCreateTime(new Date());
				}
			}
			modelTestCaseService.saveTestCaseVo(testCase);
			return new ResultVO(true, StatusCode.OK, "复制成功");
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
			return new ResultVO(true, StatusCode.OK, "复制失败"+e.getLocalizedMessage());
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
			if (testCaseIDS != null) {
				String[] list = testCaseIDS.split(",");
				System.out.println("list.size()");
				for (int i = 0; i < list.length; i++) {
					int id = Integer.parseInt(list[i]);
					System.out.println(id);
					modelTestCaseService.delete(id);
				}
				return new ResultVO(true, StatusCode.OK, "删除成功");
			} else {
				return new ResultVO(false, StatusCode.ERROR, "删除失败");
			}
		} catch (Exception e) {
			log.error("删除模型案例:" + testCaseIDS + "异常", e);
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
		ModelTestCaseVO testCase = new ModelTestCaseVO();
		try {
			testCase = modelTestCaseService.getTestCaseDetail(testCaseID);
		} catch (Exception e) {
			log.error("查询模型案例:" + testCaseID + "异常", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

		return new ResultVO(true, StatusCode.OK, "查询成功", testCase);

	}

	public String genAttachmentFileName(String cnName, String defaultName) {
		try {
			cnName = new String(cnName.getBytes("gb2312"), "ISO8859-1");
		} catch (Exception e) {
			cnName = defaultName;
		}
		return cnName;
	}

}
