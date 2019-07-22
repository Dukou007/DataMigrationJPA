package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.TestQuery;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TestQueryRepository extends JpaRepository<TestQuery, Integer> {

    @Query(value = "select t.* from test_query t where t.test_case_id = ?1",nativeQuery=true)
    List<TestQuery> findByCaseId(int testCaseId);
    @Query(value = "select t.* from test_query t where t.data_source_id = ?1",nativeQuery=true)
    List<TestQuery> findByDataSourceId(int id);
    
    
//	TestQuery findbyName(String sourceDataSourceName);
}
