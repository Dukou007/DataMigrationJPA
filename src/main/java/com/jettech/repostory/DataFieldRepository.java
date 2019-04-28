package com.jettech.repostory;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.xmlbeans.impl.jam.JParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
@Repository
public interface DataFieldRepository extends JpaRepository<DataField, Integer> {

	@Query("select d from DataField AS d where d.name = ?1 and d.talbeName =?2")
	DataField findByNameAndTableName(String name, String talbeName);

	@Query(value = "select * from test_field  where test_table_id =?1", nativeQuery = true)
	List<DataField> findByForeignKey(int test_table_id);
	

	@Query(value = "select * from test_field  where test_table_id =?1 and if(?2 is null or ?2 = '',1=1,name like concat('%',?2,'%'))", nativeQuery = true)
	List<DataField> findFieldNameByTableID(int test_table_id,String name);

	@Query(value = "select t.* from test_field t,test_table b,test_database c where t.test_table_id =b.id and c.id=b.test_database_id and b.name=?1 and c.name=?2", nativeQuery = true)
	List<DataField> findByTBName(String name, String dbname);

	@Query(value = "select * from test_field  where talbe_name=?1", nativeQuery = true)
	List<DataField> findByTableName(String name);

	@Modifying
	@Transactional
	@Query(value = "UPDATE DataField t SET t.version=?1 WHERE t.id=?2")
	int update(int version, int id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE DataField t SET t.deleted=?1 WHERE t.id=?2")
	int deletedByID(boolean deleted, int id);

	@Modifying
	@Transactional
	@Query("delete from DataField  where id = ?1")
	int delById(int id);
	@Modifying
	@Transactional
	@Query(value = "UPDATE DataField " + "t " + "SET t.createTime=?1," + "t.createUser=?2," + "t.editTime=?3,"
			+ "t.editUser=?4," + "t.dataLength=?5," + "t.dataPrecision=?6," + "t.dataType=?7," + "t.des=?8,"
			+ "t.isForeignKey=?9," + "t.isIndex=?10," + "t.isNullable=?11," + "t.isPrimaryKey=?12," + "t.name=?13,"
			+ "t.talbeName=?14,"
//	  		+ "t.testTable=?15,"
//	  		+ "t.testQueryFields=?16,"
			+ "t.deleted=?15," + "t.version=?16 " + "WHERE t.id=?17")
	int updateOneByID(Date createTime, String createUser, Date editTime, String editUser, int dataLength,
			int dataPrecision, String dataType, String des, boolean isForeignKey, boolean isIndex, boolean isNullable,
			boolean isPrimaryKey, String name, String talbeName, boolean deleted, int version, int id);

	@Query(value = "select * from test_field  where test_table_id =?1", countQuery = "SELECT count(*) FROM test_field  where test_table_id =?1", nativeQuery = true)
	Page<DataField> findByForeignKeyByPage(int test_table_id, Pageable pageable);

	@Query(value = "SELECT * FROM test_field t WHERE t.`name`=?1", nativeQuery = true)
	DataField findByFieldName(String name);
	@Query(value="SELECT * FROM `test_field` t WHERE t.quality_test_query_id =?1",nativeQuery=true)
	List<DataField> findAllByTestQueryId(Integer id);
	@Query("select d from DataField d where d.name = ?1")
	DataField findByName(String name);
}
