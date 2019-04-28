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

	/**根据执行结果的id，result来查询结果明细并分页
	 * @param testResultID
	 * @param result
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/findQualityTestResultItemByQualityTestResultID", method = RequestMethod.GET)
	public ResultVO findQualityTestResultItemByQualityTestResultID(@RequestParam Integer testResultID,
		/*	@RequestParam(value = "selectValue",  required = false) String selectValue,*/
			@RequestParam(value = "result",  required = false) String result,
		/*	@RequestParam(value = "columnName",  required = false) String columnName,*/
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<QualityTestResultItem> testResultItemList = qualityTestResultItemService
					.findTestResultItemByTestResultIDAndResultAndSelectValueAndColumnName(testResultID,/*selectValue,*/result,/*columnName,*/ pageable);
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
