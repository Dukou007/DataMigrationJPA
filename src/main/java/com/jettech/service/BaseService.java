package com.jettech.service;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.domain.BaseModel;
import com.jettech.entity.BaseEntity;

public abstract class BaseService<T, K> {
	protected JpaRepository<T, K> repository;

}
