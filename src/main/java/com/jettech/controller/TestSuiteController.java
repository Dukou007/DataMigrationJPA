package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;

import org.apache.ibatis.binding.BindingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.BizException;
import com.jettech.entity.Product;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestSuite;
import com.jettech.service.ITestCaseService;
import com.jettech.service.TestSuiteCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestSuiteVO;
import com.mysql.cj.xdevapi.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/testSuite")
@Api(value = "TestSuiteController|用于测试集模块的服务")
public class TestSuiteController {

	private static Logger log = LoggerFactory.getLogger(TestSuiteController.class);

	@Autowired
	TestSuiteService testSuiteService;
	@Autowired
	private ITestCaseService testCaseService;

	@Autowired
	private TestSuiteCaseService testSuiteCaseService;
	
	
	@ResponseBody
	@RequestMapping(value = "/doTestSuite", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "根据testSuiteId执行测试集", notes = "根据testSuiteId执行测试集")
	@ApiImplicitParam(paramType = "Integer", name = "testSuiteId", value = "测试集ID", required = true, dataType = "Integer")
	public ResultVO doTestCase(Integer testSuiteId) {
		try {
			testSuiteService.doTestSuite(testSuiteId);
			return new ResultVO(true, StatusCode.OK, "开始执行");
		} catch (Exception e) {
			e.getLocalizedMessage();
			return new ResultVO(false, StatusCode.ERROR, "执行失败");
		}

	}

