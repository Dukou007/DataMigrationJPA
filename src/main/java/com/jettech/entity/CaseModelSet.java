package com.jettech.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "case_model_set")
public class CaseModelSet extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ModelTestCase modelTestCase;
	// 基准模型集
	private DataSchema datumModelSet;
	// 测试模型集
	private DataSchema dataSchema;

	private List<CaseModelSetDetails> details = new ArrayList<>();

	public void setDataSchema(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}

	public void setModelTestCase(ModelTestCase modelTestCase) {
		this.modelTestCase = modelTestCase;
	}

	public void setDatumModelSet(DataSchema datumModelSet) {
		this.datumModelSet = datumModelSet;
	}

	@OneToOne
	@JoinColumn(name = "datum_model_set_id")
	public DataSchema getDatumModelSet() {
		return datumModelSet;
	}

	@OneToOne
	@JoinColumn(name = "test_model_set_id")
	public DataSchema getDataSchema() {
		return dataSchema;
	}

	@OneToOne
	@JoinColumn(name = "test_case_id")
	public ModelTestCase getModelTestCase() {
		return modelTestCase;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "case_model_set_id", referencedColumnName = "id")
	public List<CaseModelSetDetails> getDetails() {
		return details;
	}

	public void setDetails(List<CaseModelSetDetails> details) {
		this.details = details;
	}

}
