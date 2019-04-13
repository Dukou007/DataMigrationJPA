package com.jettech.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import com.jettech.domain.DbModel;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DBConnectionManager {
    
    private static DBConnectionManager instance;
    
    private ComboPooledDataSource dataSource = null;
    
    private static ThreadLocal<Connection> tl=new ThreadLocal<>();
 
    //让构造函数为 private，这样该类就不会被实例化
    private DBConnectionManager(){
        
    }
    
    /**
     * 
     * @return
     */
    public static synchronized DBConnectionManager getInstance() {
        
        if(null == instance) {
            instance = new DBConnectionManager();
        }
        
        return instance;
    }
    
    public ComboPooledDataSource createConnection(DbModel dbModel){
        dataSource = new ComboPooledDataSource();
        try {
            // 设置数据库url
            dataSource.setJdbcUrl(dbModel.getUrl());
            // 设置数据库用户名称
            dataSource.setUser(dbModel.getUsername());
            // 设置数据库密码
            dataSource.setPassword(dbModel.getPassword());
            // 设置数据库驱动
            dataSource.setDriverClass(dbModel.getDriver());
            // 设置连接池
            dataSource.setInitialPoolSize(10);
          //连接池中保留的最大连接数
            dataSource.setMaxPoolSize(100);
            //定义了连接池内单个连接所拥有的最大缓存statements数
            //dataSource.setMaxStatementsPerConnection(maxStatementsPerConnection);
            dataSource.setMinPoolSize(10);
            //连接用完了，自动增加3个
            dataSource.setAcquireIncrement(3);
            //连接失败后重试30次
            dataSource.setAcquireRetryAttempts(30);
            //两次连接中间间隔2000毫秒
            dataSource.setAcquireRetryDelay(500);
            //xxx毫秒后如果sql数据没有执行完将会报错，如果设置成0，那么将会无限的等待
            dataSource.setCheckoutTimeout(0);
            //异步操作，提升性能通过多线程实现多个操作同时被执行
            dataSource.setNumHelperThreads(5);
        }  catch (PropertyVetoException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return dataSource;

     }        
    public Connection getConnection() throws SQLException{
    	Connection conn = tl.get();
    	if(conn == null) {
    		conn = dataSource.getConnection();
    		tl.set(conn);
    	}
    	return conn;
    }
    
	// 释放 connection
	public static void closeConn(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
				//和线程解绑
				tl.remove();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			conn = null;
		}
	}

}
