/**
 * 
 */
package com.jettech.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.service.ITestTableService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestTableVO;

/**
 * @author zhou_xiaolong
 * @Description: testTable，查询与新增功能；后续功能根据需求增加
 * @date: 2019年2月3日 上午11:29:44
 */

@RestController
@RequestMapping("/testTable")
public class DataTableController {
	private static Logger log = LoggerFactory.getLogger(DataTableController.class);

	
	@Autowired
	private ITestTableService testTableService;

	/**
	 * @Description: 查询
	 * 
	 * 
	 *  @author:zhou_xiaolong in 2019年2月3日
	 */
	@ResponseBody
	@RequestMapping("/getTestTables")
	public String getTestTables() {
		JSONObject result = new JSONObject();
		try {
			List<DataTable> testTables = testTableService.findAll();
			ArrayList<TestTableVO> testTableVOs = new ArrayList<TestTableVO>();
			for (DataTable testTable : testTables) {
				TestTableVO testTableVO = new TestTableVO(testTable);
				testTableVOs.add(testTableVO);
				result.put("state", "1");
				result.put("rows", testTableVOs);
			}

		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}

		return result.toJSONString();
	}

	/**
	 * 增加testTables
	 */
	@ResponseBody
	@RequestMapping("/addTestTable")
	public String addTestTable(@RequestBody TestTableVO testTableVO) {
		JSONObject result = new JSONObject();
		DataTable testTable = new DataTable();
		BeanUtils.copyProperties(testTableVO, testTable);
		try {
			testTable.setId(testTableVO.getId());
			testTableService.save(testTable);
			result.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			result.put("state", "0");
		}

		return result.toJSONString();
	}
	/**
	 * 复制表
	 * @param
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/copyDataTable")
	public ResultVO copyDataTable(@PathParam(value = "id") Integer id,@PathParam(value = "name") String name) {
       return testTableService.copyDataTable(id, name);
	}
	/**
	 * 同步选择表下面所有的字段
	 * @param map
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/setOneDataTable",produces = { "application/json;charset=UTF-8" })
    public  ResultVO setOneDataTable(@PathParam(value = "testTableId") Integer testTableId){
        try {
        	testTableService.SetOneDataTable(testTableId);
	    }catch(Exception e) {
	    	log.info("同步失败"+e);
		    return new ResultVO(false, StatusCode.ERROR, "同步失败");

	    }
	      return new ResultVO(true, StatusCode.OK, "同步成功");
	}
}
