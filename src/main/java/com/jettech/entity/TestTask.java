package com.jettech.entity;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity
public class TestTask extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7209123082596859086L;
	private String name;
	private String cron;
	private Integer threadNum; // 线程数
	private TestSuite testSuite;
	private boolean actived;//true激活中false未激活
	private boolean status;//执行状态 true执行中false未执行
	
	public TestTask() {
		super();
	}


	public Integer getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(Integer threadNum) {
		this.threadNum = threadNum;
	}

	public TestTask(String cron, Integer threadNum) {
		super();
		this.cron = cron;
		this.threadNum = threadNum;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_suite_id")
	public TestSuite getTestSuite() {
		return testSuite;
	}

	public void setTestSuite(TestSuite testSuite) {
		this.testSuite = testSuite;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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


	public String getCron() {
		return cron;
	}


	public void setCron(String cron) {
		this.cron = cron;
	}

}
