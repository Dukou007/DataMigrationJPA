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
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import com.alibaba.fastjson.annotation.JSONField;

@Entity
@Table(name = "test_field", uniqueConstraints = {
		@UniqueConstraint(name = "ix_test_field_name", columnNames = { "name", "test_table_id" }) })
public class DataField extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8715986279017808804L;

	private String talbeName;

	private String name;

	private String des;

	private String dataType;

	private Integer dataLength;

	private Integer dataPrecision;

	private Boolean isNullable;

	private Boolean isPrimaryKey;

	private Boolean isForeignKey;

	private Boolean isIndex;

	private DataTable dataTable;

	private QualityTestQuery qualityTestQuery;

	private Boolean deleted;
	private Integer version;
	/*
	 * @ManyToOne
	 * 
	 * @JoinColumn(name="quality_test_case_id") public QualityTestCase
	 * getQualityTestCase() { return qualityTestCase; }
	 * 
	 * public void setQualityTestCase(QualityTestCase qualityTestCase) {
	 * this.qualityTestCase = qualityTestCase; }
	 */

	private List<TestQueryField> testQueryFields;
	private List<TestFileQueryField> testFileQueryField;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "test_field_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<TestFileQueryField> getTestFileQueryField() {
		return testFileQueryField;
	}

	public void setTestFileQueryField(List<TestFileQueryField> testFileQueryField) {
		this.testFileQueryField = testFileQueryField;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public DataField() {
	}

	public DataField(String name, String dataType) {
		this();
		this.name = name;
		this.dataType = dataType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	@Column(columnDefinition="text")
	public String getDes() {
		return des;
	}

	public void setDes(String des) {
		this.des = des == null ? null : des.trim();
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType == null ? null : dataType.trim();
	}

	public Integer getDataLength() {
		return dataLength;
	}

	public void setDataLength(Integer dataLength) {
		this.dataLength = dataLength;
	}

	public Integer getDataPrecision() {
		return dataPrecision;
	}

	public void setDataPrecision(Integer dataPrecision) {
		this.dataPrecision = dataPrecision;
	}

	public Boolean getIsNullable() {
		return isNullable;
	}

	public void setIsNullable(Boolean isNullable) {
		this.isNullable = isNullable;
	}

	public Boolean getIsPrimaryKey() {
		return isPrimaryKey;
	}

	public void setIsPrimaryKey(Boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public Boolean getIsForeignKey() {
		return isForeignKey;
	}

	public void setIsForeignKey(Boolean isForeignKey) {
		this.isForeignKey = isForeignKey;
	}

	public Boolean getIsIndex() {
		return isIndex;
	}

	public void setIsIndex(Boolean isIndex) {
		this.isIndex = isIndex;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_table_id")
	public DataTable getDataTable() {
		return dataTable;
	}

	public void setDataTable(DataTable dataTable) {
		this.dataTable = dataTable;
	}

	public String getTalbeName() {
		return talbeName;
	}

	public void setTalbeName(String talbeName) {
		this.talbeName = talbeName;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "test_field_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<TestQueryField> getTestQueryFields() {
		return testQueryFields;
	}

	public void setTestQueryFields(List<TestQueryField> testQueryFields) {
		this.testQueryFields = testQueryFields;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	@ManyToOne
	// @JoinColumn(name="test_field_id") Quality
	@JoinColumn(name = "quality_test_query_id")
	public QualityTestQuery getQualityTestQuery() {
		return qualityTestQuery;
	}

	public void setQualityTestQuery(QualityTestQuery qualityTestQuery) {
		this.qualityTestQuery = qualityTestQuery;
	}

	/*
	 * public QualityTestQuery getQualityTestQuery() { return qualityTestQuery; }
	 * 
	 * public void setQualityTestQuery(QualityTestQuery qualityTestQuery) {
	 * this.qualityTestQuery = qualityTestQuery; }
	 */

}