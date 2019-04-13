package com.jettech.repostory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.QualityTestCase;

import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface QualityTestCaseRepository extends JpaRepository<QualityTestCase, Integer>,JpaSpecificationExecutor<QualityTestCase>{
	@Query(value = "SELECT * FROM `quality_test_case` t WHERE 1=1 AND t.name LIKE %?1% ", countQuery = "select count(*) from quality_test_case t where  t.name like CONCAT('%',?1,'%') ",nativeQuery = true)
	Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable);

	Page<QualityTestCase>findByNameLike(String name, Pageable pageable);
	//====添加的方法  V 20190318=============================================
	List<QualityTestCase> findByName(String name);

	@Query(value = "select t from QualityTestCase t where t.name=?1 and t.testSuite.name=?2",nativeQuery=true)
	List<QualityTestCase> findByNameAndSuite(String testCaseName, String testSuiteName);

	/**
	 * 根据testCaseID查看TestCase详情
	 * @param testCaseID
	 * @return
	 */
	@Query(value="SELECT * FROM test_case t WHERE t.id=?1",nativeQuery=true)
	Map<String,Object> getTestCaseDetail(Integer testCaseID);

	/**
	 * 根据testSuiteID查找对应的所有的testcase（集合）
	 */
	@Query(value="select * from test_case t where t.test_suite_id=?1",nativeQuery=true)
	public List<QualityTestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * 根据测试集id查询所有案例 20190121
	 * @param testSuiteId
	 * @param pageable
	 * @return
	 */
	@Query(value="SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id=?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id=?1",nativeQuery=true)
	Page<QualityTestCase> findByTestSuiteId(Integer testSuiteId, Pageable pageable);

	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ",countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ",nativeQuery=true)
	Page<QualityTestCase> findBysuiteName(@Param("suiteName") String suiteName, Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ",nativeQuery = true)
	Page<QualityTestCase> getAllTestCaseByPage(String name, Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value="UPDATE test_case  t SET t.test_suite_id=?1 WHERE t.id=?2",nativeQuery=true)
	void changeTestCasePosition(Integer testSuiteID, Integer testCaseID);

	/**
	 * @Description: 将testSuiteID置为null;
	 * @Tips: null;
	 * @State: being used / drop
	 * @author: zhou_xiaolong in 2019年2月24日下午8:41:40
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value="UPDATE quality_test_case t SET  t.test_suite_id=null WHERE t.id=?1",nativeQuery=true)
	void backDisorder(Integer testCaseID);



	@Query(value="SELECT * FROM quality_test_case t WHERE 1=1 AND t.test_suite_id is ?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id is ?1",nativeQuery=true)
	Page<QualityTestCase> findByTestSuiteIdIsNull(Integer testSuiteID, Pageable pageable);

	@Query(value="SELECT * FROM `quality_test_case` t WHERE t.`name` LIKE %?1%",countQuery="SELECT count(*) FROM `test_case` t WHERE t.`name` LIKE %?1%",nativeQuery=true)
	Page<QualityTestCase> findTestCaseByName(String name, Pageable pageable);

	@Query(value="SELECT * FROM `quality_test_case` t WHERE t.test_suite_id=?1",countQuery="SELECT count(*) FROM `test_case` t WHERE t.test_suite_id=?1",nativeQuery=true)
	Page<QualityTestCase> findBySuiteId(Integer testSuiteID, Pageable pageable);
	@Query(value="SELECT * FROM quality_test_case qtc WHERE qtc.`name`=?1",nativeQuery=true)
	List<QualityTestCase> findByCaseName(String name);

	Page<QualityTestCase> findAllByOrderByIdDesc(Pageable pageable);

}
