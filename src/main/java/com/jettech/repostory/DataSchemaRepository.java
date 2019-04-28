package com.jettech.repostory;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.DataSchema;
import com.jettech.entity.DataTable;
import com.jettech.vo.TestDatabaseVO;

public interface DataSchemaRepository extends JpaRepository<DataSchema, Integer> {
	  @Query("select d from DataSchema d where d.name = ?1")
	  DataSchema findByName(String name);
	  @Query(value = "select * from test_database where name = ?1 and data_source_id=?2",nativeQuery = true)
	  DataSchema findByNameAndDataSourceId(String name,Integer data_source_id);
	  @Query(value = "select * from test_database where name = ?1 and data_source_id is null",nativeQuery = true)
	  DataSchema findByNameAndDataSourceIdIsNull(String name);
	  @Query("select name from DataSchema d where d.id = ?1")
	  String findName(int id);
	  @Query(value = "select * from test_database  where data_source_id =?1",nativeQuery = true)
	  List<DataSchema> findByForeignKey(int data_source_id);
	  @Query(value = "select * from test_database  where data_source_id =?1", countQuery = "select count(*) from test_database  where data_source_id =?1",nativeQuery = true)
      Page<DataSchema> findByForeignKeyByPage(int data_source_id,Pageable pageable);
	  
	  @Query(value = "select * from test_database where name  like CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_database  where name  like CONCAT('%',?1,'%')  ",nativeQuery = true)
      Page<DataSchema> findAllDbByPage(String DBName,Pageable pageable);
	  @Modifying
	  @Transactional
	  @Query(value="UPDATE DataSchema t SET t.version=?1 WHERE t.id=?2")
	  int  update(int version,int id);	
	  @Modifying
	  @Transactional
	  @Query(value="UPDATE test_database t SET t.version=?1, t.is_dict=?3, t.name=?4 ,t.data_source_id=?5 WHERE t.id=?2",nativeQuery = true)
	  int  update(int version,int id,Boolean is_dict,String name ,int data_source_id);	
	  
	  @Query(value = "select * from test_database  where data_source_id=?1 and name=?2",nativeQuery = true)
	  List<DataSchema> findSchemasByDataSourceID(int dataSourceID,String schemaName);
	  
}
