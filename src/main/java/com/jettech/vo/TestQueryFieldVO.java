package com.jettech.vo;

import com.jettech.EnumFieldType;

public class TestQueryFieldVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2553421847797498540L;

	private String fieldType = EnumFieldType.TestField.name();// 默认为字段
	private String expression;
	private Integer testFieldId;
}
