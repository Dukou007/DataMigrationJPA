package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.server.PathParam;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.jettech.entity.DataSchema;
import com.jettech.entity.QualityTestPoint;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.service.IQualityTestPointService;
import com.jettech.service.ITestFieldService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestDatabaseVO;

@RestController
@RequestMapping(value = "/qualityTestPoint")
public class QualityTestPointController {
	
	private static Logger log = LoggerFactory.getLogger(QualityTestPointController.class);
	
	@Autowired
	private IQualityTestPointService testPointService;
	@Autowired
	private ITestFieldService testFieldService;
	/*@Autowired
	private IQualityTestCaseService IQualityTestCaseService;
	@Autowired
	private TestSuiteService testSuiteService;*/
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
		//System.out.println("params=====================:"+map);
		log.info("params=====================:"+map);
//		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")+ "");
		ResultVO rv = new ResultVO(false, StatusCode.ERROR, "批量生成案例失败");
		try{
			rv = testPointService.batchCreateQualityCase(map);
//			switch(qualitySuiteId){
//				case 1:
//					rv = testPointService.tableCheckNull(map);
//					break;
//				case 2:
//					rv = testPointService.tableCheckRange(map);
//					rv = testPointService.tableCheckLength(map);
//					break;
//				case 3:
//					rv = testPointService.tableCheckRange(map);
//					rv = testPointService.tableCheckLength(map);
//					break;
//				case 4:
//					rv = testPointService.tableCheckRange(map);
//					rv = testPointService.tableCheckLength(map);
//					break;
//				case 5:
//					rv = testPointService.tableCheckRange(map);
//					rv = testPointService.tableCheckLength(map);
//					break;
//				case 6:
////					rv = testPointService.tableCheckNull(map);
//				case 7:
//					rv = testPointService.tableCheckUnique(map);
//					break;
//				case 8:
////					rv = testPointService.tableCheckNull(map);
//				case 9:
//					rv = testPointService.tableCheckNotIn(map);
//					break;
//				case 10:
//					rv = testPointService.tableCheckNotF(map);
//					break;
//				case 11:
//					rv = testPointService.tableCheckNonAmountRange(map);
//					break;
//				case 12:
////					rv = testPointService.tableCheckNull(map);
//				case 13:
////					rv = testPointService.tableCheckNull(map);
//			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//System.out.println("BATCHADD============="+rv.getMessage());
		log.info("BATCHADD============="+rv.getMessage());
		return rv;
	}

//	
//
//	//liu 20190219
//	/**
//	 * 根据一个表一个规则生成检查点1 空值检查 liu1 20190218
//	 * @param map
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "/createtableCheckNull")
//	public ResultVO createTableCheckNull(@RequestBody Map<Object, Object> map) {
//		return testPointService.tableCheckNull(map);
//	}
//
//	/**
//	 * 值域检查2   20190219
//	 * @param map
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "/createTableCheckRange")
//	public ResultVO createTableCheckRange(@RequestBody Map<Object, Object> map) {
//		return testPointService.tableCheckRange(map);
//	}
//
//	/**
//	 * 字段长度检查3 20190219
//	 * @param map
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "/createTableCheckLength")
//	public ResultVO createTableCheckLength(@RequestBody Map<Object, Object> map) {
//		return testPointService.tableCheckLength(map);
//	}
//
//	/**
//	 * 字段唯一性检查 4 20190219
//	 * @param map
//	 * @return
//	 */
//	@RequestMapping(method = RequestMethod.POST, value = "/createTableCheckUnique")
//	public ResultVO createTableCheckUnique(@RequestBody Map<Object, Object> map) {
//		return testPointService.tableCheckUnique(map);
//	}
//
//    /**
//     * 码值范围检查 5   20190219
//     * @param map
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/createTableCheckNotIn")
//    public ResultVO createTableCheckNotIn(@RequestBody Map<Object, Object> map) {
//        return testPointService.tableCheckNotIn(map);
//    }
//
//    /**
//     * 筛选非浮点数的记录6 20190219
//     * @param map
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/createTableCheckNotF")
//    public ResultVO createTableCheckNotF(@RequestBody Map<Object, Object> map) {
//        return  testPointService.tableCheckNotF(map);
//    }
//
//    /**
//     * 筛选非金额范围的记录7 20190219
//     * @param map
//     * @return
//     */
//    @RequestMapping(method = RequestMethod.POST, value = "/createTableCheckNonAmountRange")
//    public ResultVO createTableCheckNonAmountRange(@RequestBody Map<Object, Object> map) {
//        return testPointService.tableCheckNonAmountRange(map);
//    }

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
//		if(typeName == null){
//			typeName = "";
//		}
//		List<Map<String,Object>> tdlist = new ArrayList<Map<String,Object>>();
        try {
//        	if(StringUtils.isNotBlank(typeName) && EnumUtils.isValidEnum(EnumQualityRuleType.class, typeName)){
        		//Map<String,Object> map = EnumQualityRuleType.
        		//tdlist.add(typeName);
//        	}else if(StringUtils.isBlank(typeName)){
        		//for (EnumQualityRuleType ert : EnumQualityRuleType.values()) { 
//        		    tdlist.addAll(EnumQualityRuleType.toList());
//        		    EnumQualityRuleType.
        		//}
//        	}else if(!EnumUtils.isValidEnum(EnumQualityRuleType.class, typeName)){
//        		return new ResultVO(false, StatusCode.ERROR, "无效的参数:"+typeName, resultmap);
//        	}
	        resultmap.put("list",EnumQualityRuleType.toList());
	        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	    }catch(Exception e) {
	    	e.printStackTrace();
	    	e.getLocalizedMessage();
	    	return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
	    }
	}
}
