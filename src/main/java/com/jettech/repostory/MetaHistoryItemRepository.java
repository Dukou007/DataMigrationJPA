package com.jettech.repostory;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.MetaHistoryItem;


public interface MetaHistoryItemRepository extends JpaRepository<MetaHistoryItem, Integer> {
	  @Query(value = "select mhi.* from meta_history mh,meta_history_item mhi,test_database td  where mh.test_database_id=td.id and mh.id=mhi.meta_history_id and td.id =?1 and mhi.table_name =?2 ", countQuery = "SELECT count(*) FROM  meta_history mh,meta_history_item mhi,test_database td  where mh.test_database_id=td.id and mh.id=mhi.meta_history_id and td.id =?1  and mhi.table_name =?2",nativeQuery = true)
	  Page<MetaHistoryItem> findByForeignKeyByPage(int test_database_id,String tableName,Pageable pageable);	
	  @Query(value = "select mhi.* from meta_history mh,meta_history_item mhi,test_database td  where mh.test_database_id=td.id and mh.id=mhi.meta_history_id and td.id =?1 ", countQuery = "SELECT count(*) FROM  meta_history mh,meta_history_item mhi,test_database td  where mh.test_database_id=td.id and mh.id=mhi.meta_history_id and td.id =?1 ",nativeQuery = true)
	  Page<MetaHistoryItem> findByDBByPage(int test_database_id,Pageable pageable);	
	  @Modifying
	  @Transactional
	  @Query(value="delete from meta_history_item  WHERE meta_history_id=?1",nativeQuery = true)
	  int  deletedByForeignKeyID(int id);
}
