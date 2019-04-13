package com.jettech.repostory;


import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.CaseModelSet;
import com.jettech.entity.ModelTestCase;

public interface CaseModelSetRepository extends JpaRepository<CaseModelSet, Integer> {
	
     CaseModelSet findByModelTestCase(ModelTestCase  testCase);
}
