package com.jettech.entity;

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
@Table(name = "test_database", uniqueConstraints = {@UniqueConstraint(name = "ix_name_data_source", columnNames = {"name","data_source_id"})})
public class DataSchema extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9052587204507061506L;

	private String name;
	//是否是数据字典
	private Boolean isDict;

	private DataSource dataSource;

	private List<DataTable> dataTables;
	
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "data_source_id")
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "test_database_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<DataTable> getDataTables() {
		return dataTables;
	}

	public void setDataTables(List<DataTable> dataTables) {
		this.dataTables = dataTables;
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