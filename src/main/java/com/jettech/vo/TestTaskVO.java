package com.jettech.vo;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestTask;

public class TestTaskVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3951654233572844767L;

	private Integer productId;

	private Integer testSuiteId;

	private String name;

	private String cron;

	private Integer threadNum;

	private String testSuiteName;

	private Integer testCaseNum;

	private boolean actived;// true激活中false未激活

	private boolean status;// 执行状态 true执行中false未执行

	private int type;//集合类型，0迁移，1质量

	public TestTaskVO() {
	}

	public TestTaskVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestTask e = (TestTask) entity;
			this.productId = e.getTestSuite().getProduct().getId();
			this.testSuiteId = e.getTestSuite().getId();
//			this.name = e.getName();
//			this.cron = e.getCron();
//			this.threadNum = e.getThreadNum();
//			this.actived = e.isActived();
		}
	}

	public TestTaskVO(BaseEntity entity, Integer testCaseNum) {
		super(entity);
		TestTask e = (TestTask) entity;
		this.productId = e.getTestSuite().getProduct().getId();
		this.testSuiteId = e.getTestSuite().getId();
		this.name = e.getName();
		this.cron = e.getCron();
		this.threadNum = e.getThreadNum();
		this.actived = e.isActived();
		this.testSuiteName = e.getTestSuite().getName();
		this.status = e.isStatus();
		this.testCaseNum = testCaseNum;
	}

	public Integer getProductId() {
		return productId;
	}

	public void setProductId(Integer productId) {
		this.productId = productId;
	}

	public Integer getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(Integer testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public Integer getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(Integer threadNum) {
		this.threadNum = threadNum;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public Integer getTestCaseNum() {
		return testCaseNum;
	}

	public void setTestCaseNum(Integer testCaseNum) {
		this.testCaseNum = testCaseNum;
	}

	public boolean isActived() {
		return actived;
	}

	public void setActived(boolean actived) {
		this.actived = actived;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}


	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
