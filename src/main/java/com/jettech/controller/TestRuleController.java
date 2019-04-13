
package com.jettech.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.TestRule;
import com.jettech.service.TestRuleService;
import com.jettech.vo.TestRuleVO;



/**
 * @author zhou_xiaolong
 * @Description: testRules 的curd，若后续需求变动，酌情调整；
 * @date: 2019年2月2日 上午9:45:40
 */
@RequestMapping("/testRules")
@RestController
public class TestRuleController {

	@Autowired
	private TestRuleService testRuleService;

	/**
	 * @Description: 获取所有的测试规则
	 * 
	 * 
	 * @author zhou_xiaolong in 2019年2月2日
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllTestRules")
	public String getAllTestRules() {
		JSONObject result = new JSONObject();
		List<TestRule> listRules = testRuleService.findAll();
		ArrayList<TestRuleVO> TestRuleVOs = new ArrayList<TestRuleVO>();
		try {
			for (TestRule testRule : listRules) {
				TestRuleVO testRuleVO = new TestRuleVO();
				BeanUtils.copyProperties(testRule, testRuleVO);
				testRuleVO.setId(testRule.getId());
				testRuleVO.setRuleValue(testRule.getRuleValue());
				testRuleVO.setPosition(testRule.getPosition());
				TestRuleVOs.add(testRuleVO);
				result.put("state", "1");
				result.put("rows", TestRuleVOs);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}
		return result.toJSONString();
	}

	/**
	 * @Description: 查找一个testRule
	 * 
	 * 
	 * @author zhou_xiaolong in 2019年2月2日
	 */
	@ResponseBody
	@RequestMapping("/getOneTestRule")
	public String getOneTestRule(@RequestBody Integer testRuleID) {
		
		JSONObject result = new JSONObject();
		try {
			TestRule testRule = testRuleService.findById(testRuleID);
			TestRuleVO testRuleVO = new TestRuleVO();
			BeanUtils.copyProperties(testRule, testRuleVO);
			testRuleVO.setId(testRule.getId());
			result.put("state", "1");
		}catch(Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}
		return result.toJSONString();
		
	}
	
	
	/**
	 * @Description: 新增testRule
	 * 
	 * 
	 *  @author zhou_xiaolong in 2019年2月2日
	 */
	@ResponseBody
	@RequestMapping(value = "/addTestRule")
	public String addTestRule(@RequestBody TestRuleVO testRuleVO) {
		JSONObject result = new JSONObject();
		try {
			TestRule testRule = new TestRule();
			BeanUtils.copyProperties(testRuleVO, testRule);
			testRule.setId(testRuleVO.getId());
			testRule.setPosition(testRuleVO.getPosition());
			testRule.setRuleValue(testRuleVO.getRuleValue());
			testRuleService.save(testRule);

			result.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}
		return result.toJSONString();

	}

	/**
	 * @Description: 修改testRule
	 * 
	 * 
	 * @author zhou_xiaolong in 2019年2月2日
	 */
	@ResponseBody
	@RequestMapping("/updateTestRule")
	public String updateTestRule(@RequestBody TestRuleVO testRuleVO) {
		JSONObject result = new JSONObject();

		try {
			TestRule testRule = new TestRule();
			BeanUtils.copyProperties(testRuleVO, testRule);
			testRule.setPosition(testRuleVO.getPosition());
			testRule.setRuleValue(testRuleVO.getRuleValue());
			testRuleService.save(testRule);
			result.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}

		return result.toJSONString();
	}
	
	
	/**
	 * @Description: 删除testrule
	 * 
	 * 
	 * @author zhou_xiaolong in 2019年2月2日
	 */
	@ResponseBody
	@RequestMapping("/deleteTestRule")
	public String deleteTestRule(Integer testRuleID){
		JSONObject result = new JSONObject();
		
		try {
			testRuleService.delete(testRuleID);
			result.put("state", "1");
		}
		catch(Exception e) {
			e.printStackTrace();
			result.put("state", 0);
		}
		
		return result.toJSONString();
	}

}
