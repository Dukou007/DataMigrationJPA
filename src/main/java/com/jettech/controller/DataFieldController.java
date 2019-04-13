/**
 * 
 */
package com.jettech.controller;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.DataField;
import com.jettech.service.ITestFieldService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.TestFieldVO;

/**
 *  @author zhou_xiaolong
 *	@Description: TestField,查看详情功能；后续需求变动，作适当调整。
 *  @date: 2019年2月3日 上午10:06:18 
 */


@RestController
@RequestMapping(value="/testField")
public class DataFieldController {
	
	@Autowired
	private ITestFieldService testFieldService;
	
	/**
	 * @Description: 查询所有的testFields
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年2月18日下午10:22:13
	 */
	@ResponseBody
	@RequestMapping("/getAllTestField")
	public String getAllTestField() {
		JSONObject result = new JSONObject();
		try {
			List<DataField> testFields = testFieldService.findAll();
			ArrayList<TestFieldVO> testFieldVOs = new ArrayList<TestFieldVO>();
			for (DataField testField: testFields) {
				TestFieldVO testFieldVO = new TestFieldVO();
				BeanUtils.copyProperties(testField, testFieldVO);
				testFieldVO.setId(testField.getId());
				testFieldVO.setTestTableId(testField.getDataTable().getId());
				testFieldVOs.add(testFieldVO);
			}
			result.put("state", "1");
			result.put("rows", testFieldVOs);
		}catch(Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}
		return result.toJSONString();
	}
	
	/**
	 * 字段的复制
	 */
		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "/copyDataField")
		public ResultVO copyDataField(@PathParam(value = "id") Integer id,@PathParam(value = "name") String name) {
	       return testFieldService.copyDataField(id, name);
		}
}
