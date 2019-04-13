package com.jettech.repostory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.DataField;
import com.jettech.entity.TestQueryField;

public interface TestQueryFieldRepository extends JpaRepository<TestQueryField, Integer> {
	@Query(value = "select * from test_query_field  where test_field_id =?1", nativeQuery = true)
    List<TestQueryField> findByForeignKey(int test_field_id);
}
