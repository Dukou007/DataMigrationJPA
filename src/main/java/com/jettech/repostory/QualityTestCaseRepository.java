package com.jettech.repostory;

import com.jettech.EnumExecuteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.QualityTestCase;

import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
@Repository
public interface QualityTestCaseRepository
		extends JpaRepository<QualityTestCase, Integer>, JpaSpecificationExecutor<QualityTestCase> {
	@Query(value = "SELECT * FROM `quality_test_case` t WHERE 1=1 AND t.name LIKE %?1% ", countQuery = "select count(*) from quality_test_case t where  t.name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable);
	@Query(value = "SELECT * FROM quality_test_case  WHERE 1=1 and test_suite_id=?2 AND name LIKE %?1% ", countQuery = "select count(*) from quality_test_case  where  test_suite_id=?2 and name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<QualityTestCase> findCaseBySuiteIdNamePage(String name, Integer testSuiteId,Pageable pageable);
	Page<QualityTestCase> findByNameLike(String name, Pageable pageable);

	// ====添加的方法 V 20190318=============================================
	List<QualityTestCase> findByName(String name);

	@Query(value = "select t from QualityTestCase t where t.name=?1 and t.testSuite.name=?2", nativeQuery = true)
	List<QualityTestCase> findByNameAndSuite(String testCaseName, String testSuiteName);

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
	@Query(value = "select * from test_case t where t.test_suite_id=?1", nativeQuery = true)
	public List<QualityTestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * 根据测试集id查询所有案例 20190121
	 * 
	 * @param testSuiteId
	 * @param pageable
	 * @return
	 */
	@Query(value = "SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id=?1", countQuery = "SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id=?1", nativeQuery = true)
	Page<QualityTestCase> findByTestSuiteId(Integer testSuiteId, Pageable pageable);

	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", nativeQuery = true)
	Page<QualityTestCase> findBysuiteName(@Param("suiteName") String suiteName, Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<QualityTestCase> getAllTestCaseByPage(String name, Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE test_case  t SET t.test_suite_id=?1 WHERE t.id=?2", nativeQuery = true)
	void changeTestCasePosition(Integer testSuiteID, Integer testCaseID);

	/**
	 * @Description: 将testSuiteID置为null;
	 * @Tips: null;
	 * @State: being used / drop
	 * @author: zhou_xiaolong in 2019年2月24日下午8:41:40
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE quality_test_case t SET  t.test_suite_id=null WHERE t.id=?1", nativeQuery = true)
	void backDisorder(Integer testCaseID);

	@Query(value = "SELECT * FROM quality_test_case t WHERE 1=1 AND t.test_suite_id is ?1", countQuery = "SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id is ?1", nativeQuery = true)
	Page<QualityTestCase> findByTestSuiteIdIsNull(Integer testSuiteID, Pageable pageable);

	@Query(value = "SELECT * FROM `quality_test_case` t WHERE t.`name` LIKE %?1%", countQuery = "SELECT count(*) FROM `test_case` t WHERE t.`name` LIKE %?1%", nativeQuery = true)
	Page<QualityTestCase> findTestCaseByName(String name, Pageable pageable);

	@Query(value = "SELECT * FROM quality_test_case t inner join suite_quality_case s on s.quality_test_case_id=t.id  WHERE s.test_suite_id=?1", nativeQuery = true)
	List<QualityTestCase> findBySuiteId(Integer testSuiteID);

	@Query(value = "SELECT * FROM quality_test_case qtc WHERE qtc.`name`=?1", nativeQuery = true)
	List<QualityTestCase> findByCaseName(String name);

	Page<QualityTestCase> findAllByOrderByIdDesc(Pageable pageable);

	@Query(value = "SELECT * FROM quality_test_case t WHERE t.id= ?1 AND t.test_suite_id=?2", nativeQuery = true)
	QualityTestCase findByCaseIdAndSuiteId(Integer caseId, Integer suiteId);

	//// 根据测试集id以及轮次id查询失败案例 0表示失败false
	@Query(value = "select  distinct t.*  from quality_test_case t inner  JOIN quality_test_result r on  t.id=r.test_case_id inner join test_round d on"
			+ " r.test_round_id=d.id  inner join test_suite s on d.test_suite_id=s.id inner join suite_quality_case e on e.quality_test_case_id=t.id "
			+ " WHERE  r.result=0 and e.test_suite_id=?1 and d.id=?2", nativeQuery = true)
	List<QualityTestCase> findByTestSuitIdAndRoundId(Integer test_suite_id, Integer test_round_id);

	//根据测试集id查询不在其中的案例 int suiteId, Pageable pageable
	@Query(value = "select t.*  from quality_test_case t where t.id not in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1)",
			countQuery = "select count(*) from quality_test_case t where t.id not in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1)", nativeQuery = true)
	Page<QualityTestCase> findByNotSuiteId(Integer suiteId,Pageable pageable);

	//查询不在该测试集下的所有名称的案例
	@Query(value = "select t.*  from quality_test_case t where t.id not in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1) and t.name LIKE CONCAT('%',?2,'%')",
			countQuery = "select count(*) from quality_test_case t where t.id not in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1) and t.name LIKE CONCAT('%',?2,'%')", nativeQuery = true)
	Page<QualityTestCase> findByNotCaseName(Integer suiteId,String name,Pageable pageable);

	//查询案例根据测试集Id
	@Query(value = "select t.*  from quality_test_case t where t.id in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1 )",
			countQuery = "select count(*) from quality_test_case t where t.id in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1)", nativeQuery = true)
	Page<QualityTestCase> findCaseBySuiteId(Integer suiteId,Pageable pageable);
	
	//查询所属案例根据轮次Id
	@Query(value = "select * FROM quality_test_case  WHERE id  IN ( SELECT quality_test_case_id FROM suite_quality_case  WHERE test_suite_id in (select test_suite_id from test_round where id=?1))", 
			countQuery = "select count(*) FROM quality_test_case  WHERE id  IN ( SELECT quality_test_case_id FROM suite_quality_case  WHERE test_suite_id in (select test_suite_id from test_round where id=?1))", nativeQuery = true)
	Page<QualityTestCase> findCaseByTestRoundId(@Param("suiteId")Integer suiteId, Pageable pageable);

	@Query(value = "select t.*  from quality_test_case t where t.id in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1 ) and t.name LIKE CONCAT('%',?2,'%')",
			countQuery = "select count(*) from quality_test_case t where t.id in (select quality_test_case_id from suite_quality_case where test_suite_id = ?1) and t.name LIKE CONCAT('%',?2,'%')", nativeQuery = true)
	Page<QualityTestCase> findCaseByName(Integer suiteId,String name,Pageable pageable);


}
