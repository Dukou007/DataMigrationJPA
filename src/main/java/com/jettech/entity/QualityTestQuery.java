package com.jettech.entity;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="quality_test_query")
public class QualityTestQuery extends BaseEntity {

	public QualityTestQuery() {
		super();
	}

	private static final long serialVersionUID = -684777565742109484L;
	private String name;
	private String sqlText;
	private DataSource dataSource;
	private QualityTestCase qualityTestCase;
	private List<DataField> dataFields;
	private QualityTestPoint qualityTestPoint;
	
/*	@OneToOne
	@JoinColumn(name="quality_test_point_id")
	public QualityTestPoint getQualityTestPoint() {
		return qualityTestPoint;
	}
	public void setQualityTestPoint(QualityTestPoint qualityTestPoint) {
		this.qualityTestPoint = qualityTestPoint;
	}*/
	@OneToOne(mappedBy = "qualityTestQuery")
	public QualityTestCase getQualityTestCase() {
		return qualityTestCase;
	}
	@OneToMany(mappedBy = "qualityTestQuery")
	public List<DataField> getDataFields() {
		return dataFields;
	}

	public void setDataFields(List<DataField> dataFields) {
		this.dataFields = dataFields;
	}

	public void setQualityTestCase(QualityTestCase qualityTestCase) {
		this.qualityTestCase = qualityTestCase;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "data_source_id")
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	
	
	
	
}
