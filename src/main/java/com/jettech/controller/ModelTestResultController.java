package com.jettech.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.jettech.entity.ModelTestResult;
import com.jettech.repostory.ModelTestResultRepository;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ModelTestCaseService;
import com.jettech.service.ProductService;
import com.jettech.service.TestRoundService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.ModelTestResultVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/modelTestResult")
@Api(value = "Test--Result--Controller|测试结果的controller")
public class ModelTestResultController {

	@Autowired
	TestSuiteService testSuiteService;

	@Autowired
	ITestReusltService testRuleService;

	@Autowired
	ProductService productService;

	@Autowired
	ITestReusltService testReSultService;

	@Autowired
	ModelTestCaseService testCaseService;

	@Autowired
	ITestResultItemService testResultItemService;
	
	
	@SuppressWarnings("unused")
	@Autowired
	private TestRoundService testRoundService;
	
	@Autowired
	private ModelTestResultRepository repository;

	private static Logger log = LoggerFactory.getLogger(ModelTestResultController.class);

	@SuppressWarnings("serial")
	@RequestMapping(value="findTestResultByCaseIDAndStartTimeAndEndTime",method=RequestMethod.GET)
	@ApiOperation(value="testCaseList页---点击执行记录----根据案例的ID查找轮次记录,跳转到testCaseResultList的界面，根据案例ID、开始时间，结束时间来查询测试结果,。",notes="不输入开始时间和结束时间默认查找所有。")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "caseId", value = "案例集ID", required = true, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") 
	})
	public ResultVO findTestResultByCaseIDAndStartTimeAndEndTime(
			@RequestParam String caseId,
			@RequestParam (value="startTime",required=false)String startTime,
			@RequestParam (value="endTime",required=false)String endTime,
			@RequestParam(value="pageNum",defaultValue="1",required=false)Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false)Integer pageSize
			) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		Page<ModelTestResult>testResultList=null;
		Specification<ModelTestResult> specification = new Specification<ModelTestResult>() {
			@Override
			public Predicate toPredicate(Root<ModelTestResult> root, CriteriaQuery<?> query,CriteriaBuilder criteriaBuilder) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(org.apache.commons.lang.StringUtils.isNotBlank(startTime)) {
					predicateList.add(
							criteriaBuilder.greaterThanOrEqualTo(root.get("startTime").as(String.class),startTime));
				}
				if(org.apache.commons.lang.StringUtils.isNotBlank(endTime)) {
					predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime").as(String.class), endTime));
				}
				if(org.apache.commons.lang.StringUtils.isNotBlank(caseId)) {
					predicateList.add(criteriaBuilder.equal(root.get("caseId").as(String.class), caseId));
				}
					
				return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
			}
		};
		
		testResultList=this.repository.findAll(specification,pageable);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ArrayList<ModelTestResultVO> testResultVOList = new ArrayList<ModelTestResultVO>();
		for (ModelTestResult testResult : testResultList) {
			ModelTestResultVO testResultVO = new ModelTestResultVO(testResult);
			testResultVO.setCaseName(testCaseService.findById(Integer.valueOf(caseId)).getName());
			sdf.format(testResult.getStartTime());
			testResultVOList.add(testResultVO);
		}
		HashMap<String, Object> map = new HashMap<String,Object>();
		map.put("totalElements", testResultList.getTotalElements());
		map.put("totalPages", testResultList.getTotalPages());
		map.put("list", testResultVOList);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

}
