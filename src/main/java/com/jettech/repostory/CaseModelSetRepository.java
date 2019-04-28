package com.jettech.repostory;


import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.CaseModelSet;
import com.jettech.entity.ModelTestCase;

public interface CaseModelSetRepository extends JpaRepository<CaseModelSet, Integer> {
	
     CaseModelSet findByModelTestCase(ModelTestCase  testCase);
     @Modifying
 	@Transactional
 	@Query(value = "delete from  case_model_set where test_model_set_id=?1 or datum_model_set_id=?2", nativeQuery = true)
     int deleteByTestOrDatumId(int testModelSetId,int datumModelSetId);

}
