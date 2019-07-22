package com.jettech.entity;


import javax.persistence.*;

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
	private DataSchema dataSchema;
	private DataTable dataTable;
	private DataField dataField;
	private QualityTestCase qualityTestCase;
	private QualitySuite qualitySuite;
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
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_field_id")
	public DataField getDataField() {
		return dataField;
	}

	public void setDataField(DataField dataField) {
		this.dataField = dataField;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_database_id")
	public DataSchema getDataSchema() {
		return dataSchema;
	}

	public void setDataSchema(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_table_id")
	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
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
	@JoinColumn(name = "quality_suite_id")
	public QualitySuite getQualitySuite() {
		return qualitySuite;
	}

	public void setQualitySuite(QualitySuite qualitySuite) {
		this.qualitySuite = qualitySuite;
	}
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "data_source_id")
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Column(columnDefinition = "text")
	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	
	
	
	
}
