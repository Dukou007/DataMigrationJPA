package com.jettech.vo.sql;

import java.util.List;

public class InsertSqlVo {
	String dbType;
    String table;
    List<Columnslist> columnslist;
    List<Valuelist> valuelist;
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public List<Columnslist> getColumnslist() {
		return columnslist;
	}
	public void setColumnslist(List<Columnslist> columnslist) {
		this.columnslist = columnslist;
	}
	public List<Valuelist> getValuelist() {
		return valuelist;
	}
	public void setValuelist(List<Valuelist> valuelist) {
		this.valuelist = valuelist;
	}
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}


   
}
