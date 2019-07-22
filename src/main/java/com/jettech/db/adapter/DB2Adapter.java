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

import com.jettech.entity.DataSchema;
import com.jettech.entity.DataField;
import com.jettech.entity.DataTable;

/**
 * MySql数据库适配器
 * 
 * @author tan
 *
 */
public class DB2Adapter extends AbstractAdapter {

	public static String DEFAULT_DRIVER = "com.ibm.db2.jcc.DB2Driver";

	private Logger logger = LoggerFactory.getLogger(DB2Adapter.class);

	@Override
	public List<DataSchema> getDatabase(String databaseName, Connection conn) {
		List<DataSchema> databaselist = new ArrayList<DataSchema>();
		try {
			StringBuffer sql = new StringBuffer("select SCHEMANAME,owner,CREATE_TIME from syscat.schemata");
			if (!databaseName.equals("")) {
				sql.append(" where SCHEMANAME='").append(databaseName).append("' ");
			}
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			// user 为你表的名称

			while (rs.next()) {
				DataSchema databaseobj = new DataSchema();
				databaseobj.setName(rs.getString("SCHEMANAME"));
				databaselist.add(databaseobj);
			}
		} catch (SQLException e) {

		}
		return databaselist;
	}

	@Override
	public List<DataTable> getTable(String databaseName, Connection conn) {
		List<DataTable> tablelist = new ArrayList<DataTable>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select tabname from syscat.tables ");
			sql.append(" where tabschema='").append(databaseName).append("' ");

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataTable tableobj = new DataTable();
				tableobj.setName(rs.getString("tabname"));
				tablelist.add(tableobj);
			}
		} catch (SQLException e) {

		}
		return tablelist;
	}

	@Override
	public List<DataField> getField(String databaseName, String tableName, Connection conn) {
		List<DataField> fieldlist = new ArrayList<DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from SYSCAT.COLUMNS ");
			sql.append(" where TABSCHEMA='").append(databaseName.toUpperCase()).append("' and TABNAME='")
			        .append(tableName.toUpperCase()).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("COLNAME"));
				if (rs.getString("NULLS").equals("Y")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDataLength(rs.getInt("LENGTH"));
				fieldobj.setDes(rs.getString("Remarks"));
				fieldlist.add(fieldobj);
			}

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public List<DataField> getAllField(String dbName, Connection conn) {
		List<DataField> fieldlist = new ArrayList<DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select * from SYSCAT.COLUMNS ");
			sql.append(" where TABSCHEMA='").append(dbName.toUpperCase()).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());
			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("COLNAME"));
				if (rs.getString("NULLS").equals("Y")) {
					fieldobj.setIsNullable(true);
				} else {
					fieldobj.setIsNullable(false);
				}
				fieldobj.setDataLength(rs.getInt("LENGTH"));
				fieldobj.setDes(rs.getString("Remarks"));
				fieldlist.add(fieldobj);
			}

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public DataTable getTable(String databaseName, String tableName, Connection conn) {
		DataTable tableobj = new DataTable();

		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("select tabname from syscat.tables ");
			sql.append(" where tabschema='").append(databaseName).append("' ");
			sql.append("' and TABNAME='").append(tableName.toUpperCase()).append("' ");

			ResultSet rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				tableobj.setName(rs.getString("tabname"));
			}
		} catch (SQLException e) {

		}
		return tableobj;
	}

	@Override
	public Integer getTableCount(String sourceTableName, Connection conn, String schema) {
		return null;
	}
}
