package com.jettech.repostory;

import com.jettech.entity.QualityTestResultItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

public interface QualityTestResultItemRepository extends JpaRepository<QualityTestResultItem, Integer> {
	@Transactional(timeout=30000)
    @Query(value="SELECT * FROM `quality_test_result_item` t WHERE t.test_result_id=?1 and if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",countQuery="SELECT count(*) FROM `quality_test_result_item` t WHERE t.test_result_id =?1 and if(t.result!='',t.result LIKE CONCAT('%',?2,'%'), 1=1 )",nativeQuery=true)
	Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID, String result, Pageable pageable);
	@Query(value="SELECT * FROM `quality_test_result_item` t WHERE t.test_result_id=?1",nativeQuery=true)
	ArrayList<QualityTestResultItem> findByTestResultID(String id);

}
