package com.jettech.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.DataSource;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestRule;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestFieldService;
import com.jettech.service.TestQueryFieldService;
import com.jettech.service.TestQueryService;
import com.jettech.service.TestRuleService;
import com.jettech.vo.TestQueryFieldVO;
import com.jettech.vo.TestQueryVO;
import com.jettech.vo.TestRuleVO;


/**
 *  @author zhou_xiaolong
 *	@Description: testQuery的增加，修改以及查询功能
 *  @date: 2019年2月18日 下午5:07:36 
 */
@RestController
@RequestMapping("/testQuery")
public class TestQueryController {

	private static Logger log = LoggerFactory.getLogger(TestQueryController.class);

	@Autowired
	private TestQueryService testQueryService;
	@Autowired
	private ITestCaseService testCaseService;
	@Autowired
	private TestQueryFieldService testQueryFieldService;
	@Autowired
	private TestRuleService testRuleService;
	@Autowired
	private ITestFieldService testFieldService;

	private List<TestQueryFieldVO> testFields;
	private List<TestQueryFieldVO> keyFields;
	private List<TestQueryFieldVO> pageFields;
	private List<TestRuleVO> testRules;
	/**
	 * 根据testCaseID查询testquery
	 */

	@ResponseBody
	@RequestMapping(value = "/getTestQuery/{testCaseID}", produces = { "application/json;charset=UTF-8" })
	public String getTestCaseList(@PathVariable Integer testCaseID) {
		JSONObject result = new JSONObject();
		try {
			ArrayList<TestQueryVO> testQueryVOs = new ArrayList<TestQueryVO>();
			List<TestQuery> testQuerys = testQueryService.findByCaseId(testCaseID);
			for (TestQuery testQuery : testQuerys) {
				TestQueryVO testQueryVO = new TestQueryVO(testQuery);	
				testQueryVOs.add(testQueryVO);
			}
			result.put("rows", testQueryVOs);
			result.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
			log.error("", e);
		}
		return result.toJSONString();
	}

	

	/**
	 * @Description: 新增一个testQuery 
	 * tips：处理testFields、testRules、keyFields字段需谨慎
	 * 		testQuery有sourcequery和targetquery两个对象。
	 * zhou_xiaolong in 2019年2月14日
	 */
	@ResponseBody
	@RequestMapping("/addTestQuery")
	public String addTestQuery(@RequestBody TestQueryVO sourceQueryVO ,@RequestBody TestQueryVO targetQueryVO ) {
		JSONObject result = new JSONObject();
		try {
			
			ArrayList<TestQuery> testQueries = new ArrayList<TestQuery>();
			// 两个对象
			TestQuery sourceQuery = new TestQuery();
			TestQuery targetQuery = new TestQuery();
			// testcase 
			TestCase testCase = new TestCase();
			testCase.setId(sourceQueryVO.getTestCaseId());
//			sourceQuery.setTestCase(testCase);
//			targetQuery.setTestCase(testCase);
			// datasource
			DataSource sourceDataSource = new DataSource();
			DataSource targetDataSource = new DataSource();
			sourceDataSource.setName(sourceQueryVO.getDataSouceName());
			targetDataSource.setName(targetQueryVO.getDataSouceName());
			sourceQuery.setDataSource(sourceDataSource);
			targetQuery.setDataSource(targetDataSource);
			
			sourceQuery.setSqlText(sourceQueryVO.getSqlText());
			sourceQuery.setKeyText(sourceQueryVO.getKeyText());
			sourceQuery.setPageText(sourceQueryVO.getPageText());;
			sourceQuery.setMaxDuplicatedKeyCount(sourceQueryVO.getMaxDuplicatedKeyCount());
			sourceQuery.setMaxNullKeyCount(sourceQueryVO.getMaxNullKeyCount());
			//testFields、keyFields、pageFields,testRules
			sourceQuery.setTestFields(convertToEntity(testFields));
			sourceQuery.setKeyFields(convertToEntity(keyFields));
			sourceQuery.setPageFields(convertToEntity(pageFields));
			sourceQuery.setTestRules(ruleConvert(testRules));
			
			targetQuery.setSqlText(targetQueryVO.getSqlText());
			targetQuery.setKeyText(targetQueryVO.getKeyText());
			targetQuery.setPageText(targetQueryVO.getPageText());;
			targetQuery.setMaxDuplicatedKeyCount(targetQueryVO.getMaxDuplicatedKeyCount());
			targetQuery.setMaxNullKeyCount(targetQueryVO.getMaxNullKeyCount());
			//testFields、keyFields、pageFields,testRules
			targetQuery.setTestFields(convertToEntity(testFields));
			targetQuery.setKeyFields(convertToEntity(keyFields));
			targetQuery.setPageFields(convertToEntity(pageFields));
			targetQuery.setTestRules(ruleConvert(testRules));
			
			
			testQueries.add(sourceQuery);
			testQueries.add(targetQuery);
			testQueryService.save(sourceQuery);
			testQueryService.save(targetQuery);
			
			System.out.println("运行了addTestquery……………………………………………………");
			result.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}
		return result.toJSONString();
	}

