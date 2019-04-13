package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;
import com.jettech.repostory.TestResultItemRepository;
import com.jettech.service.ModelTestResultItemService;
import com.jettech.vo.ModelTestResultItemVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestResultItemVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value="modelTestResultItem")
@Api(value="Test--Result--Item--Controller|测试结果明细")
public class ModelTestResultItemController {
	
	@Autowired
	private ModelTestResultItemService testResultItemService;

	
	/**
	 * @description:根据keyandvalue查找并分页
	 * @tips:null;
	 * @param keyAndValue
	 * @param pageNum
	 * @param pageSize
	 * @return
	 * @author:zhou_xiaolong in 2019年3月1日下午1:06:41
	 */
	
	@SuppressWarnings("unused")
	private static Logger log=LoggerFactory.getLogger(ModelTestResultItemController.class);
	
	@ApiOperation(value="根据keyandvalue查找并分页")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "keyValue", value = "keyValue", required = false, dataType = "Sting"),
		@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	@RequestMapping(value="/findByKeyValue",method=RequestMethod.GET)
	public ResultVO findByKeyValue(
			@RequestParam (value="keyValue",defaultValue="",required=false)String keyValue,
			@RequestParam (value="testResultId",defaultValue="",required=false)String testResultId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize
			) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		ArrayList<ModelTestResultItemVO> testResultItemVOList = new ArrayList<ModelTestResultItemVO>();
		try {
			Page<ModelTestResultItem>testResultItemList=testResultItemService.findTestResultItemLikeKeyValue(keyValue,testResultId,pageable);
			for (ModelTestResultItem testResultItem : testResultItemList) {
				ModelTestResultItemVO testResultItemVO = new ModelTestResultItemVO(testResultItem);
				testResultItemVOList.add(testResultItemVO);
			}
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put("totalElements", testResultItemList.getTotalElements());
			map.put("totalPages", testResultItemList.getTotalPages());
			map.put("list", testResultItemVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("根据keyvalue查询报错：",e);
			e.printStackTrace();
			return new ResultVO(false,StatusCode.ERROR,"查询失败");
		}

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
	@RequestMapping(value="/findTestResultItemByTestResultID",method=RequestMethod.GET)
	@ApiOperation(value="在testCaseResultList界面，点击结果明细，到最终的界面。（根据TestResultID来查询TestResultItem）",notes="参数:" + 
			"keyValue:keyValue;" + 
			"result:result;" + 
			"columnName:columnName;" + 
			"sourceValue:sourceValue;" + 
			"targetValue:targetValue")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "testResultID", value = "测试结果ID", required = true, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findTestResultItemByTestResultID(
			@RequestParam Integer testResultID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
			@RequestParam(value = "result", defaultValue = "", required = false) String result
			) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		try {
			Page<ModelTestResultItem>testResultItemList=	testResultItemService.findTestResultItemByTestResultID(testResultID,result,pageable);
			ArrayList<ModelTestResultItemVO> testResultItemVOList = new ArrayList<ModelTestResultItemVO>();
			for (ModelTestResultItem testResultItem : testResultItemList) {
				ModelTestResultItemVO testResultItemVO = new ModelTestResultItemVO(testResultItem);
				testResultItemVOList.add(testResultItemVO);
			}
			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put("totalElements", testResultItemList.getTotalElements());
			map.put("totalPages", testResultItemList.getTotalPages());
			map.put("list", testResultItemVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("报错为:", e);
			e.printStackTrace();
			return new ResultVO(false ,StatusCode.ERROR,"查询失败");
		}
	}
}
