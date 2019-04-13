package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.MapperEntity;

public interface MapperEntityRepository extends JpaRepository<MapperEntity, Integer> {

}
