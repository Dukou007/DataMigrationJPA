package com.jettech.sqlbuilder;

import java.sql.SQLException;
import java.util.List;

import com.jettech.vo.sql.Sourceselectsql;

public interface ISqlBuilerAdapter {
	public String insertBuilder (String table,List<String> columnslist,List<String> valuelist)throws SQLException;
    //public String selectBuilder (List<String> selectlist,List<String> fromlist,boolean distinct,List<String> grouplist,List<String> havinglist,List<String> joinlist,List<String> leftjoinlist,List<String> wherelist,List<String> orderbylist)throws SQLException;
    
	public String selectBuilder (List<Sourceselectsql> selectsql)throws SQLException;
	public String deleteBuilder (String table,List<String> wherelist)throws SQLException;
    public String updateBuilder (String table,List<String> wherelist)throws SQLException;
}
