package com.jettech.vo;

import com.jettech.entity.BaseEntity;

public class TaskResultVO extends BaseVO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8531440313437619706L;

	private TestSuiteVO testSuiteVO;

	private ProductVO productVO;

	private TestTaskVO testTaskVO;

	public TaskResultVO(BaseEntity entity) {
		super(entity);
	}

	public TaskResultVO() {
	}

	public TestSuiteVO getTestSuiteVO() {
		return testSuiteVO;
	}

	public void setTestSuiteVO(TestSuiteVO testSuiteVO) {
		this.testSuiteVO = testSuiteVO;
	}

	public ProductVO getProductVO() {
		return productVO;
	}

	public void setProductVO(ProductVO productVO) {
		this.productVO = productVO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public TestTaskVO getTestTaskVO() {
		return testTaskVO;
	}

	public void setTestTaskVO(TestTaskVO testTaskVO) {
		this.testTaskVO = testTaskVO;
	}

}
