package com.jettech.repostory;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.CaseModelSet;
import com.jettech.entity.CaseModelSetDetails;

public interface CaseModelSetDetailsRepository extends JpaRepository<CaseModelSetDetails, Integer> {
	
    List<CaseModelSetDetails> findByCaseModelSet(CaseModelSet caseModelSet);
}
