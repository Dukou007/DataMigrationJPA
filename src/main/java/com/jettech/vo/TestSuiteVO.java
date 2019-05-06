package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestSuite;

public class TestSuiteVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2420703764541953184L;

	private String name;

	private Integer productID;
	
	private Integer testCaseNumber;
	
	private String productName;
	
	private int type;//集合类型，0迁移，1质量
	public TestSuiteVO() {
	}

	public TestSuiteVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestSuite testSuite = (TestSuite) entity;
			this.name = testSuite.getName();
			if(testSuite.getType() == 0){

			}else if(testSuite.getType() == 1){
				this.testCaseNumber = testSuite.getQualityTestCases().size();
			}
			if (testSuite.getProduct() != null)
				this.productID = testSuite.getProduct().getId();
				this.productName=testSuite.getProduct().getName();
				this.type=testSuite.getType();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getProductID() {
		return productID;
	}

	public void setProductID(Integer productID) {
		this.productID = productID;
	}

	/**
	 * @return the testCaseNumber
	 */
	public Integer getTestCaseNumber() {
		return testCaseNumber;
	}

	/**
	 * @param testCaseNumber the testCaseNumber to set
	 */
	public void setTestCaseNumber(Integer testCaseNumber) {
		this.testCaseNumber = testCaseNumber;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
