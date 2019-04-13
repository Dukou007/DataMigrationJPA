package com.jettech.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "mapper_entity")
public class MapperEntity {

	private Integer id;
	private String newfield;
	private String type;
	private String isPrimary;
	private String isUnique;
	private String nullable;
	private String isDefault;
	private String oldTableName;
	private String oldField;
	private String description;
	private String comments;
	private String other;
	private String newTableName;

	public String getOther() {
		return other;
	}

	public void setOther(String other) {
		this.other = other;
	}

	public String getNewTableName() {
		return newTableName;
	}

	public void setNewTableName(String newTableName) {
		this.newTableName = newTableName;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNewfield() {
		return newfield;
	}

	public void setNewfield(String newfield) {
		this.newfield = newfield;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getIsPrimary() {
		return isPrimary;
	}

	public void setIsPrimary(String isPrimary) {
		this.isPrimary = isPrimary;
	}

	public String getIsUnique() {
		return isUnique;
	}

	public void setIsUnique(String isUnique) {
		this.isUnique = isUnique;
	}

	public String getNullable() {
		return nullable;
	}

	public void setNullable(String nullable) {
		this.nullable = nullable;
	}

	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String getOldTableName() {
		return oldTableName;
	}

	public void setOldTableName(String oldTableName) {
		this.oldTableName = oldTableName;
	}

	@Column(length=320)
	public String getOldField() {
		return oldField;
	}

	public void setOldField(String oldField) {
		this.oldField = oldField;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}


	public MapperEntity(Integer id, String newfield, String type, String isPrimary, String isUnique, String nullable,
			String isDefault, String oldTableName, String oldField, String description, String comments, String other, String newTableName) {
		super();
		this.id = id;
		this.newfield = newfield;
		this.type = type;
		this.isPrimary = isPrimary;
		this.isUnique = isUnique;
		this.nullable = nullable;
		this.isDefault = isDefault;
		this.oldTableName = oldTableName;
		this.oldField = oldField;
		this.description = description;
		this.comments = comments;
		this.other = other;
		this.newTableName = newTableName;
	}

	public MapperEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

}
