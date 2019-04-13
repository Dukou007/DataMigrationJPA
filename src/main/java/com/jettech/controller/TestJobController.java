package com.jettech.controller;

import java.util.List;

//import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//import com.alibaba.druid.filter.Filter;
import com.alibaba.fastjson.JSONObject;
//import com.github.pagehelper.PageHelper;

@RestController
@RequestMapping("/testJob")
public class TestJobController {

//	private static Logger log = Logger.getLogger(TestJobController.class);
//	
//	@Autowired
//	private IFilterService filterService;
//	
////	@RequestMapping(value = "/getFilterIndex")
////	public String getFilterIndex() {
////		return "views/FilterIndex";
////	}
//	
//   @ResponseBody
//   @RequestMapping(value="getJobList",produces = { "application/json;charset=UTF-8" })
//   public  String getJobList(@RequestParam int pageNumber,int pageSize,int id){
//		JSONObject result = new JSONObject();
//		try {
//			PageHelper.startPage(pageNumber,pageSize);
//			List<Filter> pageInfo = filterService.findAllFilterByProductId(pageNumber,pageSize,id);
//			result.put("rows",pageInfo);
//			result.put("total",pageInfo.size());
//			result.put("state", "1");
//		} catch (Exception e) {
//			result.put("state", "0");
//			e.printStackTrace();
//			log.error(e);
//		}
//		return result.toString();
//	}
//	
//	 
//   @ResponseBody
//   @RequestMapping("/addFilter")
//   public String addFilter(Filter filter) {
//   //	System.out.println(filter.toString());
//   	JSONObject result = new JSONObject();
//		try {
//			filterService.addFilter(filter);
//			result.put("state", "1");
//		} catch (Exception e) {
//			result.put("state", "0");
//			e.printStackTrace();
//			log.error(e);
//		}
//		return result.toString();
//   }
//   
//   @ResponseBody
//   @RequestMapping("/updateFilter")
//	    public String updateFilter(Filter filter) {
//   	JSONObject result = new JSONObject();
//		try {
//			filterService.updateFilter(filter);
//			result.put("state", "1");
//		} catch (Exception e) {
//			result.put("state", "0");
//			e.printStackTrace();
//			log.error(e);
//		}
//		return result.toString();
//   }
//   
//   @ResponseBody
//   @RequestMapping("/deleteFilter")
//   public String deleteFilter(HttpServletRequest request) {
//		String[] list=request.getParameterValues("ids");
//		JSONObject result = new JSONObject();
//		 try {
//			for (int i = 0; i < list.length; i++) {
//			    int id = Integer.parseInt(list[i]);
//				filterService.deleteFilter(id);//删除产品
//			}
//			result.put("state", "1");
//		} catch (Exception e) {
//			result.put("state", "0");
//			e.printStackTrace();
//			log.error(e);
//		}
//		return result.toString();
//   }
}
