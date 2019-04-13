package com.jettech.repostory;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.MainSqlRecord;

public interface MainSqlRecordRepository extends JpaRepository<MainSqlRecord, Integer> {

}
