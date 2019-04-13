package com.jettech.repostory;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.MetaHistory;

public interface MetaHistoryRepository extends JpaRepository<MetaHistory, Integer> {
	 @Modifying
	  @Transactional
	  @Query(value="delete from meta_history   WHERE test_database_id=?1",nativeQuery = true)
	  int  deletedByForeignKeyID(int id);
	 @Modifying
	  @Transactional
	  @Query(value="select * from meta_history t  WHERE t.test_database_id=?1",nativeQuery = true)
	 List<MetaHistory>  findByForeignKeyID(int id);
}
