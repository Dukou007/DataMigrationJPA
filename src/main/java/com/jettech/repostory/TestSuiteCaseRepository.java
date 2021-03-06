package com.jettech.repostory;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestSuiteCase;

@Repository
public interface TestSuiteCaseRepository extends JpaRepository<TestSuiteCase, Integer> {

	@Query(value = "SELECT tsc.case_id FROM `test_suite_case` tsc LEFT JOIN test_suite ts on ts.id=tsc.suite_id WHERE tsc.suite_id=?1", nativeQuery = true)
	Integer[] findCaseIdsBysuiteId(Integer suiteId);

	@Query(value = "SELECT count(*) FROM test_suite_case tsc WHERE tsc.suite_id =?1", nativeQuery = true)
	Integer CountCase(Integer suiteId);

	@Modifying
	@Transactional
	@Query(value = "INSERT into test_suite_case  (suite_id,case_id) VALUES (?1,?2)", nativeQuery = true)
	void changeTestCasePosition(Integer suiteId, Integer caseId);

	@Query(value = "SELECT tc.* FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')", countQuery = "SELECT count(*) FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')", nativeQuery = true)
	Page<TestCase> findbyCaseNameAndSuiteId(Integer testSuiteID, String name, Pageable pageable);

	/**
	 * 查找指定的中间表对象
	 * 
	 * @param caseId
	 * @param suiteId
	 * @return
	 */
	TestSuiteCase findByCaseIdAndSuiteId(Integer caseId, Integer suiteId);

	/**
	 * 根据suiteId查找对应的中间表关系对象
	 * 
	 * @param suiteId
	 * @return
	 */
	List<TestSuiteCase> findBySuiteId(int suiteId);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM test_suite_case  WHERE case_id = ?1", nativeQuery = true)
	void deleteByCaseId(int id);

}
