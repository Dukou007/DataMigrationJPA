package com.jettech.util;

//import com.alibaba.druid.sql.ast.SQLStatement;
//import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
//import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
//import com.alibaba.druid.sql.parser.SQLStatementParser;

public class SplitData {
	public static String subString(String str, String strStart, String strEnd) {  
		/* 找出指定的2个字符在 该字符串里面的 位置 */       
		int strStartIndex = str.indexOf(strStart);       
		int strEndIndex = str.indexOf(strEnd);  
		String result ="";
		/* index 为负数 即表示该字符串中 没有该字符 */       
		if (strStartIndex < 0) {            
			 result = str.substring(0, strEndIndex);    
			 return result;
		}  
		if (strEndIndex < 0) {            
		//	System.out.println("strEndIndex="+strEndIndex);
			if(strEnd.equals("end")) {
			 result = str.substring(strStartIndex).substring(strStart.length());     
			}
			 return result;
		}     
		/* 开始截取 */        
	     result = str.substring(strStartIndex, strEndIndex).substring(strStart.length());  
		return result;    
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String str="select a,b from table";
		String result=SplitData.subString(str,"from", "end");//subString(str, "from", "where");
		System.out.println(result);
		
//		MySqlStatementParser parser = new MySqlStatementParser(sql);     
//        SQLStatement statement = parser.parseStatement();
//        MySqlInsertStatement insert = (MySqlInsertStatement)statement; 
//        String tableName = StringUtil.removeBackquote(insert.getTableName().getSimpleName());
		
		
//
//        String sql = "select * from user order by id";
//
// 
//
//        // 新建 MySQL Parser
//
//        SQLStatementParser parser = new MySqlStatementParser(sql);
//
// 
//
//        // 使用Parser解析生成AST，这里SQLStatement就是AST
//
//        SQLStatement statement = parser.parseStatement();
//
// 
//
//        // 使用visitor来访问AST
//
//        MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
//
//        statement.accept(visitor);
//
//        
//
//        System.out.println(visitor.getColumns());
//
//        System.out.println(visitor.getOrderByColumns());
	}

}
