package com.jettech.thread;

import java.util.List;
import java.util.Map;

import com.jettech.domain.QueryModel;

abstract public class BaseData {
	private Map<String, List<Object>> map;

	public Map<String, List<Object>> getMap() {
		return map;
	}

	public void setMap(Map<String, List<Object>> map) {
		this.map = map;
	}

	private QueryModel testQuery;
	private Integer testQueryId;

	public Integer getTestQueryId() {
		return testQueryId;
	}

	public void setTestQueryId(Integer testQueryId) {
		this.testQueryId = testQueryId;
	}

	public QueryModel getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(QueryModel testQuery) {
		this.testQuery = testQuery;
	}
}
