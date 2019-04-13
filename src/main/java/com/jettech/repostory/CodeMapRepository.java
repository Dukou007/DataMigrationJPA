package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.CodeMap;

public interface CodeMapRepository extends JpaRepository<CodeMap, Integer> {

}