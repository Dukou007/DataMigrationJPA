package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.jettech.entity.QualitySuite;

public interface QualitySuiteRepository extends JpaRepository<QualitySuite,Integer>, JpaSpecificationExecutor<QualitySuite> {
}
