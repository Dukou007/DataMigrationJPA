package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.jettech.entity.QualityTestResult;
@Repository
public interface QualityTestResultRepository extends JpaRepository<QualityTestResult, Integer> ,JpaSpecificationExecutor<QualityTestResult>{


	

}
