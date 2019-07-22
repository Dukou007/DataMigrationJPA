package com.jettech.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.stereotype.Component;

/**
 * 执行结果明细表，该表的名称不是固定的，根据案例和时间戳来定义
 * 
 * @author tan
 *
 */
@Entity
@Table(name = "test_result_item", indexes={ @Index(name="result_id", unique=false, columnList="testResultId")})
public class TestResultItem extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2915840692680172674L;

	private Integer testResultId;
	private String keyValue;
	private String result;
	private String columnName;
	private String sourceValue;
	private String targetValue;

	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn(name = "test_result_id")
	// public TestResult getTestReuslt() {
	// return testReuslt;
	// }
	//
	// public void setTestReuslt(TestResult testReuslt) {
	// this.testReuslt = testReuslt;
	// }

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	@Column(columnDefinition="text")
	public String getSourceValue() {
		return sourceValue;
	}

	public void setSourceValue(String sourceValue) {
		this.sourceValue = sourceValue;
	}

	@Column(columnDefinition="text")
	public String getTargetValue() {
		return targetValue;
	}

	public void setTargetValue(String targetValue) {
		this.targetValue = targetValue;
	}

	/**
	 * 关联到主表的ID，不做外键
	 * 
	 * @return
	 */
//	@Column(name = "test_result_id", nullable = false)
	public Integer getTestResultId() {
		return testResultId;
	}

	public void setTestResultId(Integer testResultId) {
		this.testResultId = testResultId;
	}

}
