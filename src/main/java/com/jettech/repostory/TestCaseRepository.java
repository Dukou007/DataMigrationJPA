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
import com.jettech.EnumExecuteStatus;
import com.jettech.entity.TestCase;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer>,
		JpaSpecificationExecutor<TestCase> {

	Page<TestCase> findAllByEnumCompareDirectionOrderByIdDesc(
			@Param("enumCompareDirection") EnumCompareDirection enumCompareDirection,
			Pageable pageable);

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
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)", countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1)")
	// @Query(value =
	// "SELECT tc.* FROM test_case tc WHERE tc.id in (SELECT case_id FROM test_suite_case WHERE suite_id=?1)",
	// countQuery="SELECT count(*) FROM test_case tc WHERE tc.id in (SELECT case_id FROM test_suite_case WHERE suite_id=3)",
	// nativeQuery = true)
	Page<TestCase> findByTestSuiteId(@Param("suiteId") Integer suiteId,
			Pageable pageable);

	/**
	 * @param testSuiteID
	 * @param name
	 * @param pageable
	 * @return 右侧根据案例集id查询所有案例
	 */
	@Query(value = "FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')", countQuery = "select count(*) FROM TestCase  WHERE id  IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1) and name like CONCAT('%',?2,'%')")
	Page<TestCase> findByTestSuiteIdAndNameContaining(Integer testSuiteID,
			String name, Pageable pageable);

	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", countQuery = "select count(*) from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like :suiteName ) ", nativeQuery = true)
	Page<TestCase> findBysuiteName(@Param("suiteName") String suiteName,
			Pageable pageable);

	/**
	 * @Description: getAllTestCaseByPage 昏页功能
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:57:45
	 */
	@Query(value = "SELECT *  FROM test_case t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_case t where  t.name like CONCAT('%',?1,'%') ", nativeQuery = true)
	Page<TestCase> getAllTestCaseByPage(@Param("name") String name,
			Pageable pageable);

	Page<TestCase> findByNameLikeOrderByIdDesc(@Param("name") String name,
			Pageable pageable);

	Page<TestCase> findByNameLikeAndEnumCompareDirectionLikeOrderByIdDesc(
			@Param("name") String name,
			@Param("enumCompareDirection") EnumCompareDirection enumCompareDirection,
			Pageable pageable);

	/**
	 * @Description: 将testSuiteID为null的案例赋值testSuiteID;
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午8:03:14
	 */
	@Modifying(clearAutomatically = true)
	@Query(value = "UPDATE test_case  t SET t.test_suite_id=?1 WHERE t.id=?2", nativeQuery = true)
	void changeTestCasePosition(@Param("testSuiteID") Integer testSuiteID,
			@Param("testCaseID") Integer testCaseID);

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
	// Page<TestCase> findByTestSuiteIsNull(Pageable pageable);

	@Query(value = "FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ", countQuery = "select count(*) FROM TestCase WHERE id NOT IN ( SELECT t.caseId FROM TestSuiteCase t inner join TestCase tc on t.caseId=tc.id WHERE t.suiteId =?1 ) and name like CONCAT('%',?2,'%') ")
	Page<TestCase> findByTestSuiteIsNullAndNameContaining(Integer testSuiteID,
			String name, Pageable pageable);

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
	// @Query(value="SELECT tc.* FROM	test_case tc WHERE	id NOT IN ( SELECT id FROM test_case WHERE test_suite_id = ?1 ) 	AND tc.NAME LIKE CONCAT( '%', '?2', '%' )",
	// countQuery="SELECT	count(*) FROM	test_case tc WHERE	id NOT IN ( SELECT id FROM test_case WHERE test_suite_id = ?1 ) 	AND tc.NAME LIKE CONCAT( '%', '?2', '%' )",
	// nativeQuery=true)
	// @Query(value="FROM TestCase WHERE id NOT IN(SELECT id FROM TestCase WHERE testSuite.id = ?1)AND name LIKE CONCAT( '%', '?2', '%' )",
	// countQuery="SELECT	COUNT(*) FROM TestCase WHERE id NOT IN(SELECT id FROM TestCase WHERE testSuite.id = ?1)AND name LIKE CONCAT( '%', '?2', '%' )")
	// Page<TestCase>
	// findByTestSuiteIdNotInAndNameContaining(@Param("testSuiteID")Integer
	// testSuiteID,@Param("name") String name, Pageable pageable);
	@Query(value = "SELECT tc.* FROM	test_suite_case tsc	INNER JOIN test_suite ts ON tsc.suite_id = ts.id INNER JOIN test_case tc ON tsc.case_id = tc.id WHERE ts.id =?1", nativeQuery = true)
	List<TestCase> findByTestSuiteId(Integer testSuiteID);

	/**
	 * @param testSuiteID
	 * @param pageable
	 * @return 左侧所有的案例
	 */

	@Query(value = " FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ", countQuery = " select count(*) FROM TestCase  WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase  WHERE suiteId =?1 ) ")
	Page<TestCase> findByTestSuiteNotContain(Integer testSuiteID,
			Pageable pageable);

	@Query(value = " select t From TestCase t join TestSuiteCase ts on ts.caseId =t.id where  t.name like CONCAT('%',?1,'%') and t.enumCompareDirection=?2 and ts.suiteId =?3 order by t.editTime desc", countQuery = "select count(*) from TestCase t,TestSuiteCase ts where ts.caseId =t.id and t.name like CONCAT('%',?1,'%') and t.enumCompareDirection=?2 and ts.suiteId =?3")
	Page<TestCase> getAllTestCByPage(String name,
			EnumCompareDirection enumCompareDirection, Integer suiteId,
			Pageable pageable);

	@Query(value = " select t From TestCase t join TestSuiteCase ts on ts.caseId =t.id where t.name like CONCAT('%',?1,'%')  and ts.suiteId =?2  order by t.editTime desc ", countQuery = "select count(*) from TestCase t,TestSuiteCase ts where ts.caseId =t.id and t.name like CONCAT('%',?1,'%') and ts.suiteId =?2")
	Page<TestCase> getAllTCByPage(String name, Integer suiteId,
			Pageable pageable);

	@Query(value = "select t From TestCase t join TestSuiteCase ts on ts.caseId =t.id where t.enumCompareDirection =?1   and ts.suiteId =?2   order by t.editTime desc ", countQuery = "select count(*) from TestCase t,TestSuiteCase ts where ts.caseId =t.id and t.enumCompareDirection =?1 and ts.suiteId =?2")
	Page<TestCase> getAllTByPage(EnumCompareDirection enumCompareDirection,
			Integer suiteId, Pageable pageable);

	@Query(value = "select t From TestCase t join TestSuiteCase ts on ts.caseId =t.id where t.enumCompareDirection =?1   and ts.suiteId =?2   order by t.editTime desc ", countQuery = "select count(*) from TestCase t,TestSuiteCase ts where ts.caseId =t.id and t.enumCompareDirection =?1 and ts.suiteId =?2")
	Page<TestCase> getAllTRByPage(EnumCompareDirection enumCompareDirection,
			Integer suiteId, Pageable pageable);

	// 根据四个条件查
	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2  and ts.suiteId=?3 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?4)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2  and ts.suiteId=?3 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?4)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNESR(String name,
			EnumCompareDirection enumCompareDirection, Integer suiteId,
			EnumExecuteStatus execState, Pageable pageable);

	// 根据四个条件查，状态为init
	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2  and ts.suiteId=?3 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2  and ts.suiteId=?3 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNESRI(String name,
			EnumCompareDirection enumCompareDirection, Integer suiteId,
			Pageable pageable);

	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId where  c.name like CONCAT('%',?1,'%') "
			+ "  and ts.suiteId=?2 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "  and ts.suiteId=?2 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNDRI(String name, Integer suiteId, Pageable pageable);

	@Query(value = "select c From TestCase c  where  "
			+ "c.name=?1 and c.enumCompareDirection=?2 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c  where   "
			+ "c.name=?1 and c.enumCompareDirection=?2  and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllERI(String name,
			EnumCompareDirection enumCompareDirection, Pageable pageable);

	@Query(value = "select c From TestCase c  where  "
			+ "c.enumCompareDirection=?1 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c  where   "
			+ "c.enumCompareDirection=?1  and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllERIU(EnumCompareDirection enumCompareDirection,
			Pageable pageable);

	@Query(value = "select c From TestCase c  where  "
			+ " c.name=?1 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c  where   "
			+ " c.name=?1 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNRI(String name, Pageable pageable);

	@Query(value = "select c From TestCase c  where  " + " c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c  where   "
			+ " c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllRI(Pageable pageable);

	// 根据3个条件查name,方向，执行状态
	@Query(value = "select c From TestCase c join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2   and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and c.enumCompareDirection=?2   and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNER(String name,
			EnumCompareDirection enumCompareDirection,
			EnumExecuteStatus execState, Pageable pageable);

	// 根据3个条件查name,集合ID，执行状态
	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ "and ts.suiteId=?2 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ " and ts.suiteId=?2 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNDR(String name, Integer suiteId,
			EnumExecuteStatus execState, Pageable pageable);

	// 根据3个条件查方向,集合ID，执行状态
	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.enumCompareDirection=?1 "
			+ "and ts.suiteId=?2 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  c.enumCompareDirection=?1 "
			+ " and ts.suiteId=?2 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?3)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllEDR(EnumCompareDirection enumCompareDirection,
			Integer suiteId, EnumExecuteStatus execState, Pageable pageable);

	// 根据2个条件查询name,状态
	@Query(value = "select c From TestCase c join TestResult r on c.id=r.caseId  where c.name like CONCAT('%',?1,'%')"
			+ "  and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join  TestResult r on c.id=r.caseId  where  c.name like CONCAT('%',?1,'%') "
			+ " and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllNR(String name, EnumExecuteStatus execState,
			Pageable pageable);

	// 根据2个条件查询方向,状态
	@Query(value = "select c From TestCase c join TestResult r on c.id=r.caseId  where c.enumCompareDirection=?1"
			+ "  and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join  TestResult r on c.id=r.caseId  where  c.enumCompareDirection=?1 "
			+ " and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllER(EnumCompareDirection enumCompareDirection,
			EnumExecuteStatus execState, Pageable pageable);

	// 根据2个条件查集合ID，执行状态
	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where  "
			+ " ts.suiteId=?1 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId join TestResult r on c.id=r.caseId  where "
			+ " ts.suiteId=?1 and c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?2)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllDR(Integer suiteId, EnumExecuteStatus execState,
			Pageable pageable);

	// 根据执行状态查
	@Query(value = "select c From TestCase c join TestResult r on c.id=r.caseId  where  "
			+ " c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?1)"
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestResult r on c.id=r.caseId where "
			+ " c.id in "
			+ "(select t.caseId from TestResult t where t.id in(select max(id) as id from TestResult t group by caseId) and t.execState =?1)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllR(EnumExecuteStatus execState, Pageable pageable);

	/**
	 * 根据案例状态查询所有不在本测试集下的案例
	 * 
	 * @param testSuiteID
	 * @param exeState
	 * @param pageable
	 * @return
	 */
	@Query(value = "select C FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId ) and tr.execState = ?2 )", countQuery = "select count(*) FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId not IN (  SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId ) and tr.execState = ?2 )")
	// @Query(value =
	// "select C FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId IN ( SELECT id FROM TestCase WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 )) GROUP BY caseId ) and tr.execState = ?2 )",
	// countQuery =
	// "select count(*) FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId IN ( SELECT id FROM TestCase WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 )) GROUP BY caseId ) and tr.execState = ?2 )")
	// @Query(value =
	// "SELECT * FROM test_case WHERE id IN ( select case_id from test_result where id in ( SELECT max(id) FROM test_result WHERE case_id IN ( SELECT id FROM test_case WHERE id NOT IN ( SELECT case_id FROM test_suite_case WHERE suite_id = ?1 )) GROUP BY case_id ) and exec_state = ?2 )",
	// countQuery =
	// "SELECT count(*) FROM test_case WHERE id IN ( select case_id from test_result where id in ( SELECT max(id) FROM test_result WHERE case_id IN ( SELECT id FROM test_case WHERE id NOT IN ( SELECT case_id FROM test_suite_case WHERE suite_id = ?1 )) GROUP BY case_id ) and exec_state = ?2 )",nativeQuery
	// = true)
	Page<TestCase> findByTestSuiteIsNullAndState(Integer testSuiteID,
			EnumExecuteStatus exeState, Pageable pageable);

	/**
	 * 根据案例状态和案例名称查询所有不在本测试集下的案例
	 * 
	 * @param testSuiteID
	 * @param exeState
	 * @param name
	 * @param pageable
	 * @return
	 */
	@Query(value = "select C FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId IN ( SELECT id FROM TestCase WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 )) GROUP BY caseId ) and tr.execState = ?2 ) and C.name like CONCAT('%',?3,'%')", countQuery = "select count(*) FROM TestCase C WHERE C.id IN ( select tr.caseId as id from TestResult tr where tr.id in ( SELECT max(id) as id FROM TestResult WHERE caseId IN ( SELECT id FROM TestCase WHERE id NOT IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 )) GROUP BY caseId ) and tr.execState = ?2 ) and C.name like CONCAT('%',?3,'%')")
	// @Query(value =
	// "SELECT * FROM test_case WHERE id IN ( select case_id from test_result where id in ( SELECT max(id) FROM test_result WHERE case_id IN ( SELECT id FROM test_case WHERE id NOT IN ( SELECT case_id FROM test_suite_case WHERE suite_id = ?1 )) GROUP BY case_id ORDER BY id DESC) and exec_state = ?2 ) and name like CONCAT('%',?3,'%')",
	// countQuery =
	// "SELECT count(*) FROM test_case WHERE id IN ( select case_id from test_result where id in ( SELECT max(id) FROM test_result WHERE case_id IN ( SELECT id FROM test_case WHERE id NOT IN ( SELECT case_id FROM test_suite_case WHERE suite_id = ?1 )) GROUP BY case_id ORDER BY id DESC) and exec_state = ?2 ) and name like CONCAT('%',?3,'%')",nativeQuery
	// = true)
	Page<TestCase> findByTestSuiteIsNullAndNameState(Integer testSuiteID,
			EnumExecuteStatus exeState, String name, Pageable pageable);

	/**
	 * 初始化时查出所有不在测试集下的案例
	 * 
	 * @param testSuiteID
	 * @param exeState
	 * @param pageable
	 * @return
	 */
	@Query(value = "select C FROM TestCase C WHERE C.id not IN (( SELECT max(caseId) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) ", countQuery = "select count(*) FROM TestCase C WHERE C.id not IN (( SELECT max(caseId) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) ")
	// @Query(value =
	// "select C FROM TestCase C WHERE C.id not IN (( SELECT max(id) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) "
	// , countQuery =
	// "select count(*) FROM TestCase C WHERE C.id not IN (( SELECT max(id) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) ")
	Page<TestCase> findByCaseIsInit(Integer testSuiteID, Pageable pageable);

	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId where  "
			+ " c.enumCompareDirection=?1  and ts.suiteId=?2 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId  where  "
			+ " c.enumCompareDirection=?1  and ts.suiteId=?2 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllEDRI(EnumCompareDirection enumCompareDirection,
			Integer suiteId, Pageable pageable);

	@Query(value = "select c From TestCase c join TestSuiteCase ts on c.id=ts.caseId where  "
			+ "  ts.suiteId=?1 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId) "
			+ " GROUP BY c.id", countQuery = "select count(*) From TestCase c join TestSuiteCase ts on c.id=ts.caseId  where  "
			+ "  ts.suiteId=?1 and c.id not in "
			+ "(select t.caseId from TestResult t group by caseId)"
			+ " GROUP BY c.id  ")
	Page<TestCase> getAllDRI(Integer suiteId, Pageable pageable);

	/**
	 * 初始化时根据案例名称查出所有不在测试集下的案例
	 * 
	 * @param testSuiteID
	 * @param exeState
	 * @param name
	 * @param pageable
	 * @return
	 */
	@Query(value = "select C FROM TestCase C WHERE C.id not IN (( SELECT max(caseId) as id FROM TestResult WHERE caseId NOT IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) and C.name like CONCAT('%',?2,'%')", countQuery = "select count(*) FROM TestCase C WHERE C.id not IN (( SELECT max(caseId) as id FROM TestResult WHERE caseId not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) GROUP BY caseId )) and C.id not IN ( SELECT caseId FROM TestSuiteCase WHERE suiteId = ?1 ) and C.name like CONCAT('%',?2,'%')")
	Page<TestCase> findByCaseIsInitName(Integer testSuiteID, String name,
			Pageable pageable);

}
