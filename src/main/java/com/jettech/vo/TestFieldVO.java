package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataField;

public class TestFieldVO extends BaseVO {

	private static final long serialVersionUID = -5619933935665195465L;

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

	private Integer testTableId;

	// private TestTable testTable;
	//
	// private List<TestQueryField> testQueryFields;

	private Boolean deleted;
	private Integer version;

	public TestFieldVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			DataField e = (DataField) entity;
			testTableId = e.getDataTable().getId();
		}
	}

	// @Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public TestFieldVO() {
	}

	public TestFieldVO(String name, String dataType) {
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

	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn(name = "test_table_id")
	// public TestTable getTestTable() {
	// return testTable;
	// }
	//
	// public void setTestTable(TestTable testTable) {
	// this.testTable = testTable;
	// }

	public String getTalbeName() {
		return talbeName;
	}

	public void setTalbeName(String talbeName) {
		this.talbeName = talbeName;
	}

	// @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	// @JoinColumn(name = "test_field_id", referencedColumnName = "id")
	// @JSONField(serialize = false)
	// public List<TestQueryField> getTestQueryFields() {
	// return testQueryFields;
	// }
	//
	// public void setTestQueryFields(List<TestQueryField> testQueryFields) {
	// this.testQueryFields = testQueryFields;
	// }

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getTestTableId() {
		return testTableId;
	}

	public void setTestTableId(Integer testTableId) {
		this.testTableId = testTableId;
	}

}