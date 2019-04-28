/**
 * 
 */
package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.jettech.vo.StatusCode;
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
		
		/**
		 * 通过schemaID获取表信息
		 * @param schemaID
		 * @return
		 */
		@ResponseBody
	    @RequestMapping(value="/getFieldsByTableID",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
	    public  ResultVO getFieldsByTableID(@PathParam(value = "tableID") Integer tableID,@PathParam(value = "fieldName") String fieldName){
			Map<String,Object> resultmap = new HashMap<String,Object>();
			List<TestFieldVO> arrvolist=new ArrayList<>();
	        try {
	        	List<DataField> dtlist= testFieldService.findFieldNameByTableID(tableID,fieldName);
		        for(DataField df : dtlist) {
		        	TestFieldVO testFieldVO = new TestFieldVO(df);
		        	arrvolist.add(testFieldVO);
		        }
		        resultmap.put("list",arrvolist);
		        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		    }catch(Exception e) {
		    	e.printStackTrace();
		    	e.getLocalizedMessage();
		    	return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
		    }
		}
}