	/**
	 * @Description: testRuleVO实体转为testRule实体
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年2月18日下午5:55:28
	 */
	private List<TestRule> ruleConvert(List<TestRuleVO> testRuleVOs) {
		ArrayList<TestRule> testRules = new ArrayList<TestRule>();
		for(TestRuleVO testRuleVO:testRuleVOs) {
			TestRule testRule = new TestRule();
			BeanUtils.copyProperties(testRuleVO, testRule);
			testRules.add(testRule);
		}
		return testRules;
	}



	/**
	 * @Description: TestQueryFieldVO转化为TestQueryField
	 * @tips:
	 * 
	 * @author:zhou_xiaolong in 2019年2月18日下午10:30:48
	 */
	private List<TestQueryField> convertToEntity(List<TestQueryFieldVO> voList) {
		List<TestQueryField> sourceTestFields = new ArrayList<>();
		for (TestQueryFieldVO vo : voList) {
			TestQueryField entity = new TestQueryField();
			BeanUtils.copyProperties(vo, entity);
			sourceTestFields.add(entity);
		}
		return sourceTestFields;
	}

	/**
	 * 向一个查询增加查询字段
	 * 
	 * @param testQueryId      查询的ID
	 * @param testQueryFieldId 查询字段的ID
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/addTestSelectField/{testQueryId}/{testQueryFieldId}")
	public String addTestSelectField(@PathVariable("testQueryId") Integer testQueryId,
			@PathVariable("testQueryFieldId") Integer testQueryFieldId) {
		JSONObject result = new JSONObject();
		try {

			if (testQueryId != null) {
				TestQuery entity = testQueryService.findById(testQueryId);
				if (entity == null) {
					throw new Exception("not found testQuery,id:" + testQueryId);
				}
				TestQueryField queryField = testQueryFieldService.findById(testQueryFieldId);
				if (queryField != null) {
					entity.getTestFields().add(queryField);
					result.put("state", "1");
				} else {
				}

			} else {
				throw new Exception("testQueryId is null");
			}
		} catch (Exception e) {
			log.error("", e);
			result.put("state", "0");
		}

		return result.toJSONString();
	}

	/**
	 * 修改一个testquery
	 * 
	 * @param testQueryVO
	 * @return
	 */
	@RequestMapping("/updateTestQuery")
	public String updateTestQuery(@RequestBody TestQueryVO sourceQueryVO ,@RequestBody TestQueryVO targetQueryVO ) {
		JSONObject result = new JSONObject();
		try {
			ArrayList<TestQuery> testQueries = new ArrayList<TestQuery>();
			// 两个对象
			TestQuery sourceQuery = new TestQuery();
			TestQuery targetQuery = new TestQuery();
			// datasouce
			DataSource sourceDataSource = new DataSource();
			DataSource targetDataSource = new DataSource();
			sourceDataSource.setName(sourceQueryVO.getDataSouceName());
			targetDataSource.setName(targetQueryVO.getDataSouceName());
			sourceQuery.setDataSource(sourceDataSource);
			targetQuery.setDataSource(targetDataSource);
			
			sourceQuery.setSqlText(sourceQueryVO.getSqlText());
			sourceQuery.setKeyText(sourceQueryVO.getKeyText());
			sourceQuery.setPageText(sourceQueryVO.getPageText());;
			sourceQuery.setMaxDuplicatedKeyCount(sourceQueryVO.getMaxDuplicatedKeyCount());
			sourceQuery.setMaxNullKeyCount(sourceQueryVO.getMaxNullKeyCount());
			//testFields、keyFields、pageFields,testRules
			sourceQuery.setTestFields(convertToEntity(testFields));
			sourceQuery.setKeyFields(convertToEntity(keyFields));
			sourceQuery.setPageFields(convertToEntity(pageFields));
			sourceQuery.setTestRules(ruleConvert(testRules));
			
			targetQuery.setSqlText(targetQueryVO.getSqlText());
			targetQuery.setKeyText(targetQueryVO.getKeyText());
			targetQuery.setPageText(targetQueryVO.getPageText());;
			targetQuery.setMaxDuplicatedKeyCount(targetQueryVO.getMaxDuplicatedKeyCount());
			targetQuery.setMaxNullKeyCount(targetQueryVO.getMaxNullKeyCount());
			//testFields、keyFields、pageFields,testRules
			targetQuery.setTestFields(convertToEntity(testFields));
			targetQuery.setKeyFields(convertToEntity(keyFields));
			targetQuery.setPageFields(convertToEntity(pageFields));
			targetQuery.setTestRules(ruleConvert(testRules));
			
			testQueries.add(sourceQuery);
			testQueries.add(targetQuery);
			testQueryService.save(sourceQuery);
			testQueryService.save(targetQuery);
			
			result.put("state", "1");
			System.out.println("更新成功………………………………");
		} catch (Exception e) {
			result.put("state", "0");
			e.printStackTrace();

		}

		return result.toJSONString();
	}

}
