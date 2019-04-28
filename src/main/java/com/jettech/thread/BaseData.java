package com.jettech.thread;

import java.util.List;
import java.util.Map;

import com.jettech.domain.QueryFileModel;
import com.jettech.domain.QueryModel;

abstract public class BaseData {
	private Map<String, List<Object>> map;

	public Map<String, List<Object>> getMap() {
		return map;
	}

	public void setMap(Map<String, List<Object>> map) {
		this.map = map;
	}

	private QueryFileModel testFileQuery;
	private Integer testFileQueryId;

	public QueryFileModel getTestFileQuery() {
		return testFileQuery;
	}

	public void setTestFileQuery(QueryFileModel testFileQuery) {
		this.testFileQuery = testFileQuery;
	}

	public Integer getTestFileQueryId() {
		return testFileQueryId;
	}

	public void setTestFileQueryId(Integer testFileQueryId) {
		this.testFileQueryId = testFileQueryId;
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
