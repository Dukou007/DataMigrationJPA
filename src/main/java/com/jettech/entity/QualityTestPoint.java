package com.jettech.entity;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jettech.EnumConvertRule;

@Entity
/*@Table(name = "quality_test_point")*/
public class QualityTestPoint extends BaseEntity {
	/**
	 *
	 */
	private static final long serialVersionUID = -5538169784934719121L;

//	private QualityTestQuery qualityTestQuery;
	private DataField dataField;
	private QualityCodeMap qualityCodeMap;
	private EnumConvertRule ruleType;
	private String ruleValue;
	private Integer position;
	private String format;
	private QualitySuite qualitySuite;

//
//
//	@OneToOne(mappedBy="qualityTestPoint")
//	public QualityTestQuery getQualityTestQuery() {
//		return qualityTestQuery;
//	}
//
//	public void setQualityTestQuery(QualityTestQuery qualityTestQuery) {
//		this.qualityTestQuery = qualityTestQuery;
//	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_field_id")
	public DataField getDataField() {
		return dataField;
	}

	public void setDataField(DataField dataField) {
		this.dataField = dataField;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "quality_code_map_id")
	public QualityCodeMap getQualityCodeMap() {
		return qualityCodeMap;
	}

	public void setQualityCodeMap(QualityCodeMap qualityCodeMap) {
		this.qualityCodeMap = qualityCodeMap;
	}

	public EnumConvertRule getRuleType() {
		return ruleType;
	}

	public void setRuleType(EnumConvertRule ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}



	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "quality_suite_id")
	public QualitySuite getQualitySuite() {
		return qualitySuite;
	}

	public void setQualitySuite(QualitySuite qualitySuite) {
		this.qualitySuite = qualitySuite;
	}

}





/*
package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.jettech.EnumConvertRule;

@Entity
@Table(name="quality_test_point")
public class QualityTestPoint extends BaseEntity {
	*/
/**
	 *
	 *//*

	private static final long serialVersionUID = -5538169784934719121L;

	private TestQuery testQuery;
	private TestField testField;
	private CodeMap codeMap;
	private EnumConvertRule ruleType;
	private String ruleValue;
	private Integer position;
	private String format;
//	private QualitySuite qualitySuite;




	@OneToOne
	@JoinColumn(name="quality_test_query")
	public TestQuery getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(TestQuery testQuery) {
		this.testQuery = testQuery;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_field_id")
	public TestField getTestField() {
		return testField;
	}

	public void setTestField(TestField testField) {
		this.testField = testField;
	}

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "code_map_id")
	public CodeMap getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(CodeMap codeMap) {
		this.codeMap = codeMap;
	}

	public EnumConvertRule getRuleType() {
		return ruleType;
	}

	public void setRuleType(EnumConvertRule ruleType) {
		this.ruleType = ruleType;
	}

	public String getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}



	*/
/*@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "quality_suite_id")
	public QualitySuite getQualitySuite() {
		return qualitySuite;
	}

	public void setQualitySuite(QualitySuite qualitySuite) {
		this.qualitySuite = qualitySuite;
	}*//*


}
*/
