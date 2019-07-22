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

import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataField;

/**
 * MySql数据库适配器
 * 
 * @author tan
 *
 */
public class OracleAdapter extends AbstractAdapter {
	public static String DEFAULT_DRIVER = "oracle.jdbc.driver.OracleDriver";
	private Logger logger = LoggerFactory.getLogger(OracleAdapter.class);

	@Override
	public List<DataSchema> getDatabase(String databaseName, Connection conn) {
		List<DataSchema> databaselist = new ArrayList<DataSchema>();
		try {

			StringBuffer sql = new StringBuffer("select * from all_users");
			if (databaseName != null && !databaseName.equals("")) {
				sql.append(" where  USERNAME='").append(databaseName.toUpperCase()).append("' ");
			}

			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			// user 为你表的名称

			while (rs.next()) {
				DataSchema databaseobj = new DataSchema();
				databaseobj.setName(rs.getString("USERNAME"));
				databaselist.add(databaseobj);
			}
			rs.close();
			pstmt.close();

		} catch (SQLException e) {

		}
		return databaselist;
	}

	@Override
	public List<DataTable> getTable(String databaseName, Connection conn) {
		List<DataTable> tablelist = new ArrayList<DataTable>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select  table_name from all_tables");
			sql.append(" where owner='").append(databaseName.toUpperCase()).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataTable tableobj = new DataTable();
				tableobj.setName(rs.getString("Table_Name"));
				// tableobj.setCreateTime(rs.getTimestamp("CREATE_TIME"));
				// tableobj.setEditTime(rs.getDate("UPDATE_TIME"));
				tablelist.add(tableobj);
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return tablelist;
	}

	@Override
	public List<DataField> getField(String databaseName, String tableName, Connection conn) {
		List<DataField> fieldlist = new ArrayList<DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer(
			        "select  * " + "from (select a.owner r0,a.table_name r1,a.column_name r2,a.comments r3 "
			                + "from all_col_comments a),(select t.owner r4,t.table_name r5,t.column_name  r6 ,t.* from all_tab_columns t) "
			                + "where r4=r0 and r5=r1 and r6=r2 ");
			sql.append(" and owner='").append(databaseName.toUpperCase()).append("' and TABLE_NAME='").append(tableName)
			        .append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("COLUMN_NAME"));
				String datatype = rs.getString("DATA_TYPE");
				fieldobj.setDataType(datatype);
				fieldobj.setTalbeName(rs.getString("TABLE_NAME"));
				if (rs.getString("NULLABLE").equals("Y")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDataLength(rs.getInt("data_length"));
				fieldobj.setDataPrecision(rs.getInt("data_PRECISION"));
				// if(rs.getString("COLUMN_KEY").equals("PRI")) {
				// fieldobj.setIsPrimaryKey(true);
				// }else {
				// fieldobj.setIsPrimaryKey(false);
				// }
				fieldobj.setDes(rs.getString("r3"));
				fieldlist.add(fieldobj);
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public List<DataField> getAllField(String dbName, Connection conn) {
		List<DataField> fieldlist = new ArrayList<DataField>();
		try {
			// 获取当前用户
			Statement stmt = conn.createStatement();
			// select
			// a.table_name,a.column_name,a.data_type,a.data_length,a.data_precision,a.data_scale,a.nullable,a.column_id,b.comments
			// from all_tab_columns a left join all_col_comments b on
			// a.table_name=b.table_name and a.column_name=b.column_name
			// where a.owner='dquser' and b.owner ='dquser';
			StringBuffer sql = new StringBuffer();
			// StringBuffer sql = new StringBuffer(
			// "select * " + "from (select a.owner r0,a.table_name
			// r1,a.column_name r2,a.comments r3 "
			// + " from all_col_comments a),(select t.owner r4,t.table_name
			// r5,t.column_name r6 ,t.* from all_tab_columns t) "
			// + "where r4=r0 and r5=r1 and r6=r2 ");
			// sql.append(" and owner='").append(dbName.toUpperCase()).append("'
			// ");

			sql.append("select a.table_name,a.column_name,a.data_type,a.data_length,a.data_precision,a.data_scale");
			sql.append(",a.nullable,a.column_id,b.comments");
			sql.append(" from all_tab_columns a left join all_col_comments b");
			sql.append(" on a.table_name=b.table_name and a.column_name=b.column_name");
			sql.append(" where a.owner='" + dbName + "' and b.owner ='" + dbName + "'");

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("column_name"));
				String datatype = rs.getString("data_type");
				fieldobj.setDataType(datatype);
				fieldobj.setTalbeName(rs.getString("table_name"));
				if (datatype.equals("NUMBER")) {
					fieldobj.setDataLength(rs.getInt("data_precision"));
				} else {
					fieldobj.setDataLength(rs.getInt("data_length"));
				}
				fieldobj.setDataPrecision(rs.getInt("data_precision"));
				if (rs.getString("nullable").equals("Y")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDes(rs.getString("comments"));
				fieldobj.setFieldId(rs.getInt("column_id"));
				fieldlist.add(fieldobj);
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public DataTable getTable(String databaseName, String tableName, Connection conn) {
		DataTable tableobj = new DataTable();

		try {
			Statement stmt = conn.createStatement();

			StringBuffer sql = new StringBuffer("select  table_name from all_Tables");
			sql.append(" where owner='").append(databaseName.toUpperCase()).append("' ");
			sql.append(" and table_name='").append(tableName).append("' ");

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {

				tableobj.setName(rs.getString("Table_Name"));
			}
			rs.close();
			stmt.close();

		} catch (SQLException e) {

		}
		return tableobj;
	}

	@Override
	public Integer getTableCount(String sourceTableName, Connection conn, String schema) {
		return null;
	}
}
