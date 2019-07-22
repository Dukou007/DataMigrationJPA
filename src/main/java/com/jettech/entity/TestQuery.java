package com.jettech.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 查询的实体,包含SQL,关键字段
 * 
 * @author tan
 *
 */
@Entity
@Table(name = "test_query")
public class TestQuery extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -684777565742109484L;
	private String name;
	private String sqlText;
	private String selectText;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private DataSource dataSource;
	
	
//	private TestCase testCase;
	private List<TestQueryField> testFields;
	private List<TestQueryField> keyFields;
	private List<TestQueryField> pageFields;
	private List<TestRule> testRules;
	private String keyText;
	private String pageText;
	private String sqlIntro; //sql注释

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getMaxNullKeyCount() {
		return maxNullKeyCount;
	}

	public void setMaxNullKeyCount(Integer maxNullKeyCount) {
		this.maxNullKeyCount = maxNullKeyCount;
	}

	public Integer getMaxDuplicatedKeyCount() {
		return maxDuplicatedKeyCount;
	}
    public void setSelectText(String selectText) {
		this.selectText = selectText;
	}
	public void setMaxDuplicatedKeyCount(Integer maxDuplicatedKeyCount) {
		this.maxDuplicatedKeyCount = maxDuplicatedKeyCount;
	}
   
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "data_source_id")
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public String getSelectText() {
		return selectText;
	}

	@Column(columnDefinition="text")
	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

//	@OneToOne(fetch = FetchType.EAGER)
//	@JoinColumn(name = "test_case_id")
//	public TestCase getTestCase() {
//		return testCase;
//	}
//
//	public void setTestCase(TestCase testCase) {
//		this.testCase = testCase;
//	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "select_query_id", referencedColumnName = "id")
	public List<TestQueryField> getTestFields() {
		return testFields;
	}

	public void setTestFields(List<TestQueryField> testFields) {
		this.testFields = testFields;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "key_query_id", referencedColumnName = "id")
	public List<TestQueryField> getKeyFields() {
		return keyFields;
	}

	public void setKeyFields(List<TestQueryField> keyFields) {
		this.keyFields = keyFields;
	}

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "test_query_id", referencedColumnName = "id")
	public List<TestRule> getTestRules() {
		return testRules;
	}

	public void setTestRules(List<TestRule> testRules) {
		this.testRules = testRules;
	}

	/**
	 * key转换的Text,多个Key之间使用逗号[,]分割
	 * 
	 * @return
	 */
	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "page_query_id", referencedColumnName = "id")
	public List<TestQueryField> getPageFields() {
		return pageFields;
	}

	public void setPageFields(List<TestQueryField> pageFields) {
		this.pageFields = pageFields;
	}

	public String getPageText() {
		return pageText;
	}

	public void setPageText(String pageText) {
		this.pageText = pageText;
	}

	public String getSqlIntro() {
		return sqlIntro;
	}

	public void setSqlIntro(String sqlIntro) {
		this.sqlIntro = sqlIntro;
	}

}
