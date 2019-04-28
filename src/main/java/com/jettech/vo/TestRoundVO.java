package com.jettech.vo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestRound;
import com.jettech.util.DateUtil;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestRoundVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5503907721257549776L;
	private String startTime;
	private String endTime;
	private Integer successCount;
	private Integer caseCount;
	private Integer failCount;
	private String suiteName;
	private Integer SuiteID;

	public TestRoundVO() {
	}

	public TestRoundVO(BaseEntity entity) throws ParseException {
		super(entity);
		if (entity != null) {
			TestRound e = (TestRound) entity;
			if (e.getTestSuite() != null)
				this.suiteName = e.getTestSuite().getName();
				this.SuiteID = e.getTestSuite().getId();
				this.successCount = e.getSuccessCount();
				if(e.getCaseCount()!=null&&e.getCaseCount()>0) {
					this.failCount = e.getCaseCount() - e.getSuccessCount();
				}else {
					this.caseCount=0;
					this.successCount=0;
					this.failCount=0;
				}
				String newStartTime = e.getStartTime().toString();
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				this.startTime=df.parse(newStartTime).toString();
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


	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
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
