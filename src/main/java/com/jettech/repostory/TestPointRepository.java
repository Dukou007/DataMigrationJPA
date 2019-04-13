package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.QualityTestPoint;

public interface TestPointRepository extends JpaRepository<QualityTestPoint,Integer> {
}
