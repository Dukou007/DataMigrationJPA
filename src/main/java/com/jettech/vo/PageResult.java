package com.jettech.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果的封装，该类封装的数据要和前台约定好
 * 
 * @author Administrator
 * @param <E>
 */
public class PageResult<E> implements Serializable {

	private long total;// 总记录数
	private List<E> rows;// 带有分页的结果集

	public PageResult() {
	}

	public PageResult(long total, List<E> rows) {
		this.total = total;
		this.rows = rows;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<E> getRows() {
		return rows;
	}

	public void setRows(List<E> rows) {
		this.rows = rows;
	}
}
