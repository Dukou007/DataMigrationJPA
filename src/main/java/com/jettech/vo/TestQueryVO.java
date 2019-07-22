package com.jettech.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestQuery;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestQueryVO extends BaseVO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String sqlText;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private Integer testCaseId;
	private String dataSouceName;
    
	private String selectText;
		
	private Integer dataSourceId;
	
	private List<TestRuleVO> testRules;

	private String keyText;
	private String pageText;
	private String sqlIntro; //sql注释

	public TestQueryVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestQuery e = (TestQuery) entity;
		}
	}

	public TestQueryVO() {
	}
    public void setSelectText(String selectText) {
		this.selectText = selectText;
	}
    public String getSelectText() {
		return selectText;
	}
	public Integer getDataSourceId() {
		return dataSourceId;
	}

	public void setDataSourceId(Integer dataSourceId) {
		this.dataSourceId = dataSourceId;
	}

	public List<TestRuleVO> getTestRules() {
		return testRules;
	}

	public void setTestRules(List<TestRuleVO> testRules) {
		this.testRules = testRules;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
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

	public void setMaxDuplicatedKeyCount(Integer maxDuplicatedKeyCount) {
		this.maxDuplicatedKeyCount = maxDuplicatedKeyCount;
	}

	// public DataSource getDataSource() {
	// return dataSource;
	// }
	// public void setDataSource(DataSource dataSource) {
	// this.dataSource = dataSource;
	// }
	// public TestCase getTestCase() {
	// return testCase;
	// }
	// public void setTestCase(TestCase testCase) {
	// this.testCase = testCase;
	// }
	// public List<TestQueryField> getTestFields() {
	// return testFields;
	// }
	// public void setTestFields(List<TestQueryField> testFields) {
	// this.testFields = testFields;
	// }
	// public List<TestQueryField> getKeyFields() {
	// return keyFields;
	// }
	// public void setKeyFields(List<TestQueryField> keyFields) {
	// this.keyFields = keyFields;
	// }
	// public List<TestQueryField> getPageFields() {
	// return pageFields;
	// }
	// public void setPageFields(List<TestQueryField> pageFields) {
	// this.pageFields = pageFields;
	// }
	// public List<TestRule> getTestRules() {
	// return testRules;
	// }
	// public void setTestRules(List<TestRule> testRules) {
	// this.testRules = testRules;
	// }
	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

	public String getPageText() {
		return pageText;
	}

	public void setPageText(String pageText) {
		this.pageText = pageText;
	}

	/**
	 * @return the testCaseId
	 */
	public Integer getTestCaseId() {
		return testCaseId;
	}

	/**
	 * @param testCaseId the testCaseId to set
	 */
	public void setTestCaseId(Integer testCaseId) {
		this.testCaseId = testCaseId;
	}

	/**
	 * @return the dataSouceName
	 */
	public String getDataSouceName() {
		return dataSouceName;
	}

	/**
	 * @param dataSouceName the dataSouceName to set
	 */
	public void setDataSouceName(String dataSouceName) {
		this.dataSouceName = dataSouceName;
	}

	public String getSqlIntro() {
		return sqlIntro;
	}

	public void setSqlIntro(String sqlIntro) {
		this.sqlIntro = sqlIntro;
	}

}
