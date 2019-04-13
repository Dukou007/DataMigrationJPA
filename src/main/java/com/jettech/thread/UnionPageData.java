package com.jettech.thread;

import java.util.List;
import java.util.Map;

import com.jettech.domain.QueryModel;

/**
 * 同时存储源和目标的分页数据
 * 
 * @author tan
 *
 */
public class UnionPageData extends PageData {

	private Map<String, List<Object>> mapTarget;
	private QueryModel queryTarget;
	
	/**
	 * 目标数据页的对象
	 * 
	 * @return
	 */
	public Map<String, List<Object>> getMapTarget() {
		return mapTarget;
	}

	public void setMapTarget(Map<String, List<Object>> mapTarget) {
		this.mapTarget = mapTarget;
	}

	public QueryModel getQueryTarget() {
		return queryTarget;
	}

	public void setQueryTarget(QueryModel queryTarget) {
		this.queryTarget = queryTarget;
	}
}
