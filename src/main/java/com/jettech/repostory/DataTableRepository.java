package com.jettech.repostory;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.jettech.entity.DataTable;

public interface DataTableRepository extends JpaRepository<DataTable, Integer> {
	@Query("select d from DataTable d where d.name = ?1")
	DataTable findByName(String name);

	// add
	@Query(value = "select t.* from test_table t,test_database db where t.test_database_id= db.id and t.name = ?1 and  db.name=?2", nativeQuery = true)
	DataTable findByNameAndDBName(String name, String dbname);

	@Query(value = "select * from test_table  where test_database_id =?1", nativeQuery = true)
	List<DataTable> findByForeignKey(int test_database_id);

	@Query(value = "select * from test_table  where test_database_id =?1 and if(?2 is null or ?2 = '',1=1,name like concat('%',?2,'%'))", nativeQuery = true)
	List<DataTable> findTablesBySchemaID(int test_database_id, String name);

	@Query(value = "select * from test_table  where test_database_id =?1", countQuery = "select count(*) from test_table  where test_database_id =?1", nativeQuery = true)
	Page<DataTable> findByForeignKeyByPage(int test_database_id,
			Pageable pageable);

	@Query(value = "select * from test_table  where test_database_id =?1 and name  like CONCAT('%',?2,'%') ", countQuery = "select count(*) from test_table  where test_database_id =?1 and name like CONCAT('%',?2,'%') ", nativeQuery = true)
	Page<DataTable> findByForeignKeyAndTableByPage(int test_database_id,
			String tableName, Pageable pageable);

	@Query(value = "select t.* from test_table t,test_database db where t.test_database_id= db.id and db.name=?1", nativeQuery = true)
	List<DataTable> findByDbName(String name);

	@Modifying
	@Transactional
	@Query(value = "UPDATE DataTable t SET t.version=?1 WHERE t.id=?2")
	int update(int version, int id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE DataTable t SET t.deleted=?1 WHERE t.id=?2")
	int deletedByID(boolean deleted, int id);

	@Modifying
	@Transactional
	@Query("delete from DataTable  where id = ?1")
	int delById(int id);
}
