package com.jettech.thread;

import java.util.List;
import java.util.Map;

import com.jettech.domain.QualityQueryModel;
import com.jettech.domain.QueryModel;

abstract public class QualityBaseData {
	private Map<String, List<Object>> map;

	public Map<String, List<Object>> getMap() {
		return map;
	}

	public void setMap(Map<String, List<Object>> map) {
		this.map = map;
	}

	private QualityQueryModel testQuery;
	private Integer qualityTestQueryId;

	public Integer getTestQueryId() {
		return qualityTestQueryId;
	}

	public void setTestQueryId(Integer qualityTestQueryId) {
		this.qualityTestQueryId = qualityTestQueryId;
	}

	public QualityQueryModel getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(QualityQueryModel testQuery) {
		this.testQuery = testQuery;
	}
}
