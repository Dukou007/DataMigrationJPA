package com.jettech.repostory;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Integer> ,JpaSpecificationExecutor<TestResult>{

    @Query(value ="select * from test_result t where t.case_id =?1",nativeQuery = true)
    Page<TestResult> findTestResultListByCaseId(Integer caseId, Pageable pageable);

    
    @Query(value = "select  t.* from test_result  t LEFT JOIN test_case c on t.case_id =c.id  WHERE  c.name LIKE CONCAT('%',?1,'%')",countQuery="SELECT count(*) from test_result  t LEFT JOIN test_case c on t.case_id =c.id  WHERE  c.name LIKE CONCAT('%',?1,'%')",nativeQuery = true)
	Page<TestResult> findCaseByNameLike(String name,Pageable pageable);
    
    
    
    Page<TestResult> findByCaseId( String caseId, Pageable pageable);
    /**
     * @description:根据roundID查询result并按照开始时间倒序分页
     * @tips:null
     * @param testRoundID
     * @param pageable
     * @return
     * @author:zhou_xiaolong in 2019年3月2日上午9:26:27
     */
    @Query(value="SELECT * FROM test_result t WHERE t.test_round_id=?1 ORDER BY t.start_time",countQuery="SELECT COUNT(*) FROM test_result t WHERE t.test_round_id=?1",nativeQuery=true)
	Page<TestResult> findTestResultByIdOrderStartTime(Integer testRoundID, Pageable pageable);
    /**
     * @description:模糊查询keyvalue
     * @tips:null
     * @param keyValue
     * @param pageable
     * @return
     * @author:zhou_xiaolong in 2019年3月2日上午9:27:02
     */
    @Query(value="SELECT * FROM test_result_item t WHERE 1=1 AND t.key_value LIKE CONCAT('%',?1,'%')",countQuery="SELECT count(*) FROM test_result_item t WHERE 1=1 AND t.key_value CONCAT('%',?1,'%')",nativeQuery=true)
	Page<TestResultItem> findByKeyValue(Integer keyValue, Pageable pageable);
  
    @Query(value="SELECT * FROM test_result t WHERE 1=1 AND t.case_id= ?1 order by id desc",countQuery="SELECT count(*) FROM test_result t WHERE 1=1 AND t.case_id=?1",nativeQuery=true)
	Page<TestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable);

    @Query(value="SELECT * FROM `test_result` tr WHERE  tr.exec_state=?1",countQuery="SELECT count(*) FROM `test_result` tr WHERE  tr.exec_state=?1",nativeQuery=true)
	Page<TestResult> findAllByExecState(String state,Pageable pageable);


    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM test_result tr WHERE tr.case_id=?1 AND tr.source_data LIKE CONCAT('%',?2,'%')",countQuery="SELECT count(*) FROM test_result tr WHERE tr.case_id=?1 AND tr.source_data LIKE CONCAT('%',?2,'%')",nativeQuery=true)
	Page<TestResult> findByCaseIdAndSourceDataSource(String caseId,String dataSource, Pageable pageable);
    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM test_result tr WHERE tr.case_id=?1 AND tr.target_data LIKE CONCAT('%',?2,'%')",countQuery="SELECT count(*) FROM test_result tr WHERE tr.case_id=?1 AND tr.target_data LIKE CONCAT('%',?2,'%')",nativeQuery=true)
	Page<TestResult> findByCaseIdAndTargetDataSource(String caseId,String dataSource, Pageable pageable);
    
    
    @Query(value="select * from test_result where case_id=?1 order by start_time desc,end_time desc limit 1",nativeQuery=true)
	TestResult findEndTimeByCaseId(Integer caseId);

    @Query(value="select exec_state,count(*) as num from test_result where case_id=?1 group by exec_state",nativeQuery=true)
    List<Map<String,Object>>findTestCaseStatus(Integer caseId);
    @Query(value="select id from test_result where case_id=?1 ",nativeQuery=true)
    List<Integer> findByCaseId(Integer caseId);
	List<TestResult> findByTestRoundId(Integer testRoundId);

	@Query(value="select * from test_result  where case_id in (select id from test_case where id=?1) order by create_time desc limit 0,1",nativeQuery=true)
	List<TestResult> findResultByCaseIds(int caseId);








}
