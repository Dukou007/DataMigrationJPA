package com.jettech.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.entity.TestResult;
import com.jettech.service.ITestReusltService;
import com.jettech.vo.PageResult;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@RestController
@RequestMapping("/test")
public class TestController {
	@Autowired
	private ITestReusltService testReusltService;

	/**
	 * 删除操作
	 * 
	 * @param id
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/delete/{id}")
	public ResultVO delete(@PathVariable("id") int id) {
		testReusltService.delete(id);
		return new ResultVO(true, StatusCode.OK, "删除成功");
	}

	/**
	 * 保存操作
	 * 
	 * @param testResult
	 */

	@RequestMapping(method = RequestMethod.POST, value = "/insert")
	public ResultVO insert(@RequestBody TestResult testResult) {
		testReusltService.save(testResult);
		return new ResultVO(true, StatusCode.OK, "保存成功");
	}

	/**
	 * 更新操作
	 * 
	 * @param testResult
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update/{id}")
	public ResultVO update(@PathVariable("id") int id, @RequestBody TestResult testResult) {
		testResult.setId(id);
		testReusltService.save(testResult);
		return new ResultVO(true, StatusCode.OK, "更新成功");
	}

	/**
	 * 根据id查询
	 * 
	 * @param id
	 * @return
	 */

	@RequestMapping(method = RequestMethod.GET, value = "selectOneById/{id}")
	public ResultVO getTestReusltById(@PathVariable("id") int id) {
		TestResult testResult = testReusltService.findById(id);
		return new ResultVO(true, StatusCode.OK, "查询成功", testResult);
	}

	/**
	 * 查询所有，带分页
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */

	@RequestMapping(method = RequestMethod.POST, value = "selectResultList/{pageNum}/{pageSize}")
	public ResultVO selectResultList(@RequestBody(required=false) TestResult testResult, @PathVariable("pageNum") int pageNum,
			@PathVariable("pageSize") int pageSize) {

		// 1.根据条件查询标签
		Page<TestResult> labels = testReusltService.findPage(testResult, pageNum, pageSize);
		// 2.创建分页的封装结果集
		PageResult<TestResult> pageResult = new PageResult<>(labels.getTotalElements(), labels.getContent());
		// 2.创建返回值并返回
		return new ResultVO(true, StatusCode.OK, "查询成功", pageResult);
	}
}