package com.jettech.sqlbuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jettech.vo.sql.Sourceselectsql;

import ca.krasnay.sqlbuilder.DeleteBuilder;
import ca.krasnay.sqlbuilder.InsertBuilder;
import ca.krasnay.sqlbuilder.SelectBuilder;
import ca.krasnay.sqlbuilder.UpdateBuilder;

public class MysqlBuilderAdapter implements ISqlBuilerAdapter {
    public String insertBuilder (String table,List<String> columnslist,List<String> valuelist)throws SQLException
    {    		
    	InsertBuilder insert=new InsertBuilder(table);  
    	if(columnslist!=null&&!columnslist.isEmpty()&&!columnslist.equals("")&&columnslist.size()!=0) {
	    	for(int i=0;i<columnslist.size();i++) 
	    	{
	    		insert.set(columnslist.get(i), valuelist.get(i));
	        }
    	}
    	return insert.toString();
    }
    public String selectBuilder (List<Sourceselectsql> selectsql)throws SQLException
    {
    	
    	SelectBuilder select=new SelectBuilder();
    	
        List<String> selectlist=new ArrayList<String>();	
        Sourceselectsql object=selectsql.get(0);
        if(selectsql!=null&&object!=null&&object.getSelectlist()!=null) {
            for(int i=0;i<object.getSelectlist().size();i++) {
            	selectlist.add(object.getSelectlist().get(i).getItem());
            }
        }
    	if(selectlist!=null&&!selectlist.isEmpty()&&!selectlist.equals("")&&selectlist.size()!=0) {
	    	for(int i=0;i<selectlist.size();i++) 
	    	{
	    		select.column(selectlist.get(i));
	    	}
    	}
    	boolean distinct=object.isDistinct();
    	if(distinct) {
    		select.distinct();
    	}
        List<String> fromlist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getFromlist()!=null) {		                     
            for(int i=0;i<object.getFromlist().size();i++) {
            	fromlist.add(object.getFromlist().get(i).getItem());
            }
        }
    	if(fromlist!=null&&!fromlist.isEmpty()&&!fromlist.equals("")&&fromlist.size()!=0) {
        	for(int i=0;i<fromlist.size();i++) 
        	{
        		select.from(fromlist.get(i));
        	}
    	}
    	
        List<String> grouplist=new ArrayList<String>();	

        if(selectsql!=null&&object!=null&&object.getGrouplist()!=null) {		                        
            for(int i=0;i<object.getGrouplist().size();i++) {
            	grouplist.add(object.getGrouplist().get(i).getItem());
            }
        }
 
    	if(grouplist!=null&&!grouplist.isEmpty()&&!grouplist.equals("")&&grouplist.size()!=0) {
        	for(int i=0;i<grouplist.size();i++) 
        	{
        		select.groupBy(grouplist.get(i));
        	}
    	}
 
        List<String> havinglist=new ArrayList<String>();	
        if(selectsql!=null&&object!=null&&object.getHavinglist()!=null) {		                        
            for(int i=0;i<object.getHavinglist().size();i++) {
            	havinglist.add(object.getHavinglist().get(i).getItem());
            }
        }

