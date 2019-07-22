package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.EnumQualityRuleType;
import com.jettech.entity.DataField;
import com.jettech.entity.QualityTestPoint;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.service.IQualityTestPointService;
import com.jettech.service.ITestFieldService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@RestController
@RequestMapping(value = "/qualityTestPoint")
public class QualityTestPointController {
	
	private static Logger log = LoggerFactory.getLogger(QualityTestPointController.class);
	
	@Autowired
	private IQualityTestPointService testPointService;
	@Autowired
	private ITestFieldService testFieldService;
	@Autowired
    QualityTestPointRepository qualityTestPointRepository;
	/**
	 * 查询所有检查点
	 * 
	 * @param id
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/selectAllPoint")
	public ResultVO selectAllPoint() {
		List<QualityTestPoint> qualityTestPoints = qualityTestPointRepository.findAll();
		return new ResultVO(true, StatusCode.OK, "查询成功", qualityTestPoints);
	}


	/**
	 * 创建页面请求参数map
	 * 
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET, value = "/createMap")
	public ResultVO createMap() {
		Map<String, Object> map = new HashMap<>();
		List<DataField> testFields = testFieldService.findAllByTableId(2);
		List<String> selectFieldNames = new ArrayList<>();
		List<Integer> testfieldIds=new ArrayList<Integer>();
		for (int i = 0; i < testFields.size(); i++) {
			DataField testField = testFields.get(i);
			testfieldIds.add(testField.getId());
		}
		map.put("testfieldIds", testfieldIds);//测试字段id
		map.put("qualitySuiteIds", 1);//多个规则集id
		map.put("selectFieldNames", selectFieldNames);//所选字段名称
		map.put("testSuiteIds", 1);//测试集id
		return new ResultVO(true, StatusCode.OK, "参数设置成功", map);
	}
	

    /**
	 * 新增/配置检查点(根据多个或一个字段，一个规则集，产生多个或一个检查点) 并自动生成sql脚本，
	 * 自动生成测试案例，测试任务
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/checkPoint")
	public ResultVO checkPoint(@RequestBody Map<Object, Object> map) {
		return testPointService.checkPoint(map);
	}

	
	/**
	 * 质量检查批量生成
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/createQualityDataCheckCase",produces = {"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public ResultVO createQualityDataCheckCase(@RequestBody Map<Object, Object> map) {
		log.info("params=====================:"+map);
		ResultVO rv = new ResultVO(false, StatusCode.ERROR, "批量生成案例失败");
		try{
			rv = testPointService.batchCreateQualityCase(map);
		}catch(Exception e){
			e.printStackTrace();
		}
		log.info("BATCHADD============="+rv.getMessage());
		return rv;
	}


	@RequestMapping(method = RequestMethod.POST, value = "/checkPointTable")
	public ResultVO checkPointByTable(@RequestBody Map<Object, Object> map) {
		return testPointService.checkPointByTable(map);
	}

	/**
	 * 获取所有质量规则枚举类型
	 * @param dataSourceID
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getEnumQualityRuleType",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
    public  ResultVO getEnumQualityRuleType(){
		Map<String,Object> resultmap = new HashMap<String,Object>();
        try {
	        resultmap.put("list",EnumQualityRuleType.toList());
	        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	    }catch(Exception e) {
	    	e.printStackTrace();
	    	e.getLocalizedMessage();
	    	return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
	    }
	}
}
