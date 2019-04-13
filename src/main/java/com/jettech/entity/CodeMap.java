package com.jettech.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.alibaba.fastjson.annotation.JSONField;

@Entity
@Table(name = "code_map")
public class CodeMap extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6230519510814815417L;

	private String name;
	private List<CodeMapItem> items ;
	private List<TestRule> testRules;
//	private List<TestMap> testMaps;

//	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@JoinColumn(name = "code_map_id", referencedColumnName = "id")
//	@JSONField(serialize = false)
//	public List<TestMap> getTestMaps() {
//		return testMaps;
//	}
//
//	public void setTestMaps(List<TestMap> testMaps) {
//		this.testMaps = testMaps;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "code_map_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<CodeMapItem> getItems() {
		return items;
	}

	public void setItems(List<CodeMapItem> items) {
		this.items = items;
	}
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "code_map_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<TestRule> getTestRules() {
		return testRules;
	}

	public void setTestRules(List<TestRule> testRules) {
		this.testRules = testRules;
	}

}
