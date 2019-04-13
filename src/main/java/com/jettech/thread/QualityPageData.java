package com.jettech.thread;

import java.util.List;
import java.util.Map;

import com.jettech.domain.QueryModel;
import com.jettech.entity.TestQuery;

/**
 * 数据页,存储一个表的全量数据或者部分数据
 * 
 * @author tan
 *
 */
public class QualityPageData extends QualityBaseData {
	private String minKey;
	private String maxKey;
	private Integer pageIndex;

	public String getMinKey() {
		return minKey;
	}

	public void setMinKey(String minKey) {
		this.minKey = minKey;
	}

	public String getMaxKey() {
		return maxKey;
	}

	public void setMaxKey(String maxKey) {
		this.maxKey = maxKey;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

}
