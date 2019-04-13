package com.jettech.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestRound;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestRoundVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5503907721257549776L;
	private Date startTime;
	private Date endTime;
	private Integer successCount;
	private Integer caseCount;
	private Integer failCount;
	private String suiteName;
	private Integer SuiteID;

	public TestRoundVO() {
	}

	public TestRoundVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestRound e = (TestRound) entity;
			if (e.getTestSuite() != null)
				this.suiteName = e.getTestSuite().getName();
				this.SuiteID = e.getTestSuite().getId();
				this.successCount = e.getSuccessCount();
				this.failCount = e.getCaseCount() - e.getSuccessCount();
		}
	}

	public String getSuiteName() {
		return suiteName;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}

	public Integer getCaseCount() {
		return caseCount;
	}

	public void setCaseCount(Integer caseCount) {
		this.caseCount = caseCount;
	}

	public Integer getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Integer getFailCount() {
		return failCount;
	}

	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}

	public Integer getSuiteID() {
		return SuiteID;
	}

	public void setSuiteID(Integer suiteID) {
		SuiteID = suiteID;
	}

}
