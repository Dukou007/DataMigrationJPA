package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.CompFields;

public interface CompFieldsRepository extends JpaRepository<CompFields, Integer> {

	@Query("select t from CompFields as t where t.newTableName =?1")
	public CompFields findByNewTableName(String newTableName);
}
