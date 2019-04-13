package com.jettech.vo;

import java.sql.Connection;
import java.util.List;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;

public class SycData {
	private DataSource dataSource;
	private Connection conn;
	private AbstractAdapter adapter;
	private String dbName;
	private List<DataTable> dataModelList;
	private DataSchema dataSchema;
    private String result;
	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public AbstractAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(AbstractAdapter adapter) {
		this.adapter = adapter;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public List<DataTable> getDataModelList() {
		return dataModelList;
	}

	public void setDataModelList(List<DataTable> dataModelList) {
		this.dataModelList = dataModelList;
	}

	public DataSchema getDataSchema() {
		return dataSchema;
	}

	public void setDataSchema(DataSchema dataSchema) {
		this.dataSchema = dataSchema;
	}

	

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public SycData(DataSource dataSource, Connection conn,
			AbstractAdapter adapter, String dbName,
			List<DataTable> dataModelList, DataSchema testDatabase,
			String result) {
		super();
		this.dataSource = dataSource;
		this.conn = conn;
		this.adapter = adapter;
		this.dbName = dbName;
		this.dataModelList = dataModelList;
		this.dataSchema = dataSchema;
		this.result = result;
	}

	public SycData() {
		super();
	}

}
