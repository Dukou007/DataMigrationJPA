package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jettech.entity.QualityTestQuery;
@Repository
public interface QualityTestQueryRepository extends JpaRepository<QualityTestQuery, Integer>{

}
