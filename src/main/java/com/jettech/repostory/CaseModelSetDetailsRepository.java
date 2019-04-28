package com.jettech.repostory;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.CaseModelSet;
import com.jettech.entity.CaseModelSetDetails;
import com.jettech.entity.DataTable;

public interface CaseModelSetDetailsRepository extends JpaRepository<CaseModelSetDetails, Integer> {
	
    List<CaseModelSetDetails> findByCaseModelSet(CaseModelSet caseModelSet);
    @Modifying
	@Transactional
	@Query(value = "delete from  case_model_set_details where datum_model_table_id=?1 or test_model_table_id=?2", nativeQuery = true)
    int deleteBydatumTabOrTestTab(int datumModelSetTableId,int testModelSetTableId);
}
