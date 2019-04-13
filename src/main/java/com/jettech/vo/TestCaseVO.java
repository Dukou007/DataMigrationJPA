package com.jettech.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.jettech.EnumTestCaseType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestCase;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCaseVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = -6640456296318609286L;

	private String name;
	private String expertValue; // 期望值
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	private String sourceDataSourceName;// 源查询数据源名称
	private String targetDataSourceName;// 目标查询数据源名称
	private Integer sourceDataSourceID;// 源查询数据源ID
	private Integer targetDataSourceID;// 目标查询数据源ID
	private Integer pageSize = 0;
	private Boolean usePage ;// 默认不分页
	private Integer testSuiteID;// 测试集ID
	private Integer sourceQueryID;// 原查询ID
	private Integer targetQueryID;// 目标查询ID
	private EnumTestCaseType caseType;

	

	private TestQueryVO sourceQuery; //源查询
    private TestQueryVO targetQuery; //目标查询

    public EnumTestCaseType getCaseType() {
    	return caseType;
    }
    
    
    public void setCaseType(EnumTestCaseType caseType) {
    	this.caseType = caseType;
    }
	public TestCaseVO() {
	}


	public TestCaseVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			TestCase e = (TestCase) entity;
			if (e != null) {
				this.name=e.getName();
				this.caseType=EnumTestCaseType.None;
				if (e.getSourceQuery() != null && e.getSourceQuery().getDataSource() != null) {
					this.sourceDataSourceName = e.getSourceQuery().getDataSource().getName();
					this.sourceDataSourceID=e.getSourceQuery().getDataSource().getId();
				} else {
					this.sourceDataSourceName = "null";
					this.sourceDataSourceID=null;
				}
				if (e.getTargetQuery() != null && e.getTargetQuery().getDataSource() != null) {
					this.targetDataSourceName = e.getTargetQuery().getDataSource().getName();
					this.targetDataSourceID=e.getTargetQuery().getDataSource().getId();
				} else {
					this.targetDataSourceName = "null";
					this.targetDataSourceID=null;
				}
//				if(e.getTestSuite()!=null) {
//					this.testSuiteID=e.getTestSuite().getId();
//				}else {
//					this.testSuiteID=null;
//				}
			}
		}
	}

	public Boolean getSQLCase() {
		return isSQLCase;
	}

	public void setSQLCase(Boolean SQLCase) {
		isSQLCase = SQLCase;
	}

	public TestCaseVO(String name, String expertValue, String version, Boolean isSQLCase, Integer maxResultRows) {
		this();
		this.name = name;
		this.expertValue = expertValue;
		this.version = version;
		this.isSQLCase = isSQLCase;
		this.maxResultRows = maxResultRows;
	}

	public TestQueryVO getSourceQuery() {
		return sourceQuery;
	}

	public void setSourceQuery(TestQueryVO sourceQuery) {
		this.sourceQuery = sourceQuery;
	}

	public TestQueryVO getTargetQuery() {
		return targetQuery;
	}

	public void setTargetQuery(TestQueryVO targetQuery) {
		this.targetQuery = targetQuery;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpertValue() {
		return expertValue;
	}

	public void setExpertValue(String expertValue) {
		this.expertValue = expertValue;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Boolean getIsSQLCase() {
		return isSQLCase;
	}

	public void setIsSQLCase(Boolean isSQLCase) {
		this.isSQLCase = isSQLCase;
	}

	public Integer getMaxResultRows() {
		return maxResultRows;
	}

	public void setMaxResultRows(Integer maxResultRows) {
		this.maxResultRows = maxResultRows;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	public Integer getTestSuiteID() {
		return testSuiteID;
	}

	public void setTestSuiteID(Integer testSuiteID) {
		this.testSuiteID = testSuiteID;
	}

	public Integer getSourceQueryID() {
		return sourceQueryID;
	}

	public void setSourceQueryID(Integer sourceQueryID) {
		this.sourceQueryID = sourceQueryID;
	}

	public Integer getTargetQueryID() {
		return targetQueryID;
	}

	public void setTargetQueryID(Integer targetQueryID) {
		this.targetQueryID = targetQueryID;
	}
	public String getSourceDataSourceName() {
		return sourceDataSourceName;
	}

	public void setSourceDataSourceName(String sourceDataSourceName) {
		this.sourceDataSourceName = sourceDataSourceName;
	}

	public String getTargetDataSourceName() {
		return targetDataSourceName;
	}

	public Integer getSourceDataSourceID() {
		return sourceDataSourceID;
	}

	public void setSourceDataSourceID(Integer sourceDataSourceID) {
		this.sourceDataSourceID = sourceDataSourceID;
	}

	public Integer getTargetDataSourceID() {
		return targetDataSourceID;
	}

	public void setTargetDataSourceID(Integer targetDataSourceID) {
		this.targetDataSourceID = targetDataSourceID;
	}

	public void setTargetDataSourceName(String targetDataSourceName) {
		this.targetDataSourceName = targetDataSourceName;
	}

}
