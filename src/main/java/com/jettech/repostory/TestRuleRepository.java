package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.Product;
import com.jettech.entity.TestRule;

public interface TestRuleRepository extends JpaRepository<TestRule, Integer> {

}
