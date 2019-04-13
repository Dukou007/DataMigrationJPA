/**
 * 
 */
package com.jettech.vo;

/**
 *  @author Eason007
 *	@Description: TestRuleVO
 *  @date: 2019年2月2日 上午9:55:27 
 */
public class TestRuleVO extends BaseVO{

	/**
	 * long serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String ruleValue;
	private Integer position;
	
	private String dateFormat;
	private Integer rule;
	private Integer codeMapId;
    
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public Integer getRule() {
		return rule;
	}
	public void setRule(Integer rule) {
		this.rule = rule;
	}
	public Integer getCodeMapId() {
		return codeMapId;
	}
	public void setCodeMapId(Integer codeMapId) {
		this.codeMapId = codeMapId;
	}
	public String getRuleValue() {
		return ruleValue;
	}
	public Integer getPosition() {
		return position;
	}
	public void setRuleValue(String ruleValue) {
		this.ruleValue = ruleValue;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	
	

}
