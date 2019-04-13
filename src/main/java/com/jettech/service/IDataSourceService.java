package com.jettech.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.jettech.EnumDatabaseType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.vo.ResultVO;
import com.jettech.vo.SycData;

public interface IDataSourceService extends IService<DataSource, Integer> {

	List<DataSource> findByNameLike(String name);

	DataSource findByName(String name);
	public ResultVO copyDataSource(int id,String name);
	public List<String> getDriver(String type);
	//获取支持的数据库类型  20190326
	List<String> selectDataSourceType();
	public String GetMetaData(Integer dataSourceId) throws Exception;
	public SycData getAdapterAndConnection(EnumDatabaseType dbType,
			String driver, String url, String port, String host,
			String username, String pwd, String sid);
	public String syncTableAndFiled(AbstractAdapter adapter, Connection conn,
			DataSchema dataSchema);
	public String syncOneTableAndFiled(AbstractAdapter adapter,
			Connection conn, DataTable exitsDataTable);
	public void delOneDatasource(int data_source_id) throws SQLException;

	public boolean GetDBLink(DataSource ds) throws SQLException;
}
