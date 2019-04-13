package com.jettech.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.ModelTestCase;
import com.jettech.entity.TestCase;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelTestCaseVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = -6640456296318609286L;

	private String name;
	private String expertValue; // 期望值
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
	private Integer sourceDataSourceID;// 源查询数据源ID
	private Integer targetDataSourceID;// 目标查询数据源ID
	private Integer pageSize = 0;
	private Boolean usePage = false;// 默认不分页
	private Integer testSuiteID;// 测试集ID
  
	private CaseModelSetVO caseModelSetVO;
	//案例模型集
	
	
	
	public ModelTestCaseVO() {
	}

	public ModelTestCaseVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			ModelTestCase e = (ModelTestCase) entity;
			if (e != null) {
				this.name = e.getName();
			}
		}
	}

	public Boolean getSQLCase() {
		return isSQLCase;
	}

	public void setSQLCase(Boolean SQLCase) {
		isSQLCase = SQLCase;
	}

	public ModelTestCaseVO(String name, String expertValue, String version, Boolean isSQLCase, Integer maxResultRows) {
		this();
		this.name = name;
		this.expertValue = expertValue;
		this.version = version;
		this.isSQLCase = isSQLCase;
		this.maxResultRows = maxResultRows;
	}
	public void setCaseModelSetVO(CaseModelSetVO caseModelSetVO) {
		this.caseModelSetVO = caseModelSetVO;
	}
    public CaseModelSetVO getCaseModelSetVO() {
		return caseModelSetVO;
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

}
