package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.entity.DataSchema;
import com.jettech.service.DataSchemaService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestDatabaseVO;

@RestController
@RequestMapping(value = "/testDatabase")
public class DataSchemaController {
	@Autowired
	private DataSchemaService testDatabaseService;
	private static Logger log = LoggerFactory.getLogger(DataSchemaController.class);

	
	/**
	 * 库的复制
	 */
		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "/copyDataSchema")
		public ResultVO copyDataTable(@PathParam(value = "id") Integer id,@PathParam(value = "name") String name) {
	       try {
			return testDatabaseService.copyDataSchema(id, name);
		} catch (Exception e) {
			log.error("复制testdatabase:失败", e);
			 return new ResultVO(false, StatusCode.ERROR, "复制失败");
		}
		}
		
		/**
		 * 添加操作
		 * @param
		 */
		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "/add")
		public ResultVO add(@RequestBody TestDatabaseVO testDatabaseVO) {
	       try{
	    	   return testDatabaseService.add(testDatabaseVO);
			}catch (Exception e) {
				log.error("增加testdatabase:失败", e);
			      return new ResultVO(false, StatusCode.ERROR, "添加失败");
			    }
			
		}
		/**
		 * 更改操作
		 * @param
		 */
		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "/update")
		public ResultVO update(@RequestBody TestDatabaseVO testDatabaseVO) {
	       try{
	    	   return testDatabaseService.update(testDatabaseVO);
			}catch (Exception e) {
				log.error("更改testdatabase:" + testDatabaseVO.getId() + "失败", e);
			      return new ResultVO(false, StatusCode.ERROR, "修改失败");
			    }
			
		}
		/**
		 * 删除操作
		 * 
		 * @param id
		 */
		@ResponseBody
		@RequestMapping(method = RequestMethod.POST, value = "/delete")
		public ResultVO delete(@PathParam(value = "id") Integer id) {
			try {
				return testDatabaseService.delete(id);
			} catch (Exception e) {
				log.error("删除失败"+e);
				return new ResultVO(false, StatusCode.ERROR, "删除失败");			}
		}
		

		/**
		 * 选择一个数据库名称id导出数据字典
		 * 
		 * @param id
		 * @return
		 */
		@RequestMapping(value = "/getDatabase")
		public String getDatabase(HttpServletResponse response,
				@PathParam(value = "id") int id) throws Exception {
			try {
				return testDatabaseService.getDatabase(response, id);
			} catch (Exception e) {
				log.error("导出失败"+e);
				return "导出失败";
			}
		}
	/**
	 * 选择不同的表id导出数据字典
	 * @param response
	 * @param ids
	 * @return
	 * @throws Exception
	 */
		@RequestMapping(value = "/downloadSelect", method = RequestMethod.POST)
		public String downloadSelect(HttpServletResponse response,
				@RequestBody List<Integer> ids) throws Exception {
			try {
			return testDatabaseService.downloadSelect(response, ids);
		} catch (Exception e) {
			log.error("导出失败"+e);
			return "导出失败";
		}
		}
		/**
		 * 同步选择库下面所有的表字段
		 * @param map
		 * @return
		 */
		@ResponseBody
	    @RequestMapping(value="/setOneDataSchema",produces = { "application/json;charset=UTF-8" })
	    public  ResultVO setOneDataSchema(@PathParam(value = "testDataBaseId") Integer testDataBaseId){
	        try {
	        	testDatabaseService.SetOneDataSchema(testDataBaseId);
		    }catch(Exception e) {
		    	e.printStackTrace();
		    	log.info("同步失败"+e);
			    return new ResultVO(false, StatusCode.ERROR, "同步失败"+e);

		    }
	          log.info("同步成功");
		      return new ResultVO(true, StatusCode.OK, "同步成功");
		}
		
		/**
		 * 获得所有的数据库信息
		 * @param dataSourceId
		 * @param pageNum
		 * @param pageSize
		 * @return
		 */
		@ResponseBody
	    @RequestMapping(value="/getAllDatabase",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
	    public  ResultVO getAllDatabaseByPage(){
			Map<String,Object> resultmap = new HashMap<String,Object>();
			List<TestDatabaseVO> arrvolist=new ArrayList<>();
	        try {     	
	        	List<DataSchema>    tdlist= testDatabaseService.getAllDataSchema();
		        for(DataSchema td : tdlist) {
		        	TestDatabaseVO testDatabaseVO = new TestDatabaseVO(td);
		        	arrvolist.add(testDatabaseVO);
		        }
		        resultmap.put("list",arrvolist);
		    }catch(Exception e) {
		    	log.error("查询失败"+e);
		    	return new ResultVO(false, StatusCode.ERROR, "查询失败");
		    }
	        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		}
		
		/**
		 * 通过dataSourceID获取schema信息
		 * @param dataSourceID
		 * @return
		 */
		@ResponseBody
	    @RequestMapping(value="/getSchemasByDataSourceID",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
	    public  ResultVO getSchemaByDataSourceID(@PathParam(value = "dataSourceID") Integer dataSourceID,@PathParam(value = "schemaName") String schemaName){
			Map<String,Object> resultmap = new HashMap<String,Object>();
			List<TestDatabaseVO> arrvolist=new ArrayList<>();
			if(schemaName == null){
				schemaName = "";
			}
	        try {
	        	List<DataSchema> tdlist = testDatabaseService.getSchemasByDataSourceID(dataSourceID,schemaName);
		        for(DataSchema td : tdlist) {
		        	TestDatabaseVO testDatabaseVO = new TestDatabaseVO(td);
		        	arrvolist.add(testDatabaseVO);
		        }
		        resultmap.put("list",arrvolist);
		        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		    }catch(Exception e) {
		    	log.error("查询失败"+e);
		    	e.getLocalizedMessage();
		    	return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
		    }
		}
		
		/**
		 * 通过dataSourceID和schemaName获取schema信息
		 * @param dataSourceID
		 * @return
		 */
		@ResponseBody
	    @RequestMapping(value="/getSchmaByDataSourceIdAndName",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
	    public  ResultVO getSchmaByDataSourceIdAndName(@PathParam(value = "dataSourceID") Integer dataSourceID,@PathParam(value = "schemaName") String schemaName){
			Map<String,Object> resultmap = new HashMap<String,Object>();
			if(schemaName == null){
		    	return new ResultVO(false, StatusCode.ERROR, "schemaName不能为空", resultmap);
			}
	        try {
	        	DataSchema dataSchema = testDatabaseService.getSchemasByDataSourceIdAndName(dataSourceID,schemaName);
	        	if(dataSchema==null){
	        		return new ResultVO(false, StatusCode.ERROR, "这个数据源下面没有该名称的数据库", resultmap);
	        	}
	        	TestDatabaseVO testDatabaseVO=new TestDatabaseVO(dataSchema);
	        	resultmap.put("list",testDatabaseVO);
		        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		    }catch(Exception e) {
		    	log.error("查询失败"+e);
		    	e.getLocalizedMessage();
		    	return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
		    }
		}
}
