package com.jettech.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.jettech.entity.MapperEntity;
import com.jettech.entity.TestCase;
import com.jettech.service.IMapperService;


@RestController
public class MapperController {

	//private final static String filePath = "D://tmp/";
	//private final static String filePath = File.separator+"wls"+File.separator+"wls81"+File.separator+"tmp"+File.separator;
	@Value("${file.filePath}")
	private String filePath;
	@Autowired
	private IMapperService mapperServiceImpl;
	
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true)); // true:允许输入空值，false:不能为空值

    }
	
	
	@GetMapping("/loadMapper/{fileName}")
	public String loadMapper(@PathVariable("fileName")String fileName) throws Exception {
		JSONObject result = new JSONObject();
		try {
			mapperServiceImpl.loadMapper(filePath+fileName, MapperEntity.class);
			result.put("result", "success");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toJSONString();
	}
}
