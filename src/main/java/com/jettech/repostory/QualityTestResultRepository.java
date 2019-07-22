package com.jettech.repostory;


import com.jettech.EnumExecuteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.QualityTestResult;

import java.util.List;

@Repository
public interface QualityTestResultRepository extends JpaRepository<QualityTestResult, Integer> ,JpaSpecificationExecutor<QualityTestResult>{

	@Query(value="select * from quality_test_result  where test_round_id=?1 and test_case_name like CONCAT('%',?2,'%') ", countQuery = "select count(*) from quality_test_result  where test_round_id=?1 and test_case_name like CONCAT('%',?2,'%')",nativeQuery=true)
	Page<QualityTestResult> findByRidAndName(int roundId,String name,Pageable pageable);

	List<QualityTestResult> findByTestCaseId(int testCaseId, Sort sort);



}
