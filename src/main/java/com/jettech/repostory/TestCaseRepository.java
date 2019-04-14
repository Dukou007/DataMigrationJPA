package com.jettech.repostory;

import java.util.List;
import java.util.Map;

import org.hibernate.type.TrueFalseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.TestCase;

@Repository
public interface TestCaseRepository
		extends JpaRepository<TestCase, Integer>, PagingAndSortingRepository<TestCase, Integer> {

	Page<TestCase> findAllByOrderByIdDesc(Pageable pageable);

	List<TestCase> findByName(String name);

	//////// List<TestCase> findByNameAndSuite(String testCaseName, String
	//////// testSuiteName);

	/**
	 * 根据testCaseID查看TestCase详情
	 * 
	 * @param testCaseID
	 * @return
	 */
//	@Query(value="SELECT * FROM test_case t WHERE t.id=?1",nativeQuery=true)
//	Map<String,Object> getTestCaseDetail(Integer testCaseID);

	/**
	 * 根据testSuiteID查找对应的所有的testcase（集合）
	 */
//	@Query(value="select * from test_case t where t.test_suite_id=?1",nativeQuery=true)
//	public List<TestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * 根据测试集id查询所有案例 20190121
	 * 
	 * @param testSuiteId
	 * @param pageable
	 * @return
	 */
	// @Query(value="SELECT * FROM test_case t WHERE 1=1 AND
	// t.test_suite_id=?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1
	// AND t.test_suite_id=?1",nativeQuery=true)
	// Page<TestCase> findByTestSuiteId(Integer testSuiteId, Pageable pageable);

	// Page<TestCase> findByTestSuiteIdAndNameContaining(Integer testSuiteId,String
	// name, Pageable pageable);

	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", nativeQuery = true)
	Page<TestCase> findBysuiteName(@Param("suiteName") String suiteName, Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<TestCase> getAllTestCaseByPage(String name, Pageable pageable);

	Page<TestCase> findByNameLikeOrderByIdDesc(@Param("name") String name, Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
//	@Transactional
//	@Modifying(clearAutomatically = true)
//	@Query(value="UPDATE test_case  t SET t.test_suite_id=?1 WHERE t.id=?2",nativeQuery=true)
//	void changeTestCasePosition(Integer testSuiteID, Integer testCaseID);

	/**
	 * @Description: 将testSuiteID置为null;
	 * @Tips: null;
	 * @State: being used / drop
	 * @author: zhou_xiaolong in 2019年2月24日下午8:41:40
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE test_case t SET  t.test_suite_id=null WHERE t.id=?1", nativeQuery = true)
	void backDisorder(Integer testCaseID);

	// @Query(value="SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id is
	// ?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND
	// t.test_suite_id is ?1",nativeQuery=true)
	// Page<TestCase> findByTestSuiteIsNull(Pageable pageable);

//	Page<TestCase> findByTestSuiteIsNullAndNameContaining(String name,Pageable pageable);
	/**
	 * @Description: 根据名称查找
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月11日下午2:38:07
	 * @param name
	 * @return
	 */
	@Query(value = "SELECT * FROM test_case t WHERE t.`name` =?1", nativeQuery = true)
	TestCase findByCaseName(String name);

//	List<TestCase> findAllById(Integer id);
	@Query(value = "from TestCase order by id desc", countQuery = "select count(*) from TestCase  ")
	Page<TestCase> findAllOrderByIdDesc(Pageable pageable);

	@Transactional(timeout = 30000)
	@Query(value = "SELECT tc.* FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')", countQuery = "SELECT count(*) FROM test_suite_case tsc INNER JOIN test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.id=?1 AND tc.name LIKE CONCAT('%',?2,'%')", nativeQuery = true)
	Page<TestCase> findbyCaseNameAndSuiteId(Integer testSuiteID, String name, Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param pageable
	 * @return 左侧所有的案例
	 */

//	@Query(value = "SELECT tc.* FROM test_case tc WHERE tc.id NOT IN (SELECT tsc.case_id FROM test_suite_case tsc WHERE tsc.suite_id =?1)", countQuery = " SELECT count(*) FROM test_case tc WHERE tc.id NOT IN (SELECT tsc.case_id FROM test_suite_case tsc WHERE tsc.suite_id =?1)",nativeQuery=true)
	@Query(value = " FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ", countQuery = " select count(*) FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ")
	Page<TestCase> findByTestSuiteNotContain(Integer testSuiteID, Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param name
	 * @param pageable
	 * @return 左侧所有的案例
	 */
//	@Query(value = "SELECT tc.* FROM test_case tc WHERE tc.id NOT IN (SELECT tsc1.case_id FROM test_suite_case tsc1 INNER JOIN test_case tc1  on tsc1.case_id=tc1.id WHERE tsc1.suite_id =4 AND tc1.`name` LIKE CONCAT('%','?1','%') )",
//			countQuery = "SELECT count(*) FROM test_case tc WHERE tc.id NOT IN (SELECT tsc1.case_id FROM test_suite_case tsc1 INNER JOIN test_case tc1  on tsc1.case_id=tc1.id WHERE tsc1.suite_id =4 AND tc1.`name` LIKE CONCAT('%','?1','%') )",
//			nativeQuery=true)
	@Query(value = "FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ", countQuery = "select count(*) FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ")
	Page<TestCase> findByTestSuiteIsNullAndNameContaining(Integer testSuiteID,String name, Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param name
	 * @param pageable
	 * @return 右侧根据案例集id查询所有案例
	 */
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')", countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')")
	Page<TestCase> findByTestSuiteIdAndNameContaining(Integer testSuiteID, String name, Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param pageable
	 * @return 右侧根据案例集id查询所有案例
	 */
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)", countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)")
	Page<TestCase> findByTestSuiteId(Integer testSuiteID, Pageable pageable);

	@Query(value = "SELECT tc.* FROM test_suite_case tsc  INNER JOIN test_case tc  on tsc.case_id =tc.id INNER JOIN test_suite ts on tsc.suite_id = ts.id WHERE tc.name LIKE CONCAT('%','?1','%') AND ts.name LIKE CONCAT('%','?2','%')", nativeQuery = true)
	List<TestCase> findByNameAndSuite(String testCaseName, String suiteName);

}
