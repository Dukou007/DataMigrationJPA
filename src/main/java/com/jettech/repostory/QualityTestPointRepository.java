package com.jettech.repostory;

import com.jettech.entity.QualityTestPoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualityTestPointRepository extends JpaRepository<QualityTestPoint,Integer> {
}
