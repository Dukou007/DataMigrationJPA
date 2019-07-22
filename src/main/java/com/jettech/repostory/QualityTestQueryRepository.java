package com.jettech.repostory;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.QualityTestQuery;
@Repository
public interface QualityTestQueryRepository extends JpaRepository<QualityTestQuery, Integer>{
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "delete from quality_test_query   WHERE id=?1", nativeQuery = true)
	void deleteById(Integer id);
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "select * from  quality_test_query  t where t.test_field_id=?1", nativeQuery = true)
	List<QualityTestQuery> findByTestFieldId(Integer id);
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "select * from  quality_test_query  t where t.data_source_id=?1", nativeQuery = true)
	List<QualityTestQuery> findByDataSourceId(Integer id);

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "select * from  quality_test_query  t where t.quality_suite_id=?1", nativeQuery = true)
	List<QualityTestQuery> findByQualitySuiteId(Integer id);
}
