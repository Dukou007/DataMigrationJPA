package com.jettech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.service.IQualityCodeMapService;
import com.jettech.vo.QualityCodeMapVO;
import com.jettech.vo.ResultVO;

/**
 *
 * @author ljl
 *
 * Makedate:2019年2月21日 下午2:44:52
 */
@RestController
@RequestMapping(value = "/qualityCodeMap")
public class QualityCodeMapController {
	@Autowired
	private IQualityCodeMapService codeMapService;

	/**
	 * 保存操作或修改
	 *
	 * @param codeMap
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/insert")
	public ResultVO insert(@RequestBody QualityCodeMapVO qualityCodeMapVO) {
		return codeMapService.insert(qualityCodeMapVO);
	}
	/**
	 * 删除操作
	 *
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/delete/{id}")
	public ResultVO delete(@PathVariable("id") int id) {
		return codeMapService.delete(id);
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
		return codeMapService.getOneById(id);
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
		return codeMapService.getAll(pageNum, pageSize);
	}

}
