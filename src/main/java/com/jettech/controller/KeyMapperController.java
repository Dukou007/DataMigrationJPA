package com.jettech.controller;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.CompFields;
import com.jettech.service.IKeyMapperService;

@RestController
public class KeyMapperController {

	@Autowired
	private IKeyMapperService keyMapperServiceImpl;
	
	//private final static String filePath = "D://tmp/";
	private final static String filePath = File.separator+"wls"+File.separator+"wls81"+File.separator+"tmp"+File.separator;
	
	@GetMapping("/loadKeyMapper/{fileName}")
	public String loadKeyMapper(@PathVariable("fileName")String fileName) {
		JSONObject result = new JSONObject();
		try {
		
			keyMapperServiceImpl.loadKeyMapper(filePath+fileName);
			 result.put("result", "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toJSONString();
	}
}
