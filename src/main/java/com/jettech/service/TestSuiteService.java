package com.jettech.service;

import com.jettech.BizException;
import com.jettech.entity.TestSuite;
import com.jettech.vo.TestSuiteVO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TestSuiteService extends IService<TestSuite, Integer> {

	String doTestSuite(Integer testSuiteId);

	TestSuite getByName(String name);

	TestSuite getByName(String testSuiteName, String productName);

	Page<TestSuite> getByNameLikeList(String name, Pageable pageable);
	public void delete(String ids);
	/**
	 * 获取一个产品下的所有测试集,包含子产品的
	 * @param productId
	 * @return
	 */
	List<TestSuiteVO> findByProductId(Integer productId);

	/**
	 * @Description: 根据名称查找测试集
	 * @tips:
	 * 
	 * @author:zhou_xiaolong in 2019年2月21日下午6:18:53
	 */
	List<TestSuite> findTestSuiteByName(String name);

	/**
	 * @Description: 根据名称查找测试集分页
	 * @tips:
	 * 
	 * @author:zhou_xiaolong in 2019年2月22日下午4:13:14
	 */
	Page<TestSuite> getAllTestSuiteByPage(String name, Pageable pageable);

	/**
	 * @Description: 根据ID查找并分页
	 * @Tips: null;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月24日下午5:37:13
	 */
	Page<TestSuite> getAllTestSuiteByProductID(Integer productID, Pageable pageable);

	/**
	 * @Description: 查找所有分页
	 * @Tips: null;
	 * @State: being used / drop
	 * @author:zhou_xiaolong in 2019年2月24日下午7:15:35
	 */
	Page<TestSuite> getTestSuiteList(Pageable pageable);

	String doTest(Integer testSuiteID);

	void copyTestSuite(Integer testSuiteId) throws BizException;

	//质量执行测试集方法添加20190321
	String doQualityTestSuite(Integer testSuiteId);

	List<TestSuite> getBySuiteNameAndProductId(String name, Integer productID);

}
