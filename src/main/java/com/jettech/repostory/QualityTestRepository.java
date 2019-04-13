package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.QualityTestResultItem;

public interface QualityTestRepository extends JpaRepository<QualityTestResultItem, Integer>{

}
