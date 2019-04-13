package com.jettech.vo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jettech.entity.BaseEntity;

public abstract class BaseVO implements Serializable {

	private static final long serialVersionUID = -8325547656261282038L;

	private Integer id;

	private String createUser;

	private String editUser;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date editTime;

	public BaseVO() {
	}

	/**
	 * 将实体的属性复制到VO对象中，如果有实体不存在的属性(简单类型)，需要在VO对象的构造函数中自行复制
	 * 
	 * @param entity
	 */
	public BaseVO(BaseEntity entity) {
		this();
		if (entity != null) {
			BeanUtils.copyProperties(entity, this);
		}
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getEditUser() {
		return editUser;
	}

	public void setEditUser(String editUser) {
		this.editUser = editUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getEditTime() {
		return editTime;
	}

	public void setEditTime(Date editTime) {
		this.editTime = editTime;
	}

}
