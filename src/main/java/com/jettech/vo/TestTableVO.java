package com.jettech.vo;

import com.jettech.entity.BaseEntity;
import com.jettech.entity.DataTable;

public class TestTableVO extends BaseVO {

	private static final long serialVersionUID = 5181972457220823562L;

	private String name;

	private String des;

	private Boolean isView;

	// private TestDatabase testDatabase;
	//
	// private List<TestField> testFileds = new ArrayList<>();

	private Boolean deleted;

	private Boolean isDict;
	
	private Integer version;

	public TestTableVO() {
		super();
	}

	private Integer testDatabaseId;

	public TestTableVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			DataTable e = (DataTable) entity;
			testDatabaseId = e.getDataSchema().getId();
		}
	}

	// @Version
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
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

	public Boolean getIsView() {
		return isView;
	}

	public void setIsView(Boolean isView) {
		this.isView = isView;
	}
	//
	// @ManyToOne(fetch = FetchType.EAGER)
	// @JoinColumn(name = "test_database_id")
	// public TestDatabase getTestDatabase() {
	// return testDatabase;
	// }
	//
	// public void setTestDatabase(TestDatabase testDatabase) {
	// this.testDatabase = testDatabase;
	// }
	//
	// @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	// @JoinColumn(name = "test_table_id", referencedColumnName = "id")
	// @JSONField(serialize = false)
	// public List<TestField> getTestFileds() {
	// return testFileds;
	// }
	//
	// public void setTestFileds(List<TestField> testFileds) {
	// this.testFileds = testFileds;
	// }

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Integer getTestDatabaseId() {
		return testDatabaseId;
	}

	public void setTestDatabaseId(Integer testDatabaseId) {
		this.testDatabaseId = testDatabaseId;
	}
	public Boolean getIsDict() {
		return isDict;
	}

	public void setIsDict(Boolean isDict) {
		this.isDict = isDict;
	}
}