package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.jettech.entity.QualitySuite;
import com.jettech.service.IQualitySuiteService;
import com.jettech.vo.QualitySuiteVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@RestController
@RequestMapping(value = "/qualitySuite")
public class QualitySuiteController {

	private static Logger log = LoggerFactory.getLogger(QualitySuiteController.class);
	
	@Autowired
	private IQualitySuiteService qualitySuiteService;
	
	@ResponseBody
	@RequestMapping("/getAllQualitySuite")
	public ResultVO getAllTestField() {
		Map<String,Object> resultmap = new HashMap<String,Object>();
		try {
			List<QualitySuite> list = qualitySuiteService.findAll();
			ArrayList<QualitySuiteVO> listVO = new ArrayList<QualitySuiteVO>();
			for (QualitySuite qualitySuite: list) {
				QualitySuiteVO qualitySuiteVO = new QualitySuiteVO(qualitySuite);
				listVO.add(qualitySuiteVO);
			}
			 resultmap.put("list",listVO);
			 log.info("查询规则集成功。。。");;
			return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
		}catch(Exception e) {
			e.printStackTrace();
			log.error("查询规则集失败。。。"+e.getMessage());
			return new ResultVO(false, StatusCode.ERROR, "查询失败", resultmap);
		}
	}


	/**
	 * 添加修改操作
	 * 添加集合，集合中包含所涉及的规则id
	 * @param
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/addAndUpdate")
	public ResultVO add(@RequestBody List<QualitySuiteVO> qualitySuiteVOs) {
		return qualitySuiteService.add(qualitySuiteVOs);
	}
	/**
	 * 删除操作
	 *
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
	public ResultVO delete(@PathVariable("id") int id) {
		return qualitySuiteService.delete(id);
	}

	/**
	 * 根据id查询
	 *
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "selectOneById/{id}")
	public ResultVO getOneById(@PathVariable("id") int id) {
		return  qualitySuiteService.getOneById(id);
	}
	/**
	 * 查询所有的
	 *
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "selectAll/{pageNum}/{pageSize}")
	public ResultVO getAll(@PathVariable("pageNum") int pageNum,
						   @PathVariable("pageSize") int pageSize) {
		return  qualitySuiteService.getAll(pageNum, pageSize);
	}





}
