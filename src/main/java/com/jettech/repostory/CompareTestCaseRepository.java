package com.jettech.repostory;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.CompareTestCase;
import com.jettech.entity.TestCase;
import com.jettech.vo.TestCaseVO;
@Repository
public interface CompareTestCaseRepository extends JpaRepository<CompareTestCase, Integer> {
	
	@Query(value="SELECT t.* FROM test_case  t WHERE 1=1 AND t.name LIKE %?1% ",nativeQuery=true)
	List<CompareTestCase> findByName(String name);

//	@Query(value = "select t from TestCase t where t.name=?1 and t.testSuite.name=?2")
//	List<CompareTestCase> findByNameAndSuite(String testCaseName, String testSuiteName);

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
//	@Query(value="select * from test_case t where t.test_suite_id=?1",nativeQuery=true)
//	public List<CompareTestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * 根据测试集id查询所有案例 20190121
	 * @param testSuiteId
	 * @param pageable
	 * @return
	 */
//	@Query(value="SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id=?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id=?1",nativeQuery=true)
//	Page<CompareTestCase> findByTestSuiteId(Integer testSuiteId, Pageable pageable);

//	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ",countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ",nativeQuery=true)
//	Page<CompareTestCase> findBysuiteName(@Param("suiteName") String suiteName,Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
//	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE %?1% ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ",nativeQuery = true)
//	Page<CompareTestCase> getAllTestCaseByPage(String name, Pageable pageable);

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
//	@Transactional
//	@Modifying(clearAutomatically = true)
//	@Query(value="UPDATE test_case t SET  t.test_suite_id=null WHERE t.id=?1",nativeQuery=true)
//	void backDisorder(Integer testCaseID);

	

//	@Query(value="SELECT * FROM test_case t WHERE 1=1 AND t.test_suite_id is ?1",countQuery="SELECT COUNT(*) FROM test_case t WHERE 1=1 AND t.test_suite_id is ?1",nativeQuery=true)
//	Page<CompareTestCase> findByTestSuiteIdIsNull(Integer testSuiteID, Pageable pageable);
	/**
	 * @Description: 根据名称查找
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月11日下午2:38:07
	 * @param name
	 * @return
	 */
	@Query(value="SELECT * FROM test_case t WHERE t.`name` =?1",nativeQuery=true)
	CompareTestCase findByCaseName(String name);
}
