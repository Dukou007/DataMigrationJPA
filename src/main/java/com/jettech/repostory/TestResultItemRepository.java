package com.jettech.repostory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.TestResultItem;
@Repository
public interface TestResultItemRepository extends JpaRepository<TestResultItem, Integer> , JpaSpecificationExecutor<TestResultItem>{

	@Transactional(timeout=30000)
    @Query(value="SELECT * FROM `test_result_item` t WHERE t.test_result_id =?1 AND if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",countQuery="SELECT count(*) FROM `test_result_item` t WHERE t.test_result_id =?1  AND if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",nativeQuery=true)
	Page<TestResultItem> findTestResultItemByTestResultID(Integer testResultID,String result, Pageable pageable);
    @Query(value="SELECT * FROM test_result_item t WHERE t.id=?1",nativeQuery=true)
    TestResultItem findByTestResultItemID(String i);
    
    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM test_result_item t WHERE t.key_value LIKE %?1%",countQuery="SELECT count(*) FROM test_result_item t WHERE t.key_value LIKE %?1%",nativeQuery=true)
	Page<TestResultItem> findTestResultItemLikeKeyValue(String keyValue, Pageable pageable);
    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM test_result_item t WHERE t.result=?1",countQuery="SELECT count(*) FROM test_result_item t WHERE t.result=?1",nativeQuery=true)
	Page<TestResultItem> findTestResultItemByResult(String result, Pageable pageable);
	@Query(value="SELECT * FROM `test_result_item` t WHERE t.test_result_id =?1",nativeQuery=true)
    List<TestResultItem> findByTestResultID(String id);
	@Transactional(timeout=300000)
	@Query(value="SELECT * FROM test_result_item",nativeQuery=true)
	List<TestResultItem> findAllTestResultItem();

    Page<TestResultItem> findBytestResultId(Integer testResultId, Pageable pageable);
    @Transactional(timeout=300000)
	@Query(value="SELECT * FROM `test_result_item` t WHERE t.column_name =?1",countQuery="SELECT count(*) FROM `test_result_item` t WHERE t.column_name =?1",nativeQuery=true)
	Page<TestResultItem> findByColumnName(String columnName, Pageable pageable);
	@Modifying
	@Query(value = "DELETE FROM test_result_item  WHERE test_result_id = ?1", nativeQuery = true)
	void deleteByResultId(int id);
  
    
    
    
}
