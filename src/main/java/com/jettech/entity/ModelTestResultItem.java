package com.jettech.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.springframework.stereotype.Component;

/**
 * 执行结果明细表，该表的名称不是固定的，根据案例和时间戳来定义
 * 
 * @author tan
 *
 */
@Entity
@Component
public class ModelTestResultItem extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2915840692680172674L;

	private Integer testResultId;
	private String keyValue;
	private String result;
	private String columnName;
	private String soruceValue;
	private String tragetValue;

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
	public String getSoruceValue() {
		return soruceValue;
	}

	public void setSoruceValue(String soruceValue) {
		this.soruceValue = soruceValue;
	}

	@Column(columnDefinition="text")
	public String getTragetValue() {
		return tragetValue;
	}

	public void setTragetValue(String tragetValue) {
		this.tragetValue = tragetValue;
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
