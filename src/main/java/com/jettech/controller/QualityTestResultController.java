package com.jettech.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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

import com.jettech.entity.QualityTestResult;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.service.IQualityTestResultService;
import com.jettech.vo.QualityTestResultVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(value="数据质量的测试结果")
@RequestMapping(value="/qualityTestResult")
public class QualityTestResultController {
	
	private static Logger log = LoggerFactory.getLogger(QualityTestResultController.class);

	@Autowired
	private QualityTestResultRepository repository;
	
	@Autowired
	private IQualityTestResultService qualityTestResultService;
	
	
	@Autowired
	private QualityTestCaseRepository qualityTestCaseRepository;

	@SuppressWarnings("serial")
	@RequestMapping(value="findTestResultByQualityCaseIDAndStartTimeAndEndTime",method=RequestMethod.GET)
	@ApiOperation(value="testCaseList页---点击执行记录----根据案例的ID查找轮次记录,跳转到testCaseResultList的界面，根据案例ID、开始时间，结束时间来查询测试结果,。",notes="不输入开始时间和结束时间默认查找所有。")
	@ApiImplicitParams({
		@ApiImplicitParam(paramType = "query", name = "testCaseId", value = "案例集ID", required = true, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
		@ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码值", required = false, dataType = "Long"),
		@ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页条数", required = false, dataType = "Long") 
	})
	public ResultVO findTestResultByQualityCaseIDAndStartTimeAndEndTime(
			@RequestParam Integer testCaseId,
			@RequestParam (value="startTime",required=false)String startTime,
			@RequestParam (value="endTime",required=false)String endTime,
			@RequestParam(value="pageNum",defaultValue="1",required=false)Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false)Integer pageSize
			) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		try {
			/*
			 * //转化日期格式 if(startTime.length()!=0&&endTime.length()!=0) { startTime =
			 * startTime.replace("Z", " UTC"); endTime = endTime.replace("Z", " UTC");
			 * SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
			 * SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); Date ds
			 * = sdf.parse(startTime); Date de = sdf.parse(endTime); startTime =
			 * sdf1.format(ds); endTime = sdf1.format(de).substring(0, 11)+"23:59:59"; }
			 */
			Page<QualityTestResult>testResultList=qualityTestResultService.findTestResultByQualityCaseIDAndStartTimeAndEndTime(testCaseId,startTime,endTime,pageable);
			ArrayList<QualityTestResultVO> testResultVOList = new ArrayList<QualityTestResultVO>();
			for (QualityTestResult testResult : testResultList) {
				QualityTestResultVO testResultVO = new QualityTestResultVO(testResult);
				testResultVO.setTestCaseName(qualityTestCaseRepository.getOne(testCaseId).getName());
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String,Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("查询失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}
	
	/**根据caseId和result来分页查询
	 * @param testCaseId
	 * @param result
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value="findByCaseIdAndResult",method=RequestMethod.GET)
	public ResultVO findByCaseIdAndResult(
			@RequestParam Integer testCaseId,
			@RequestParam (value="result",required=false)Boolean result,
			@RequestParam(value="pageNum",defaultValue="1",required=false)Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false)Integer pageSize
			
			) {
		Pageable pageable = PageRequest.of(pageNum-1, pageSize);
		try {
			Page<QualityTestResult>testResultList=qualityTestResultService.findByCaseIdAndResult(testCaseId,result,pageable);
			ArrayList<QualityTestResultVO> testResultVOList = new ArrayList<QualityTestResultVO>();
			for (QualityTestResult testResult : testResultList) {
				QualityTestResultVO testResultVO = new QualityTestResultVO(testResult);
				testResultVO.setTestCaseName(qualityTestCaseRepository.getOne(testCaseId).getName());
				testResultVO.setCreateTime(testResult.getCreateTime());
				testResultVOList.add(testResultVO);
			}
			HashMap<String, Object> map = new HashMap<String,Object>();
			map.put("totalElements", testResultList.getTotalElements());
			map.put("totalPages", testResultList.getTotalPages());
			map.put("list", testResultVOList);
			return new ResultVO(true, StatusCode.OK, "查询成功", map);
		} catch (Exception e) {
			log.error("查询报错：", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
		
	}
	
	
	/**导出证迹的表格
	 * @param testRoundId
	 * @return
	 */
	@RequestMapping(value="exportEvidence",method=RequestMethod.GET)
	public ResultVO exportEvidence(@RequestParam String testResultIds,HttpServletResponse res) {
		try {
			//增加判断，是否存在。
			if(StringUtils.isNotBlank(testResultIds)) {
				qualityTestResultService.exportEvidence(testResultIds,res);
				return null;
			}else {
				return new ResultVO(false, StatusCode.ERROR, "请勾选要导出的目标");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("导出失败",e);
			return new ResultVO(false, StatusCode.ERROR, "导出失败");
		}
		
	}
	@RequestMapping(value="getTResultByRid",method=RequestMethod.GET)
	public ResultVO getTResultByRid(
			@RequestParam Integer testRoundId,
			@RequestParam (value="caseName",required=false)String caseName,
			@RequestParam(value="pageNum",defaultValue="1",required=false)Integer pageNum,
			@RequestParam(value="pageSize",defaultValue="10",required=false)Integer pageSize
			) {
		return qualityTestResultService.findByTestRIdAndName(testRoundId, caseName, pageNum, pageSize);
	}




}
