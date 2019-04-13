package com.jettech.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.jettech.EnumConvertRule;

@Entity
@Table(name = "test_rule")
public class TestRule extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 34694707821379662L;

	private TestQuery testQuery;
	private DataField dataField;
	private EnumConvertRule rule;
	private String ruleValue;
	private Integer position;
	private String dateFormat;
	private CodeMap codeMap;


	
	
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
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

	public String getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	/**
	 * 返回需要的编码表
	 * 
	 * @return
	 */
	@Transient
	public Map<String, String> getConverCodeMap() {
		if (this.rule != EnumConvertRule.CodeMap) {
			return null;
		}
		Map<String, String> map = new HashMap<>();
		if (codeMap != null && codeMap.getItems() != null && codeMap.getItems().size() > 0) {
			for (CodeMapItem item : codeMap.getItems()) {
				map.put(item.getOrgValue(), item.getRefValue());
			}
		}
		return map;
	}

	public DataField getDataField() {
		return dataField;
	}

	public void setDataField(DataField dataField) {
		this.dataField = dataField;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "code_map_id")
	public CodeMap getCodeMap() {
		return codeMap;
	}

	public void setCodeMap(CodeMap codeMap) {
		this.codeMap = codeMap;
	}
	@ManyToOne
	@JoinColumn(name = "test_query_id")
	public TestQuery getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(TestQuery testQuery) {
		this.testQuery = testQuery;
	}

	// @OneToOne
	// @JoinColumn(name = "test_rule_id", nullable = true)
	// public TestMap getTestMap() {
	// return testMap;
	// }
	//
	// public void setTestMap(TestMap testMap) {
	// this.testMap = testMap;
	// }

}