    	if(havinglist!=null&&!havinglist.isEmpty()&&!havinglist.equals("")&&havinglist.size()!=0) {
        	for(int i=0;i<havinglist.size();i++) 
        	{
        		select.having(havinglist.get(i));
        	}
    	}
        List<String> joinlist=new ArrayList<String>();	
        if(selectsql!=null&&object!=null&&object.getJoinlist()!=null) {		                        
            for(int i=0;i<object.getJoinlist().size();i++) {
            	joinlist.add(object.getJoinlist().get(i).getItem());
            }
        }
    	if(joinlist!=null&&!joinlist.isEmpty()&&!joinlist.equals("")&&joinlist.size()!=0) {
        	for(int i=0;i<joinlist.size();i++) 
        	{
        		select.join(joinlist.get(i));
        	}
    	}
        List<String> leftjoinlist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getLeftjoinlist()!=null) {		                      
            for(int i=0;i<object.getLeftjoinlist().size();i++) {
            	leftjoinlist.add(object.getLeftjoinlist().get(i).getItem());
            }
        }
    	if(leftjoinlist!=null&&!leftjoinlist.isEmpty()&&!leftjoinlist.equals("")&&leftjoinlist.size()!=0) {
        	for(int i=0;i<leftjoinlist.size();i++) 
        	{
        		select.leftJoin(leftjoinlist.get(i));
        	}
    	}
    	
        List<String> wherelist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getWherelist()!=null) {		               
            for(int i=0;i<object.getWherelist().size();i++) {
            	wherelist.add(object.getWherelist().get(i).getItem());
            }
        }
    	if(wherelist!=null&&!wherelist.isEmpty()&&!wherelist.equals("")&&wherelist.size()!=0) {
        	for(int i=0;i<wherelist.size();i++) 
        	{
        		select.where(wherelist.get(i));
        	}
    	}
        List<String> orderbylist=new ArrayList<String>();	 
        if(selectsql!=null&&object!=null&&object.getOrderbylist()!=null) {		                
            for(int i=0;i<object.getOrderbylist().size();i++) {
            	orderbylist.add(object.getOrderbylist().get(i).getItem());
            }
        }
    	if(orderbylist!=null&&!orderbylist.isEmpty()&&!orderbylist.equals("")&&orderbylist.size()!=0) {
        	for(int i=0;i<orderbylist.size();i++) 
        	{
    		   select.orderBy(orderbylist.get(i));
        	}
    	}
    	
    	for(int i=1;i<selectsql.size();i++) {
    	        SelectBuilder  unionBuilder=selectBuilder_union (selectsql.get(i));
    			select.union(unionBuilder);
    	}
    	return select.toString();
    }

    public SelectBuilder selectBuilder_union (Sourceselectsql selectsql)throws SQLException
    {
    	
    	SelectBuilder select=new SelectBuilder();
    	
        List<String> selectlist=new ArrayList<String>();	
        Sourceselectsql object=selectsql;
        if(selectsql!=null&&object!=null&&object.getSelectlist()!=null) {
            for(int i=0;i<object.getSelectlist().size();i++) {
            	selectlist.add(object.getSelectlist().get(i).getItem());
            }
        }
    	if(selectlist!=null&&!selectlist.isEmpty()&&!selectlist.equals("")&&selectlist.size()!=0) {
	    	for(int i=0;i<selectlist.size();i++) 
	    	{
	    		select.column(selectlist.get(i));
	    	}
    	}
    	boolean distinct=object.isDistinct();
    	if(distinct) {
    		select.distinct();
    	}
        List<String> fromlist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getFromlist()!=null) {		                     
            for(int i=0;i<object.getFromlist().size();i++) {
            	fromlist.add(object.getFromlist().get(i).getItem());
            }
        }
    	if(fromlist!=null&&!fromlist.isEmpty()&&!fromlist.equals("")&&fromlist.size()!=0) {
        	for(int i=0;i<fromlist.size();i++) 
        	{
        		select.from(fromlist.get(i));
        	}
    	}
    	
        List<String> grouplist=new ArrayList<String>();	

        if(selectsql!=null&&object!=null&&object.getGrouplist()!=null) {		                        
            for(int i=0;i<object.getGrouplist().size();i++) {
            	grouplist.add(object.getGrouplist().get(i).getItem());
            }
        }
 
    	if(grouplist!=null&&!grouplist.isEmpty()&&!grouplist.equals("")&&grouplist.size()!=0) {
        	for(int i=0;i<grouplist.size();i++) 
        	{
        		select.groupBy(grouplist.get(i));
        	}
    	}
 
        List<String> havinglist=new ArrayList<String>();	
        if(selectsql!=null&&object!=null&&object.getHavinglist()!=null) {		                        
            for(int i=0;i<object.getHavinglist().size();i++) {
            	havinglist.add(object.getHavinglist().get(i).getItem());
            }
        }

    	if(havinglist!=null&&!havinglist.isEmpty()&&!havinglist.equals("")&&havinglist.size()!=0) {
        	for(int i=0;i<havinglist.size();i++) 
        	{
        		select.having(havinglist.get(i));
        	}
    	}
        List<String> joinlist=new ArrayList<String>();	
        if(selectsql!=null&&object!=null&&object.getJoinlist()!=null) {		                        
            for(int i=0;i<object.getJoinlist().size();i++) {
            	joinlist.add(object.getJoinlist().get(i).getItem());
            }
        }
    	if(joinlist!=null&&!joinlist.isEmpty()&&!joinlist.equals("")&&joinlist.size()!=0) {
        	for(int i=0;i<joinlist.size();i++) 
        	{
        		select.join(joinlist.get(i));
        	}
    	}
        List<String> leftjoinlist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getLeftjoinlist()!=null) {		                      
            for(int i=0;i<object.getLeftjoinlist().size();i++) {
            	leftjoinlist.add(object.getLeftjoinlist().get(i).getItem());
            }
        }
    	if(leftjoinlist!=null&&!leftjoinlist.isEmpty()&&!leftjoinlist.equals("")&&leftjoinlist.size()!=0) {
        	for(int i=0;i<leftjoinlist.size();i++) 
        	{
        		select.leftJoin(leftjoinlist.get(i));
        	}
    	}
    	
        List<String> wherelist=new ArrayList<String>();	  
        if(selectsql!=null&&object!=null&&object.getWherelist()!=null) {		               
            for(int i=0;i<object.getWherelist().size();i++) {
            	wherelist.add(object.getWherelist().get(i).getItem());
            }
        }
    	if(wherelist!=null&&!wherelist.isEmpty()&&!wherelist.equals("")&&wherelist.size()!=0) {
        	for(int i=0;i<wherelist.size();i++) 
        	{
        		select.where(wherelist.get(i));
        	}
    	}
        List<String> orderbylist=new ArrayList<String>();	 
        if(selectsql!=null&&object!=null&&object.getOrderbylist()!=null) {		                
            for(int i=0;i<object.getOrderbylist().size();i++) {
            	orderbylist.add(object.getOrderbylist().get(i).getItem());
            }
        }
    	if(orderbylist!=null&&!orderbylist.isEmpty()&&!orderbylist.equals("")&&orderbylist.size()!=0) {
        	for(int i=0;i<orderbylist.size();i++) 
        	{
    		   select.orderBy(orderbylist.get(i));
        	}
    	}

    	return select;
    }
    

    public String deleteBuilder (String table,List<String> wherelist)throws SQLException
    {
    	DeleteBuilder delete=new DeleteBuilder(table);
    	
    	if(wherelist!=null&&!wherelist.isEmpty()&&!wherelist.equals("")&&wherelist.size()!=0) {
	    	for(int i=0;i<wherelist.size();i++) 
	    	{
	    		delete.where(wherelist.get(i));
	        }
    	}
    	return delete.toString();
    }
    public String updateBuilder (String table,List<String> wherelist)throws SQLException
    {
    	UpdateBuilder update=new UpdateBuilder(table);
    	
    	if(wherelist!=null&&!wherelist.isEmpty()&&!wherelist.equals("")&&wherelist.size()!=0) {
	    	for(int i=0;i<wherelist.size();i++) 
	    	{
	    		update.where(wherelist.get(i));
	        }
    	}
    	return update.toString();
    }
}
