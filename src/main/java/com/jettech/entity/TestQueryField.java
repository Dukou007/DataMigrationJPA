package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.jettech.EnumFieldType;

/**
 * 描述查询中用的字段,函数或者表达式(暂时只有字段和表达式类型)
 * 
 * @author tan
 *
 */
@Entity
@Table(name = "test_query_field")
public class TestQueryField extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2412621664197252891L;

	private DataField dataField;
	private EnumFieldType fieldType = EnumFieldType.TestField;// 默认为字段
	private String expression;
	private TestQuery selectQuery;// 作为查询字段的对应的Query
	private TestQuery keyQuery;// 作为Key字段对应的Query
	private TestQuery pageQuery;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_field_id")
	public DataField getDataField() {
		return dataField;
	}

	public void setDataField(DataField dataField) {
		this.dataField = dataField;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public EnumFieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(EnumFieldType fieldType) {
		this.fieldType = fieldType;
	}

	// @ManyToOne(fetch = FetchType.LAZY)
	// @JoinColumn(name = "test_query_id")
	// public TestQuery getTestQuery() {
	// return testQuery;
	// }
	//
	// public void setTestQuery(TestQuery testQuery) {
	// this.testQuery = testQuery;
	// }
	//
	@Transient
	public String getDataType() {
		if (this.fieldType == EnumFieldType.TestField)
			return dataField.getDataType();
		return null;
	}

	@Transient
	public String getName() {
		if (this.fieldType == EnumFieldType.TestField)
			return dataField.getName();
		return this.expression;
	}

	@Transient
	public Integer getDataLength() {
		if (this.fieldType == EnumFieldType.TestField && dataField != null)
			return dataField.getDataLength();
		return null;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "select_query_id")
	public TestQuery getSelectQuery() {
		return selectQuery;
	}

	public void setSelectQuery(TestQuery selectQuery) {
		this.selectQuery = selectQuery;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key_query_id")
	public TestQuery getKeyQuery() {
		return keyQuery;
	}

	public void setKeyQuery(TestQuery keyQuery) {
		this.keyQuery = keyQuery;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "page_query_id")
	public TestQuery getPageQuery() {
		return pageQuery;
	}

	public void setPageQuery(TestQuery pageQuery) {
		this.pageQuery = pageQuery;
	}

}
