package com.jettech.repostory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;

import org.springframework.data.jpa.repository.Query;


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

/*    @Query(value="SELECT * FROM `test_result` tr WHERE  tr.exec_state=?1",countQuery="SELECT count(*) FROM `test_result` tr WHERE  tr.exec_state=?1",nativeQuery=true)
	Page<TestResult> findAllByExecState(String state);*/


	
 
}
