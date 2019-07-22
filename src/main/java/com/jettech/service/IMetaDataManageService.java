package com.jettech.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.MainSqlRecord;
import com.jettech.entity.DataField;
import com.jettech.vo.ResultVO;
import com.jettech.vo.sql.SqlVo;

public interface IMetaDataManageService{
	public void addOneDatasource(DataSource ds) throws SQLException;
	public void updateOneDatasource(DataSource ds) throws SQLException;
	public List<DataSource> getAllDatasource() throws SQLException;
	public Page<DataSource> findAllPage(int pageNum, int pageSize);
	public Page<DataSource> getAllDSByPage(String dataSourceName,Pageable pageable)throws SQLException;
	public List<DataSchema> getAllDb(int data_source_id) throws SQLException;
	public Page<DataSchema> getAllDbByPage(int data_source_id,Pageable pageable) throws SQLException;	
	public Page<DataSchema> getAllDatabaseByPage(String DBName,Pageable pageable) throws SQLException;
	public List<DataTable> getAllTable(int test_database_id);
	public Page<DataTable> getAllTableByPage(int test_database_id,Pageable pageable) throws SQLException;
	public Page<DataTable> getSelTableByPage(int test_database_id,String tableName,Pageable pageable) throws SQLException;
	public List<DataField> getAllField(int test_table_id) throws SQLException;
	public Page<DataField> getAllFieldByPage(int test_table_id,Pageable pageable) throws SQLException ;
	public void uploadDictExcel(MultipartFile file) throws Exception;
	public ResultVO GetSqlDBLink(Integer dataSourceId,String sqlTest) throws SQLException;
	public void compareDictAndModel(int db1,int db2) throws SQLException;
    public void GetSql(SqlVo sqlvo) throws SQLException;
	public SqlVo parseSql(MainSqlRecord  m)throws SQLException;
	public void syncSchemaMetaExistTable(AbstractAdapter adapter, Connection conn, DataSource ds,DataSchema dataSchema);

}
