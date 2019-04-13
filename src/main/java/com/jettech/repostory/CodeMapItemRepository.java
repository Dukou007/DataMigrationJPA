package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.CodeMapItem;

public interface CodeMapItemRepository extends JpaRepository<CodeMapItem, Integer> {

}