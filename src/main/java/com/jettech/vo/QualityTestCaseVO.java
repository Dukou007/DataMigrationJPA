package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestCase;

//@JsonInclude(JsonInclude.Include.NON_NULL)
public class QualityTestCaseVO extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = -6640456296318609286L;

	private String name;
	private String version; // 版本
	private Boolean isSQLCase;
	private Integer maxResultRows;
//	private Integer pageSize = 0;
	private Boolean usePage = false;// 默认不分页
	private QualityTestQueryVO qualityTestQueryVo;
    private Integer testSuiteId;
	public QualityTestQueryVO getQualityTestQueryVo() {
		return qualityTestQueryVo;
	}

	public void setQualityTestQueryVo(QualityTestQueryVO qualityTestQueryVo) {
		this.qualityTestQueryVo = qualityTestQueryVo;
	}

	public QualityTestCaseVO(BaseEntity entity) {
		if (entity != null) {
			QualityTestCase testCase = (QualityTestCase) entity;
			if (testCase != null) {
				this.name = testCase.getName();
				this.isSQLCase = testCase.getIsSQLCase();
				this.maxResultRows = testCase.getMaxResultRows();
//				this.pageSize = testCase.getPageSize();
				this.usePage = testCase.getUsePage();
				this.version = testCase.getVersion();
				this.setId(testCase.getId());
				this.setCreateTime(testCase.getCreateTime());
				this.setCreateUser(testCase.getCreateUser());
				this.setEditTime(testCase.getEditTime());
				this.setEditUser(testCase.getEditUser());
				if(testCase.getQualityTestQuery()!=null) {
					this.qualityTestQueryVo=new QualityTestQueryVO(testCase.getQualityTestQuery());
				}
			}
		}

	}

	public QualityTestCaseVO() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

//	public Integer getPageSize() {
//		return pageSize;
//	}
//
//	public void setPageSize(Integer pageSize) {
//		this.pageSize = pageSize;
//	}

	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}

	public Integer getTestSuiteId() {
		return testSuiteId;
	}

	public void setTestSuiteId(Integer testSuiteId) {
		this.testSuiteId = testSuiteId;
	}

}
