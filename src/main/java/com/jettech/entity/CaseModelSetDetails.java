package com.jettech.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "case_model_set_details")
public class CaseModelSetDetails extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 案例模型集
	private CaseModelSet caseModelSet;

	private DataTable datumModelSetTable;
	
	private DataTable testModelSetTable;
 
	public void setDatumModelSetTable(DataTable datumModelSetTable) {
		this.datumModelSetTable = datumModelSetTable;
	}
	public void setTestModelSetTable(DataTable testModelSetTable) {
		this.testModelSetTable = testModelSetTable;
	}
	public void setCaseModelSet(CaseModelSet caseModelSet) {
		this.caseModelSet = caseModelSet;
	}
	
	@OneToOne
	@JoinColumn(name = "datum_model_table_id")
	public DataTable getDatumModelSetTable() {
		return datumModelSetTable;
	}
	@OneToOne
	@JoinColumn(name = "test_model_table_id")
	public DataTable getTestModelSetTable() {
		return testModelSetTable;
	}
	@ManyToOne
	@JoinColumn(name = "case_model_set_id")
	public CaseModelSet getCaseModelSet() {
		return caseModelSet;
	}

}
