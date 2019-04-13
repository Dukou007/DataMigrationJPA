package com.jettech.domain;

import java.util.HashMap;
import java.util.Map;

import com.jettech.EnumConvertRule;
import com.jettech.entity.TestRule;

public class RuleModel extends BaseModel {
	private EnumConvertRule rule;
	private String ruleValue;
	private int position;
	private String dateFormat;
	private Map<String, String> codeMap = new HashMap<>();

	public RuleModel(TestRule entity) throws Exception {
		super.parse(entity);
		codeMap = entity.getConverCodeMap();
	}

	public EnumConvertRule getRule() {
		return rule;
	}

	public void setRule(EnumConvertRule rule) {
		this.rule = rule;
	}

	public String getRuleValue() {
		return ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * 当规则为CodeMap时有效，否则为null
	 * 
	 * @return
	 */
	public Map<String, String> getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(Map<String, String> codeMap) {
		this.codeMap = codeMap;
	}
}
