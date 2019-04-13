package com.jettech.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseModelSetVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer modelTestCaseId;
	// 基准模型集
	private Integer datumModelSetId;
	// 测试模型集
	
	private Integer testModelSetId;
    
	private List<CaseModelSetDetailsVO> detailsList;
	
	
	public Integer getTestModelSetId() {
		return testModelSetId;
	}

	public void setTestModelSetId(Integer testModelSetId) {
		this.testModelSetId = testModelSetId;
	}

	public List<CaseModelSetDetailsVO> getDetailsList() {
		return detailsList;
	}

	public void setDetailsList(List<CaseModelSetDetailsVO> detailsList) {
		this.detailsList = detailsList;
	}

	public Integer getModelTestCaseId() {
		return modelTestCaseId;
	}

	public void setModelTestCaseId(Integer modelTestCaseId) {
		this.modelTestCaseId = modelTestCaseId;
	}

	public Integer getDatumModelSetId() {
		return datumModelSetId;
	}

	public void setDatumModelSetId(Integer datumModelSetId) {
		this.datumModelSetId = datumModelSetId;
	}


}
