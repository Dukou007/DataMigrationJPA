package com.jettech.db.adapter;

import java.sql.Connection;
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

public class SyBaseAdapter extends AbstractAdapter {

	private Logger logger = LoggerFactory.getLogger(SyBaseAdapter.class);

	@Override
	public List<DataSchema> getDatabase(String databaseName, Connection conn) {

		List<DataSchema> databaselist = new ArrayList<DataSchema>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("SELECT name  FROM master..sysdatabases ");
			if (!databaseName.equals("")) {
				sql.append(" where name='").append(databaseName).append("' ");
			}
			// System.out.println("sql="+sql);
			logger.info("dataBase_sql：" + "【" + sql + "】");
			ResultSet rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				DataSchema databaseobj = new DataSchema();
				databaseobj.setName(rs.getString("name"));
				databaselist.add(databaseobj);
				// System.out.println(rs.getString("TABLE_SCHEMA"));
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

			StringBuffer sql = new StringBuffer("SELECT name FROM sysobjects  WHERE TYPE='U' ");
			ResultSet rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				DataTable tableobj = new DataTable();
				tableobj.setName(rs.getString("name"));

				tablelist.add(tableobj);

			}
			logger.info("tablelist："+"【"+tablelist+"】");
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
					"SELECT a.name a1,a.length a2,  b.name a3, d.name a4 FROM syscolumns a LEFT JOIN systypes b ON a.usertype = b.usertype INNER JOIN sysobjects d ON a.id = d.id AND d.name <> 'dtproperties' LEFT JOIN syscomments e ON a.cdefault = e.id  ");
			// "SELECT a.name, b.name FROM syscolumns a LEFT JOIN systypes b ON a.usertype =
			// b.usertype INNER JOIN sysobjects d ON a.id = d.id AND d.name <>
			// 'dtproperties' LEFT JOIN syscomments e ON a.cdefault = e.id WHERE d.name ="

			sql.append(" WHERE d.name ='").append(tableName.toLowerCase()).append("' ");

			ResultSet rs = stmt.executeQuery(sql.toString());

			logger.info("field_sql：" + "【" + sql + "】");

			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("a1"));

				fieldobj.setDataLength(rs.getInt("a2"));
				fieldobj.setDataType(rs.getString("a3"));
				fieldobj.setTalbeName(rs.getString("a4"));

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
			StringBuffer sql = new StringBuffer(
					"SELECT a.name a1,a.length a2,  b.name a3, d.name a4 FROM syscolumns a LEFT JOIN systypes b ON a.usertype = b.usertype INNER JOIN sysobjects d ON a.id = d.id AND d.name <> 'dtproperties' LEFT JOIN syscomments e ON a.cdefault = e.id  ");

			ResultSet rs = stmt.executeQuery(sql.toString());

			logger.info("field_sql：" + "【" + sql + "】");

			while (rs.next()) {
				DataField fieldobj = new DataField();
				fieldobj.setName(rs.getString("a1"));

				fieldobj.setDataLength(rs.getInt("a2"));
				fieldobj.setDataType(rs.getString("a3"));
				fieldobj.setTalbeName(rs.getString("a4"));

				fieldlist.add(fieldobj);
			}

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public DataTable getTable(String databaseName, String tableName,
			Connection conn) {
		DataTable tableobj = new DataTable();
		try {
			Statement stmt = conn.createStatement();

			StringBuffer sql = new StringBuffer("SELECT name FROM sysobjects  WHERE TYPE='U' ");
			sql.append(" WHERE name ='").append(tableName.toLowerCase()).append("' ");
			ResultSet rs = stmt.executeQuery(sql.toString());

			while (rs.next()) {
				tableobj.setName(rs.getString("name"));
			}
			logger.info("tablelist："+"【"+tableobj+"】");
		} catch (SQLException e) {

		}
		return tableobj;
	}

}
