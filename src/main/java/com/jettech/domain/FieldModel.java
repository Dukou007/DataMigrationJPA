package com.jettech.domain;

import com.jettech.EnumFieldType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestQueryField;

public class FieldModel extends BaseModel {

	private EnumFieldType fieldType;
	private String expression;

	private String talbeName;

	private String name;

	private String des;

	private String dataType;

	private Integer dataLength;

	private Integer dataPrecision;

	private Boolean isNullable;

	private Boolean isPrimaryKey;

	private Boolean isForeignKey;

	private Boolean isIndex;

	public FieldModel(String name) {
		this.name = name;
	}

	public FieldModel(DataField col) {
		this.setTalbeName(col.getTableName());
		this.setName(col.getColumnName());
		this.setDes(col.getLabel());
		this.setDataType(col.getDataType());
		this.setDataLength(col.getDataLength());
		this.setDataPrecision(col.getScale());// 精度?
		// field.setDataPrecision(col);
	}

	public FieldModel(com.jettech.entity.DataField testFiled)
	{
//		this.setTalbeName(testFiled.g);
		this.setName(testFiled.getName());
		this.setDes(testFiled.getDes());
		this.setDataType(testFiled.getDataType());
		this.setDataLength(testFiled.getDataLength());
		this.setDataPrecision(testFiled.getDataPrecision());// 精度?
	}
	
	public FieldModel() {
	}

	public FieldModel(TestQueryField queryField) throws Exception {
		this();
		// super.parse(queryField);
		parseEntity(queryField);
		// this.fieldType = queryField.getFieldType();
		// if (queryField.getTestField() != null) {
		// // super.parse(queryField.getTestField());
		// this.talbeName = queryField.getTestField().getTalbeName();
		// this.name = queryField.getTestField().getName();
		// this.des = queryField.getTestField().getDes();
		// this.dataType = queryField.getTestField().getDataType();
		// this.dataLength = queryField.getTestField().getDataLength();
		// this.dataPrecision = queryField.getTestField().getDataPrecision();
		// this.isNullable = queryField.getTestField().getIsNullable();
		// this.isPrimaryKey = queryField.getTestField().getIsPrimaryKey();
		// this.isForeignKey = queryField.getTestField().getIsForeignKey();
		// this.isIndex = queryField.getTestField().getIsIndex();
		// }
		// System.out.println("fileName:"+name);
	}

	@Override
	public void parseEntity(BaseEntity entity) {
		super.parseEntity(entity);
		TestQueryField testQueryField = (TestQueryField) entity;
		this.setName(testQueryField.getName());
		this.setExpression(testQueryField.getExpression());
		this.setFieldType(testQueryField.getFieldType());
		com.jettech.entity.DataField testField = testQueryField.getDataField();
		if (testField != null) {
			if (this.name == null || this.getName().trim().isEmpty())
				this.setName(testField.getName());
			this.setTalbeName(testField.getTalbeName());
			this.setDataLength(testField.getDataLength());
			this.setDataPrecision(testField.getDataPrecision());
			this.setDataType(testField.getDataType());
			this.setDes(testField.getDes());
			this.setIsForeignKey(testField.getIsForeignKey());
			this.setIsIndex(testField.getIsIndex());
			this.setIsNullable(testField.getIsNullable());
			this.setIsPrimaryKey(testField.getIsPrimaryKey());
		}
	}

	public EnumFieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(EnumFieldType fieldType) {
		this.fieldType = fieldType;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getTalbeName() {
		return talbeName;
	}

	public void setTalbeName(String talbeName) {
		this.talbeName = talbeName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Integer getDataLength() {
		return dataLength;
	}

	public void setDataLength(Integer dataLength) {
		this.dataLength = dataLength;
	}

	public Integer getDataPrecision() {
		return dataPrecision;
	}

	public void setDataPrecision(Integer dataPrecision) {
		this.dataPrecision = dataPrecision;
	}

	public Boolean getIsNullable() {
		return isNullable;
	}

	public void setIsNullable(Boolean isNullable) {
		this.isNullable = isNullable;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public Boolean getIsForeignKey() {
		return isForeignKey;
	}

	public void setIsForeignKey(Boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}

	public Boolean getIsIndex() {
		return isIndex;
	}

	public void setIsIndex(Boolean isIndex) {
		this.isIndex = isIndex;
	}
}
