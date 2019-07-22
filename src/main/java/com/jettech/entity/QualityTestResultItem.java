package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="quality_test_result_item")
public class QualityTestResultItem extends BaseEntity{

	

	/**
	 * 
	 */
	private static final long serialVersionUID = 2915840692680172674L;

	private Integer testResultId;
	private String selectValue;
	private String result;
	private String columnName;
	private String idNumber;	//质量id值
    private int sign;   //取正反标记 0为反  1为正
	/*private String columnName;
	private String soruceValue;
	private String tragetValue;*/

	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn(name = "test_result_id")
	// public TestResult getTestReuslt() {
	// return testReuslt;
	// }
	//
	// public void setTestReuslt(TestResult testReuslt) {
	// this.testReuslt = testReuslt;
	// }

/*	public String getKeyValue() {
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

	public String getSoruceValue() {
		return soruceValue;
	}

	public void setSoruceValue(String soruceValue) {
		this.soruceValue = soruceValue;
	}

	public String getTragetValue() {
		return tragetValue;
	}

	public void setTragetValue(String tragetValue) {
		this.tragetValue = tragetValue;
	}*/

	public String getColumnName() {
		return columnName;
	}



	public void setColumnName(String columnName) {
		this.columnName = columnName;
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

	

	public QualityTestResultItem() {
		super();
	}



	public String getSelectValue() {
		return selectValue;
	}

	public void setSelectValue(String selectValue) {
		this.selectValue = selectValue;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public void setTestResultId(Integer testResultId) {
		this.testResultId = testResultId;
	}


	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }
}
