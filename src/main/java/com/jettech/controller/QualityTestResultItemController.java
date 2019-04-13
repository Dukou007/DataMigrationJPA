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

import com.jettech.entity.QualityTestResultItem;
import com.jettech.service.QualityTestResultItemService;
import com.jettech.vo.QualityTestResultItemVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value="数据质量的测试结果集，根据result的id来查找")
@RequestMapping(value = "/qualityTestResultItem")
public class QualityTestResultItemController {

	private static Logger log = LoggerFactory.getLogger(QualityTestCaseController.class);
	@Autowired
	QualityTestResultItemService qualityTestResultItemService;

	@RequestMapping(value = "/findQualityTestResultItemByQualityTestResultID", method = RequestMethod.GET)
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "query", name = "testResultID", value = "测试结果ID", required = true, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
			@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") })
	public ResultVO findQualityTestResultItemByQualityTestResultID(@RequestParam Integer testResultID,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<QualityTestResultItem> testResultItemList = qualityTestResultItemService
					.findTestResultItemByTestResultID(testResultID,"", pageable);
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

}