	/**
	 * @Description: 查询所有并分页
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月25日上午2:19:35
	 */
	@ResponseBody
	@RequestMapping(value = "/getTestSuiteList", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "查询所有并分页", notes = "查询所有并分页")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "Integer", name = "pageNum", value = "页码数", required = false, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageSize", value = "每页条数", required = false, dataType = "Integer") })
	public ResultVO getTestSuiteList(
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			ArrayList<TestSuiteVO> testSuiteVOs = new ArrayList<TestSuiteVO>();
			Page<TestSuite> testSuiteList = testSuiteService.getTestSuiteList(pageable);
			if (testSuiteList.getSize() > 0) {
				for (TestSuite testSuite : testSuiteList) {
					TestSuiteVO testSuiteVO = new TestSuiteVO();
					BeanUtils.copyProperties(testSuite, testSuiteVO);
					testSuiteVO.setProductID(testSuite.getProduct().getId());
					testSuiteVO.setProductName(testSuite.getProduct().getName());
					testSuiteVO.setTestCaseNumber(testSuiteCaseService.CountCase(testSuite.getId()));
					testSuiteVOs.add(testSuiteVO);
				}
				resultMap.put("totalPages", testSuiteList.getTotalPages());
				resultMap.put("totalElements", testSuiteList.getTotalElements());
				resultMap.put("list", testSuiteVOs);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("", e);
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	
	
	
	
	/**
	 * @Description: 给未分类的TestCase加入到已存在的TestSuite中
	 * @tips:
	 * 
	 * @author:zhou_xiaolong in 2019年2月23日下午6:52:06
	 * @throws BizException 
	 */
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/changeTestCasePosition", method = RequestMethod.POST)
	@ApiOperation(value = "给未分类的TestCase加入到已存在的TestSuite中", notes = "更改测试集ID")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "Integer", name = "testCaseID", value = "测试案例的ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "testSuiteID", value = "测试集的ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageNum", value = "页码数", required = false, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageSize", value = "每页条数", required = false, dataType = "Integer") })
	public ResultVO changeTestCasePosition(@RequestParam(value = "testCaseIDs") String testCaseIDs,
			@RequestParam(value = "testSuiteID") Integer testSuiteID/*,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize*/) throws BizException {
	/*	HashMap<Object, Object> resultMap = new HashMap<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);*/
		try {
			testCaseService.changeTestCasePosition(testSuiteID, testCaseIDs);
			/*Page<TestCase> testCaseList = testCaseService.findBySuiteId(testSuiteID, pageable);
			ArrayList<TestCaseVO> testCaseVOList = new ArrayList<TestCaseVO>();
			for (TestCase testCase : testCaseList) {
				TestCaseVO testCaseVO = new TestCaseVO();
				BeanUtils.copyProperties(testCase, testCaseVO);
				testCaseVO.setTestSuiteID(testSuiteID);
				// deal targetdatasource ,sourcedatasource
				testCaseVO.setTargetDataSourceID(testCase.getTargetQuery().getDataSource().getId());
				testCaseVO.setTargetDataSourceName(testCase.getTargetQuery().getDataSource().getName());
				testCaseVO.setSourceDataSourceID(testCase.getSourceQuery().getDataSource().getId());
				testCaseVO.setSourceDataSourceName(testCase.getSourceQuery().getDataSource().getName());
				// deal testquery
				testCaseVO.setSourceQueryID(testCase.getSourceQuery().getId());
				testCaseVO.setTargetQueryID(testCase.getTargetQuery().getId());
				testCaseVOList.add(testCaseVO);
			}
			resultMap.put("totalPages", testCaseList.getTotalPages());
			resultMap.put("totalElements", testCaseList.getTotalElements());
			resultMap.put("list", testCaseVOList);*/
			return new ResultVO(true, StatusCode.OK, "转移进入测试集成功");
		} catch (BeansException e) {
			e.printStackTrace();
			log.error("转移失败"+testCaseIDs,e);
			return new ResultVO(false, StatusCode.ERROR, "转移进入测试集失败"+e.getLocalizedMessage());
		}

	}

	/**
	 * @Description: 回归到待处理状态
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月24日下午8:46:58
	 * @throws BizException 
	 */
	@ResponseBody
	@RequestMapping(value = "backDisorder", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "从测试集中剔除", notes = "testSuiteID设置为null，其中假设testSuiteID=null为可选列")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testCaseIDS", value = "测试案例的ID", required = true, dataType = "String"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码数", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO backDisorder(
			@RequestParam(value = "testCaseIDS", required = true) String testCaseIDS,
			@RequestParam(value = "suiteId", required = true) Integer suiteId/*,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize*/) throws BizException {
		try {
			testCaseService.backDisorder(testCaseIDS,suiteId);
			return new ResultVO(true, StatusCode.OK, "转移进入测试集成功");
		} catch (BeansException e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "转移进入测试集失败"+e.getLocalizedMessage());
		}

	}

	/**
	 * @Description: 根据测试集名称查找测试集并分页。
	 * @tips:null;
	 * 
	 * @author:zhou_xiaolong in 2019年2月22日下午4:11:54
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTestSuiteByPage", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "根据测试集名称查找测试集并分页", notes = "需要输入测试集名称,不输入默认查询所有")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "String", name = "name", value = "产品名称", required = false, dataType = "String"),
			@ApiImplicitParam(paramType = "Integer", name = "pageNum", value = "页码值", required = false, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageSize", value = "每页条数", required = false, dataType = "Integer") })
	public ResultVO getAllTestSuiteByPage(
			@RequestParam(value = "name", defaultValue = "", required = false) String name,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Map<String, Object> resultmap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<TestSuite> testSuiteList = testSuiteService.getAllTestSuiteByPage(name, pageable);// findAllPage(pageNum,pageSize);
			List<TestSuiteVO> testSuiteVOs = new ArrayList<TestSuiteVO>();
			for (TestSuite testSuite : testSuiteList) {
				if (testSuite.getProduct().getId() > 0) {
					TestSuiteVO testSuiteVO = new TestSuiteVO();
					BeanUtils.copyProperties(testSuite, testSuiteVO);
					testSuiteVO.setProductID(testSuite.getProduct().getId());
					testSuiteVO.setProductName(testSuite.getProduct().getName());
					testSuiteVO.setTestCaseNumber(testSuiteCaseService.CountCase(testSuite.getId()));
					testSuiteVOs.add(testSuiteVO);
				}
			}
			resultmap.put("totalElements", testSuiteList.getTotalElements());
			resultmap.put("totalPages", testSuiteList.getTotalPages());
			resultmap.put("list", testSuiteVOs);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		} catch (Exception e) {
			e.getLocalizedMessage();
			return new ResultVO(true, StatusCode.OK, "查询失败");
		}

	}

	/**
	 * @Description: 通过产品ID查询测试集并分页
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月24日下午5:51:16
	 */
	@ResponseBody
	@RequestMapping(value = "/getTestSuiteByProductID", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "根据产品ID查询所有并分页")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "Integer", name = "productID", value = "产品ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageNum", value = "第几页", required = false, dataType = "Integer"),
			@ApiImplicitParam(paramType = "Integer", name = "pageSize", value = "每页条数", required = false, dataType = "Integer") })
	public ResultVO getTestSuiteByProductID(@RequestParam(value = "productID") Integer productID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		try {
			ArrayList<TestSuiteVO> testSuiteVOs = new ArrayList<TestSuiteVO>();
			PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
			Page<TestSuite> testSuiteList = testSuiteService.getAllTestSuiteByProductID(productID, pageable);
			for (TestSuite testSuite : testSuiteList) {
				if (testSuite.getProduct().getId() != null) {
					TestSuiteVO testSuiteVO = new TestSuiteVO();
					BeanUtils.copyProperties(testSuite, testSuiteVO);
					testSuiteVO.setTestCaseNumber(testSuiteCaseService.CountCase(testSuite.getId()));
					testSuiteVO.setProductID(testSuite.getProduct().getId());
					testSuiteVO.setProductName(testSuite.getProduct().getName());
					testSuiteVOs.add(testSuiteVO);
					resultMap.put("totalElements", testSuiteList.getTotalElements());
					resultMap.put("totalPages", testSuiteList.getTotalPages());
					resultMap.put("list", testSuiteVOs);

				}
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (BeansException e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 * @Description: 新增测试集
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月25日上午12:44:43
	 */
	@ResponseBody
	@RequestMapping(value = "/addTestSuite", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "新增测试集", notes = "新增测试机，处理productID")
	@ApiImplicitParam(paramType = "TestSuiteVO", name = "testSuiteVO", value = "testSuiteVO实体", required = true, dataType = "TestSuiteVO")
	public ResultVO addTestSuite(@RequestBody TestSuiteVO testSuiteVO) {
		try {
			List<TestSuite> testSuiteList = testSuiteService.getBySuiteNameAndProductId(testSuiteVO.getName(), testSuiteVO.getProductID());
			if(testSuiteList!=null&&testSuiteList.size()>0) {
				return new ResultVO(false, StatusCode.ERROR, "保存失败案例集不能重复");
			}else {
				TestSuite testSuite = new TestSuite();
				BeanUtils.copyProperties(testSuiteVO, testSuite);
				Product p = new Product();
				p.setId(testSuiteVO.getProductID());
				p.setName(testSuiteVO.getProductName());
				testSuite.setProduct(p);
				testSuiteService.save(testSuite);
				return new ResultVO(true, StatusCode.OK, "新增成功");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败");
		}
	}

	/**
	 * 修改测试集
	 * 
	 * @param TestSuite
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateTestSuite/{testSuiteID}", method = RequestMethod.PUT)
	@ApiOperation(value = "更新测试集", notes = "更新测试集，testSuiteID")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "testSuiteID", value = "测试集ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(name = "testSuiteVO", value = "testSuiteVO实体", required = true, dataType = "TestSuiteVO") })
	public ResultVO updateTestSuite(@PathVariable Integer testSuiteID, @RequestBody TestSuiteVO testSuiteVO) {
		try {
			TestSuite testSuite = testSuiteService.findById(testSuiteVO.getId());
			if(testSuite==null) {
				return new ResultVO(false, StatusCode.ERROR, "要修改的案例集不存在");
			}
			BeanUtils.copyProperties(testSuiteVO, testSuite);
			Product product = new Product();
			product.setId(testSuiteVO.getProductID());
			testSuite.setProduct(product);
			testSuiteService.save(testSuite);
			return new ResultVO(true, StatusCode.OK, "更新成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "更新失败");
		}
	}

	/**
	 * @Description: 删除测试集
	 * @Tips: delete!!!!!!!!;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月25日上午1:38:17
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteTestSuite", method = RequestMethod.GET)
	@ApiOperation(value = "删除测试集", notes = "可根据ids(productID)批量删除")
	@ApiImplicitParam(name = "ids", value = "产品ID", required = true, dataType = "String")
	public ResultVO deleteTestSuite(@RequestParam String ids) {
		try {
			String[] list = ids.split(",");
			for (int i = 0; i < list.length; i++) {
				int id = Integer.parseInt(list[i]);
				testSuiteCaseService.deleteRelationBySuiteId(id);
				testSuiteService.delete(id);
			}
			return new ResultVO(true, StatusCode.OK, "删除成功");
		} catch (Exception e) {
			log.error("", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "删除失败");
		}
	}
	@RequestMapping(value = "/copyTestSuite", method = RequestMethod.POST)
	public ResultVO copyTestSuite(@RequestParam Integer testSuiteId) {
		try {
			testSuiteService.copyTestSuite(testSuiteId);
			return new ResultVO(true,StatusCode.OK,"复制成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("复制失败信息为：",e);
			return new ResultVO(false,StatusCode.ERROR,"复制失败");
		}
	}
	@RequestMapping(value = "/findByProductId", method = RequestMethod.GET)
	public ResultVO findByProductId(@RequestParam Integer productId) {
		try {
			List<TestSuiteVO> testSuiteList = testSuiteService.findByProductId(productId);
			return new ResultVO(true,StatusCode.OK,"查询成功",testSuiteList);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("查询失败信息为：",e);
			return new ResultVO(false,StatusCode.ERROR,"查询失败");
		}
		
	}

	/**
	 * @Description: 根据产品ID查询对应的测试集不分页
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月21日上午10:32:14
	 * @param productId
	 * @return
	 *//*
	@RequestMapping(value="/findTestSuiteByProductId",method=RequestMethod.GET)
	public ResultVO findTestSuiteByProductId(Integer productId) {
		HashMap<String, Object> map = new HashMap<String,Object>();
		try {
			List<TestSuite> testSuiteList = testSuiteService.findByProductId(productId);
			ArrayList<TestSuiteVO> list = new ArrayList<TestSuiteVO>();
			for (TestSuite testSuite : testSuiteList) {
				TestSuiteVO testSuiteVO = new TestSuiteVO(testSuite);
				List<TestCase>testCaseList=testCaseService.findAllById(testSuite.getId());
				if(testCaseList==null) {
					testSuiteVO.setTestCaseNumber(0);
				}else {
					testSuiteVO.setTestCaseNumber(testCaseList.size());
				}
				list.add(testSuiteVO);
			}
			map.put("list", list);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("根据产品的ID查询测试集报错为：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
		
		
	}
	*/

	//质量测试集执行方法修改    20190321
	@ResponseBody
	@RequestMapping(value = "/doQualityTestSuite", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "根据testSuiteId执行测试集", notes = "根据testSuiteId执行测试集")
	@ApiImplicitParam(paramType = "Integer", name = "testSuiteId", value = "测试集ID", required = true, dataType = "Integer")
	public ResultVO doQualityTestCase(Integer testSuiteId) {
		try {
		//	testSuiteService.doTestSuite(testSuiteId);
			testSuiteService.doQualityTestSuite(testSuiteId);
			return new ResultVO(true, StatusCode.OK, "开始执行");
		} catch (Exception e) {
			e.getLocalizedMessage();
			return new ResultVO(false, StatusCode.ERROR, "执行失败");
		}

	}

	
}
