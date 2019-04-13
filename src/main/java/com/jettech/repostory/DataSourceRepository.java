package com.jettech.repostory;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.EnumDatabaseType;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;

public interface DataSourceRepository extends JpaRepository<DataSource, Integer> {

	@Query("select d from DataSource d where d.name = ?1")
	DataSource findByName(String name);

	@Query("select d from DataSource d where d.name like %?1%")
	List<DataSource> findByNameLike(String name);

	@Modifying
	@Transactional
	@Query(value = "UPDATE DataSource t SET t.name=?1,t.databaseType=?2,t.databaseVersion=?3,t.host=?4,t.userName=?5,t.password=?6,t.characterSet=?7,t.defaultSchema=?8,t.driver=?9,t.url=?10,t.port=?11,t.editTime=?12,t.editUser=?13,t.sid=?14   WHERE t.id=?15")
	int update(String name, EnumDatabaseType databaseType, String databaseVersion, String host, String userName,
	        String password, String characterSet, String defaultSchema, String driver, String url, String port,
	        Date editTime,String editUser,String sid,int id);
	
    @Query(value = "select * from data_source  where name  like CONCAT('%',?1,'%') ", countQuery = "select count(*) from data_source  where  name like CONCAT('%',?1,'%') ",nativeQuery = true)
	Page<DataSource> findAllDSNameByPage(String dataSource,Pageable pageable);

}
