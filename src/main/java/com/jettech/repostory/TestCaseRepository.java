package com.jettech.repostory;

import java.util.List;
import java.util.Map;

import org.hibernate.type.TrueFalseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.EnumCompareDirection;
import com.jettech.entity.TestCase;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer>, JpaSpecificationExecutor<TestCase> {

	Page<TestCase> findAllByEnumCompareDirectionOrderByIdDesc(
			@Param("enumCompareDirection") EnumCompareDirection enumCompareDirection, Pageable pageable);

	List<TestCase> findByName(String name);

	@Query(value = "SELECT tc.* FROM test_suite_case tsc  INNER JOIN test_case tc  on tsc.case_id =tc.id INNER JOIN test_suite ts on tsc.suite_id = ts.id WHERE tc.name LIKE CONCAT('%','?1','%') AND ts.name LIKE CONCAT('%','?2','%')", nativeQuery = true)
	List<TestCase> findByNameAndSuite(String testCaseName, String suiteName);

	/**
	 * 根据testCaseID查看TestCase详情
	 * 
	 * @param testCaseID
	 * @return
	 */
	@Query(value = "SELECT * FROM test_case t WHERE t.id=?1", nativeQuery = true)
	Map<String, Object> getTestCaseDetail(Integer testCaseID);

	/**
	 * 根据testSuiteID查找对应的所有的testcase（集合）
	 */
	@Query(value = "SELECT tc.* FROM test_case tc WHERE tc.test_suite_id=?1", nativeQuery = true)
	public List<TestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * @param testSuiteID
	 * @param pageable
	 * @return 右侧根据案例集id查询所有案例
	 */
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)", 
			countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)")
//	@Query(value = "SELECT tc.* FROM test_case tc WHERE tc.id in (SELECT case_id FROM test_suite_case WHERE suite_id=?1)", 
//			countQuery="SELECT count(*) FROM test_case tc WHERE tc.id in (SELECT case_id FROM test_suite_case WHERE suite_id=3)",
//			nativeQuery = true)
	Page<TestCase> findByTestSuiteId(@Param("suiteId")Integer suiteId, Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param name
	 * @param pageable
	 * @return 右侧根据案例集id查询所有案例
	 */
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')", countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')")
	Page<TestCase> findByTestSuiteIdAndNameContaining(Integer testSuiteID, String name, Pageable pageable);

	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", nativeQuery = true)
	Page<TestCase> findBysuiteName(@Param("suiteName") String suiteName, Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<TestCase> getAllTestCaseByPage(@Param("name") String name, Pageable pageable);

	Page<TestCase> findByNameLikeOrderByIdDesc(@Param("name") String name, Pageable pageable);

	Page<TestCase> findByNameLikeAndEnumCompareDirectionLikeOrderByIdDesc(@Param("name") String name,
			@Param("enumCompareDirection") EnumCompareDirection enumCompareDirection, Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE test_case  t SET t.test_suite_id=?1 WHERE t.id=?2", nativeQuery = true)
	void changeTestCasePosition(@Param("testSuiteID") Integer testSuiteID, @Param("testCaseID") Integer testCaseID);

	/**
	 * @Description: 将testSuiteID置为null;
	 * @Tips: null;
	 * @State: being used / drop
	 * @author: zhou_xiaolong in 2019年2月24日下午8:41:40
	 */
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE test_case t SET  t.test_suite_id=null WHERE t.id=?1", nativeQuery = true)
	void backDisorder(@Param("testCaseID") Integer testCaseID);

	// @Query(value="SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id is
	// ?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND
	// t.test_suite_id is ?1",nativeQuery=true)
//	Page<TestCase> findByTestSuiteIsNull(Pageable pageable);

	@Query(value = "FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ", countQuery = "select count(*) FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ")
	Page<TestCase> findByTestSuiteIsNullAndNameContaining(Integer testSuiteID,String name, Pageable pageable);

	/**
	 * @Description: 根据名称查找
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月11日下午2:38:07
	 * @param name
	 * @return
	 */
	@Query(value = "SELECT * FROM test_case t WHERE t.`name` =?1", nativeQuery = true)
	TestCase findByCaseName(@Param("name") String name);

	@Query(value = "SELECT * FROM test_case t WHERE t.`name` =?1", nativeQuery = true)
	List<TestCase> findCaseListByCaseName(@Param("name") String name);

	List<TestCase> findAllById(Integer id);

	@Query(value = "from TestCase order by id desc", countQuery = "select count(*) from TestCase  ")
	Page<TestCase> findAllOrderByIdDesc(Pageable pageable);

	List<TestCase> findAllByNameLike(@Param("name") String name);

	@Transactional(timeout = 30000)
	@Query(value = "SELECT count(*) FROM test_case tc WHERE tc.test_suite_id=?1", nativeQuery = true)
	Integer countBySuiteId(@Param("suiteId") Integer suiteId);

	/**
	 * 查找不在制定测试集ID中的案例
	 * 
	 * @param testSuiteId
	 * @param name
	 * @param pageable
	 * @return
	 */
//	@Query(value="SELECT tc.* FROM	test_case tc WHERE	id NOT IN ( SELECT id FROM test_case WHERE test_suite_id = ?1 ) 	AND tc.NAME LIKE CONCAT( '%', '?2', '%' )",
//			countQuery="SELECT	count(*) FROM	test_case tc WHERE	id NOT IN ( SELECT id FROM test_case WHERE test_suite_id = ?1 ) 	AND tc.NAME LIKE CONCAT( '%', '?2', '%' )",
//			nativeQuery=true)
//	@Query(value="FROM TestCase WHERE id NOT IN(SELECT id FROM TestCase WHERE testSuite.id = ?1)AND name LIKE CONCAT( '%', '?2', '%' )",
//			countQuery="SELECT	COUNT(*) FROM TestCase WHERE id NOT IN(SELECT id FROM TestCase WHERE testSuite.id = ?1)AND name LIKE CONCAT( '%', '?2', '%' )")
//	Page<TestCase> findByTestSuiteIdNotInAndNameContaining(@Param("testSuiteID")Integer testSuiteID,@Param("name") String name, Pageable pageable);
	@Query(value="SELECT tc.* FROM	test_suite_case tsc	INNER JOIN test_suite ts ON tsc.suite_id = ts.id INNER JOIN test_case tc ON tsc.case_id = tc.id WHERE ts.id =?1",nativeQuery=true)
	List<TestCase> findByTestSuiteId(Integer testSuiteID);
	/**
	 * @param testSuiteID
	 * @param pageable
	 * @return 左侧所有的案例
	 */

	@Query(value = " FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ", countQuery = " select count(*) FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ")
	Page<TestCase> findByTestSuiteNotContain(Integer testSuiteID, Pageable pageable);


}
