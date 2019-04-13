package com.jettech.repostory;


import java.util.Date;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.EnumFileType;
import com.jettech.entity.DataSource;
import com.jettech.entity.FileDataSource;

public interface FileDataSourceRepository extends JpaRepository<FileDataSource, Integer> {
	 @Query(value = "select * from file_data_source  where  name  like CONCAT('%',?1,'%') ", countQuery = "select count(*) from file_data_source  where name like CONCAT('%',?1,'%') ",nativeQuery = true)
	  Page<FileDataSource> findByNameByPage(String name,Pageable pageable);
	  @Modifying
	  @Transactional
	  @Query(value="UPDATE FileDataSource t "
	  		+ "SET t.version=?1,t.characterSet=?3,t.connectionType=?4,t.editTime=?5,t.editUser=?6,t.filePath=?7,"
	  		+"t.fileType=?8,t.host=?9,t.name=?10,t.pageSize=?11,t.password=?12,t.usePage=?13,t.userName=?14  WHERE t.id=?2")
	  int  update(int version,int id,String characterSet,int connectionType,Date editTime,String editUser,String filePath,
			  EnumFileType fileType,String host,String name,int pageSize,String password,boolean usePage,String userName);

	  @Query("select d from FileDataSource d where d.name = ?1")
	  FileDataSource findByName(String name);
}
