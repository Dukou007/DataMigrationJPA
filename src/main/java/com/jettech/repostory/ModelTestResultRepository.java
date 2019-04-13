package com.jettech.repostory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jettech.entity.ModelTestResult;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;

import org.springframework.data.jpa.repository.Query;


public interface ModelTestResultRepository extends JpaRepository<ModelTestResult, Integer> ,JpaSpecificationExecutor<ModelTestResult>{

    @Query(value ="select * from model_test_result t where t.case_id =?1",nativeQuery = true)
    Page<ModelTestResult> findTestResultListByCaseId(Integer caseId, Pageable pageable);

    Page<ModelTestResult> findByCaseId( String caseId, Pageable pageable);
    /**
     * @description:根据roundID查询result并按照开始时间倒序分页
     * @tips:null
     * @param testRoundID
     * @param pageable
     * @return
     * @author:zhou_xiaolong in 2019年3月2日上午9:26:27
     */
    @Query(value="SELECT * FROM model_test_result t WHERE t.test_round_id=?1 ORDER BY t.start_time",countQuery="SELECT COUNT(*) FROM model_test_result t WHERE t.test_round_id=?1",nativeQuery=true)
	Page<ModelTestResult> findTestResultByIdOrderStartTime(Integer testRoundID, Pageable pageable);
    /**
     * @description:模糊查询keyvalue
     * @tips:null
     * @param keyValue
     * @param pageable
     * @return
     * @author:zhou_xiaolong in 2019年3月2日上午9:27:02
     */
    @Query(value="SELECT * FROM model_test_result_item t WHERE 1=1 AND t.key_value LIKE CONCAT('%',?1,'%')",countQuery="SELECT count(*) FROM model_test_result_item t WHERE 1=1 AND t.key_value CONCAT('%',?1,'%')",nativeQuery=true)
	Page<ModelTestResultItem> findByKeyValue(Integer keyValue, Pageable pageable);
    @Query(value="SELECT * FROM model_test_result t WHERE 1=1 AND t.case_id= ?1",countQuery="SELECT count(*) FROM model_test_result t WHERE 1=1 AND t.case_id=?1",nativeQuery=true)
	Page<ModelTestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable);

}
