package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "test_map")
public class TestMap extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6230519510814815417L;

//	private CodeMap codeMap;
//	private TestRule testRule;
//
//	@JoinColumn(name = "test_rule_id", nullable = false)
//	@OneToOne
//	public TestRule getTestRule() {
//		return testRule;
//	}
//
//	public void setTestRule(TestRule testRule) {
//		this.testRule = testRule;
//	}
//
//	@OneToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "test_map_id", nullable = false)
//	public CodeMap getCodeMap() {
//		return codeMap;
//	}
//
//	public void setCodeMap(CodeMap codeMap) {
//		this.codeMap = codeMap;
//	}
}
