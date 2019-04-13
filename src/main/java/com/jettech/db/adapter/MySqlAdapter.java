package com.jettech.db.adapter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.domain.DbModel;
import com.jettech.domain.JobInfo;
import com.jettech.domain.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataTable;
import com.jettech.util.StringUtil;

/**
 * MySql数据库适配器
 * 
 * @author tan
 *
 */
public class MySqlAdapter extends AbstractAdapter {

	private Logger logger = LoggerFactory.getLogger(MySqlAdapter.class);

	/**
	 * 根据传入的连接和SQL进行查询并返回结果集
	 */
	@Override
	public ResultSet query(Connection conn, String sql) {

		Statement stmt = null;

		ResultSet rs = null;

		try {

			stmt = conn.createStatement();

			rs = stmt.executeQuery(sql);

		} catch (SQLException e) {

			logger.error("query error.sql:" + sql, e);

		}

		return rs;

	}

	@Override
	public List<DataSchema> getDatabase(String databaseName, Connection conn) {
		List<DataSchema> databaselist = new ArrayList<DataSchema>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from information_schema.SCHEMATA");
			if (!databaseName.equals("")) {
				sql.append(" where SCHEMA_NAME='").append(databaseName).append("' ");
			}

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataSchema databaseobj = new DataSchema();
				databaseobj.setName(rs.getString("SCHEMA_NAME"));
				databaselist.add(databaseobj);
				// System.out.println(rs.getString("TABLE_SCHEMA"));
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return databaselist;
	}

	@Override
	public List<DataTable> getTable(String databaseName, Connection conn) {
		List<DataTable> tablelist = new ArrayList<DataTable>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from information_schema.tables");
			sql.append(" where TABLE_SCHEMA='").append(databaseName).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataTable tableobj = new DataTable();
				tableobj.setName(rs.getString("TABLE_NAME"));
//				tableobj.setCreateTime(rs.getTimestamp("CREATE_TIME"));
//				tableobj.setEditTime(rs.getDate("UPDATE_TIME"));
				tablelist.add(tableobj);
				// System.out.println(rs.getString("TABLE_SCHEMA"));
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return tablelist;
	}

	@Override
	public List<com.jettech.entity.DataField> getField(String databaseName, String tableName, Connection conn) {
		List<com.jettech.entity.DataField> fieldlist = new ArrayList<com.jettech.entity.DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from information_schema.columns");
			sql.append(" where TABLE_SCHEMA='").append(databaseName).append("' and TABLE_NAME='").append(tableName)
					.append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			// System.out.println("sql="+sql);
			while (rs.next()) {
				com.jettech.entity.DataField fieldobj = new com.jettech.entity.DataField();
				fieldobj.setTalbeName(rs.getString("TABLE_NAME"));
				fieldobj.setName(rs.getString("COLUMN_NAME"));
				String datatype = rs.getString("DATA_TYPE");
				fieldobj.setDataType(datatype);
				if (rs.getString("IS_NULLABLE").equals("YES")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDataLength(rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
				fieldobj.setDataPrecision(rs.getInt("NUMERIC_PRECISION"));
				if (rs.getString("COLUMN_KEY").equals("PRI")) {
					fieldobj.setIsPrimaryKey(true);
				} else {
					fieldobj.setIsPrimaryKey(false);
				}
				fieldobj.setDes(rs.getString("COLUMN_COMMENT"));
				fieldlist.add(fieldobj);
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public List<com.jettech.entity.DataField> getAllField(String dbName, Connection conn) {
		List<com.jettech.entity.DataField> fieldlist =  new ArrayList<com.jettech.entity.DataField>();;
		Statement stmt;
		try {
			stmt = conn.createStatement();
		StringBuffer sql = new StringBuffer("select * from information_schema.columns");
		sql.append(" where TABLE_SCHEMA='").append(dbName)
				.append("' ");
		logger.info("sql:"+sql);
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				com.jettech.entity.DataField fieldobj = new com.jettech.entity.DataField();
				fieldobj.setTalbeName(rs.getString("TABLE_NAME"));
				fieldobj.setName(rs.getString("COLUMN_NAME"));
				String datatype = rs.getString("DATA_TYPE");
				fieldobj.setDataType(datatype);
				if (rs.getString("IS_NULLABLE").equals("YES")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDataLength(rs.getInt("CHARACTER_MAXIMUM_LENGTH"));
				fieldobj.setDataPrecision(rs.getInt("NUMERIC_PRECISION"));
				if (rs.getString("COLUMN_KEY").equals("PRI")) {
					fieldobj.setIsPrimaryKey(true);
				} else {
					fieldobj.setIsPrimaryKey(false);
				}
				fieldobj.setDes(rs.getString("COLUMN_COMMENT"));
				fieldlist.add(fieldobj);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return fieldlist;
	}

	@Override
	public DataTable getTable(String databaseName, String tableName,
			Connection conn) {
		DataTable tableobj = new DataTable();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from information_schema.tables");
			sql.append(" where TABLE_SCHEMA='").append(databaseName).append("' ");
			sql.append("and TABLE_NAME='").append(tableName).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				tableobj.setName(rs.getString("TABLE_NAME"));
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableobj;
	}

}
