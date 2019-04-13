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

import com.jettech.entity.ModelTestCase;

@Repository
public interface ModelTestCaseRepository extends JpaRepository<ModelTestCase, Integer> {

	@Query(value = "SELECT t.* FROM model_test_case  t WHERE 1=1 AND t.name LIKE %?1% ", nativeQuery = true)
	List<ModelTestCase> findByName(String name);

//	@Query(value = "select t from TestCase t where t.name=?1 and t.testSuite.name=?2")
//	List<ModelTestCase> findByNameAndSuite(String testCaseName, String testSuiteName);

	/**
	 * 根据testCaseID查看TestCase详情
	 * 
	 * @param testCaseID
	 * @return
	 */
	@Query(value = "SELECT * FROM model_model_test_case t WHERE t.id=?1", nativeQuery = true)
	Map<String, Object> getTestCaseDetail(Integer testCaseID);

	/**
	 * 根据testSuiteID查找对应的所有的testcase（集合）
	 */
//	@Query(value="select * from model_model_test_case t where t.test_suite_id=?1",nativeQuery=true)
//	public List<ModelTestCase> findAllTestCase(Integer testSuiteID);

	/**
	 * 根据测试集id查询所有案例 20190121
	 * 
	 * @param testSuiteId
	 * @param pageable
	 * @return
	 */
	@Query(value="SELECT tc.* FROM test_suite_case tsc INNER JOIN model_test_case tc ON  tsc.case_id=tc.id INNER JOIN test_suite ts ON tsc.suite_id =ts.id WHERE tsc.suite_id is =?1",countQuery="SELECT count(*) FROM test_suite_case tsc INNER JOIN model_test_case tc ON  tsc.case_id=tc.id INNER JOIN test_suite ts ON tsc.suite_id =ts.id WHERE tsc.suite_id is =?1",nativeQuery=true)
	Page<ModelTestCase> findByTestSuiteId(Integer testSuiteId, Pageable pageable);

	@Query(value = "SELECT tc.* FROM test_suite_case tsc INNER JOIN model_test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.`name` LIKE CONCAT('%','?1','%')", countQuery = "SELECT count(*) FROM test_suite_case tsc INNER JOIN model_test_case tc on tsc.case_id=tc.id INNER JOIN test_suite ts on tsc.suite_id =ts.id WHERE ts.`name` LIKE CONCAT('%','?1','%')", nativeQuery = true)
	Page<ModelTestCase> findBysuiteName(@Param("suiteName") String suiteName, Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM model_test_case t WHERE 1=1 AND if(t.name!='',t.name LIKE CONCAT('%',?1,'%'), 1=1 ) order by id desc", countQuery = "select count(*) from model_test_case t where 1=1 and if(t.name!='',t.name LIKE CONCAT('%',?1,'%'), 1=1 ) ", nativeQuery = true)
	Page<ModelTestCase> getAllTestCaseByPage(String name, Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE model_test_case  t SET t.test_suite_id=?1 WHERE t.id=?2", nativeQuery = true)
	void changeTestCasePosition(Integer testSuiteID, Integer testCaseID);

	/**
	 * @Description: 将testSuiteID置为null;
	 * @Tips: null;
	 * @State: being used / drop
	 * @author: zhou_xiaolong in 2019年2月24日下午8:41:40
	 */
	@Transactional
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE model_test_case t SET  t.test_suite_id=null WHERE t.id=?1", nativeQuery = true)
	void backDisorder(Integer testCaseID);

	@Query(value = "SELECT tc.* FROM test_suite_case tsc  INNER JOIN model_test_case tc  on tsc.case_id =tc.id INNER JOIN test_suite ts on tsc.suite_id = ts.id WHERE tc.name LIKE CONCAT('%','?1','%') AND ts.name LIKE CONCAT('%','?2','%')", nativeQuery = true)
	List<ModelTestCase> findByNameAndSuite(String testCaseName, String name);

	@Query(value="SELECT tc.* FROM test_suite_case tsc INNER JOIN model_test_case tc ON  tsc.case_id=tc.id INNER JOIN test_suite ts ON tsc.suite_id =ts.id WHERE tsc.suite_id is  NULL",countQuery="SELECT count(*) FROM test_suite_case tsc INNER JOIN model_test_case tc ON  tsc.case_id=tc.id INNER JOIN test_suite ts ON tsc.suite_id =ts.id WHERE tsc.suite_id is  NULL",nativeQuery=true)
	Page<ModelTestCase> findByTestSuiteIdIsNull(Integer testSuiteID, Pageable pageable);

}
