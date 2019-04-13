package com.jettech.repostory;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestSuiteCase;

@Repository
public interface TestSuiteCaseRepository extends JpaRepository<TestSuiteCase, Integer> {

	@Query(value = "SELECT tsc.case_id FROM `test_suite_case` tsc LEFT JOIN test_suite ts on ts.id=tsc.suite_id WHERE tsc.suite_id=?1", nativeQuery = true)
	Integer[] findCaseIdsBysuiteId(Integer suiteId);
	@Query(value="SELECT count(*) FROM test_suite_case tsc WHERE tsc.suite_id =?1",nativeQuery=true)
	Integer CountCase(Integer suiteId);
	
	@Modifying
	@Transactional
	@Query(value = "UPDATE test_suite_case tsc set tsc.suite_id=null WHERE tsc.case_id =?1", nativeQuery = true)
	void backDisorder(Integer valueOf);
	@Modifying
	@Transactional
	@Query(value = "INSERT into test_suite_case  (suite_id,case_id) VALUES (?1,?2)", nativeQuery = true)
	void changeTestCasePosition(Integer suiteId, Integer caseId);
	@Transactional(timeout=30000)
	@Query(value="SELECT tc.* FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')",countQuery="SELECT count(*) FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')",nativeQuery=true)
	Page<TestCase> findbyCaseNameAndSuiteId(Integer testSuiteID, String name, Pageable pageable);
	
}
