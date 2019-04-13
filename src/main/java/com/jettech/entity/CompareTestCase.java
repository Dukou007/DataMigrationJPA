package com.jettech.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * 数据比较案例
 * 
 * @author tan
 *
 */
@Entity
@Table(name = "compare_test_case")
public class CompareTestCase extends TestCase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -322207843896378526L;

	private TestQuery sourceQuery;// 源查询
	private TestQuery targetQuery;// 目标查询

	@OneToOne(cascade = CascadeType.ALL, optional = true)
	@JoinColumn(name = "source_query_id")
	public TestQuery getSourceQuery() {
		return sourceQuery;
	}

	public void setSourceQuery(TestQuery sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	@OneToOne(cascade = CascadeType.ALL, optional = true)
	@JoinColumn(name = "target_query_id")
	public TestQuery getTargetQuery() {
		return targetQuery;
	}

	public void setTargetQuery(TestQuery targetQuery) {
		this.targetQuery = targetQuery;
	}
}
