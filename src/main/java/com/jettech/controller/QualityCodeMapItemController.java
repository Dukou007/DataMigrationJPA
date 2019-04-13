package com.jettech.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.service.IQualityCodeMapItemService;
import com.jettech.vo.QualityCodeMapItemVO;
import com.jettech.vo.ResultVO;

@RestController
@RequestMapping(value = "/qualityCodeMapItem")
public class QualityCodeMapItemController {
	@Autowired
	private IQualityCodeMapItemService codeMapItemService;

	/**
	 * 保存操作或修改
	 *
	 * @param codeMap
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/insert")
	public ResultVO insert(@RequestBody QualityCodeMapItemVO qualityCodeMapItemVO) {
		return codeMapItemService.save(qualityCodeMapItemVO);
	}
	/**
	 * 删除操作
	 *
	 * @param id
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
	public ResultVO delete(@PathVariable("id") int id) {
		return codeMapItemService.delete(id);
	}
	/**
	 * 根据id查询
	 *
	 * @param id
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET, value = "selectOneById/{id}")
	public ResultVO getOneById(@PathVariable("id") int id) {
		return codeMapItemService.getOneById(id);
	}
	/**
	 * 根据code_id查询
	 *
	 * @param id
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET, value = "selectByCodeMapId/{id}")
	public ResultVO getAllByCodeMapId(@PathVariable("id") int id) {
		return codeMapItemService.getAllByCodeMapId(id);
	}

	/**
	 * 查询所有，带分页
	 *
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "getAll/{pageNum}/{pageSize}")
	public ResultVO getAll(@PathVariable("pageNum") int pageNum,
						   @PathVariable("pageSize") int pageSize){
		return codeMapItemService.findAllByPage(pageNum,pageSize);
	}

}
