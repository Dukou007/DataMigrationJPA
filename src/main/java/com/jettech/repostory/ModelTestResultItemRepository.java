package com.jettech.repostory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;
@Repository
public interface ModelTestResultItemRepository extends JpaRepository<ModelTestResultItem, Integer> , JpaSpecificationExecutor<ModelTestResultItem>{

	@Transactional(timeout=30000)
    @Query(value="SELECT * FROM `model_test_result_item` t WHERE t.test_result_id =?1 AND if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",countQuery="SELECT count(*) FROM `model_test_result_item` t WHERE t.test_result_id =?1 AND if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",nativeQuery=true)
	Page<ModelTestResultItem> findTestResultItemByTestResultID(Integer testResultID, String result, Pageable pageable);
    @Query(value="SELECT * FROM model_test_result_item t WHERE t.id=?1",nativeQuery=true)
    ModelTestResultItem findByTestResultItemID(String i);
    
    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM model_test_result_item t WHERE test_result_id=?2 and  t.key_value LIKE %?1%",countQuery="SELECT count(*) FROM model_test_result_item t WHERE test_result_id=?2 and t.key_value LIKE %?1%",nativeQuery=true)
	Page<ModelTestResultItem> findTestResultItemLikeKeyValue(String keyValue,String testResultId, Pageable pageable);
    @Transactional(timeout=30000)
    @Query(value="SELECT * FROM model_test_result_item t WHERE t.result=?1",countQuery="SELECT count(*) FROM model_test_result_item t WHERE t.result=?1",nativeQuery=true)
	Page<ModelTestResultItem> findTestResultItemByResult(String result, Pageable pageable);
	@Query(value="SELECT * FROM `model_test_result_item` t WHERE t.test_result_id =?1",nativeQuery=true)
    List<ModelTestResultItem> findByTestResultID(String id);
	@Transactional(timeout=300000)
	@Query(value="SELECT * FROM model_test_result_item",nativeQuery=true)
	List<ModelTestResultItem> findAllTestResultItem();

    Page<ModelTestResultItem> findBytestResultId(Integer testResultId, Pageable pageable);
}
