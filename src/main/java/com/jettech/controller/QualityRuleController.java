package com.jettech.controller;

import com.jettech.service.IQualityRuleService;
import com.jettech.vo.QualityRuleVO;
import com.jettech.vo.ResultVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 
 * @author ljl
 *
 * Makedate:2019年1月31日 下午1:45:51
 */
@RestController
@RequestMapping(value = "/qualityRule")
public class QualityRuleController {

	@Autowired
	private IQualityRuleService qualityRuleService;
	
	
	/**
	 * 添加操作
	 * 多对多关系的绑定由关系维护端来完成，关系被维护端不能绑定关系，所以只能有集合来绑定关系
	 * @param
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/addAndUpdate")
	public ResultVO add(@RequestBody QualityRuleVO qualityRuleVO) {
		return qualityRuleService.add(qualityRuleVO);
	}
	/**
	 * 删除操作
	 * 
	 * @param id
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
	public ResultVO delete(@PathVariable("id") int id) throws Exception {
		ResultVO rs = qualityRuleService.delete(id);
			return rs;
		
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
		return qualityRuleService.getOneById(id);
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
		return qualityRuleService.getAll(pageNum, pageSize);
	}
}
