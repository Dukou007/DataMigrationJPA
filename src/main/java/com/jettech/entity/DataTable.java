package com.jettech.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.alibaba.fastjson.annotation.JSONField;
import com.jettech.EnumDataModelType;

@Entity
@Table(name = "test_table", uniqueConstraints = {
        @UniqueConstraint(name = "ix_test_table_name", columnNames = { "name", "test_database_id" }) })
public class DataTable extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2600012358611396985L;

	private String name;

	private String des;

	private Boolean isView;
	// 是否是数据字典
	private Boolean isDict;

	private DataSchema dataSchema;

	private List<DataField> dataFields = new ArrayList<>();

	private Boolean deleted;

	private Integer version;

	private EnumDataModelType modelType;

	@Version
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

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "test_database_id")
	public DataSchema getDataSchema() {
		return dataSchema;
	}

	public void setDataSchema(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "test_table_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<DataField> getDataFields() {
		return dataFields;
	}

	public void setDataFields(List<DataField> dataFields) {
		this.dataFields = dataFields;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Boolean getIsDict() {
		return isDict;
	}

	public void setIsDict(Boolean isDict) {
		this.isDict = isDict;
	}

	public EnumDataModelType getModelType() {
		return modelType;
	}

	public void setModelType(EnumDataModelType modelType) {
		this.modelType = modelType;
	}

}