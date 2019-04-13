package com.jettech.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="comp_fields")
public class CompFields implements Serializable {

	/**
	 * 用于比较sql的key
	 */
	
	private Integer id;
	private String newTableName;
	private String newKeyField;
	private String oldTableName;
	private String oldKeyField;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}

	public String getNewTableName() {
		return newTableName;
	}
	public void setNewTableName(String newTableName) {
		this.newTableName = newTableName;
	}
	public String getNewKeyField() {
		return newKeyField;
	}
	public void setNewKeyField(String newKeyField) {
		this.newKeyField = newKeyField;
	}
	public String getOldTableName() {
		return oldTableName;
	}
	public void setOldTableName(String oldTableName) {
		this.oldTableName = oldTableName;
	}
	public String getOldKeyField() {
		return oldKeyField;
	}
	public void setOldKeyField(String oldKeyField) {
		this.oldKeyField = oldKeyField;
	}
	public CompFields(Integer id, String newTableName, String newKeyField, String oldTableName, String oldKeyField) {
		super();
		this.id = id;
		this.newTableName = newTableName;
		this.newKeyField = newKeyField;
		this.oldTableName = oldTableName;
		this.oldKeyField = oldKeyField;
	}
	public CompFields() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
