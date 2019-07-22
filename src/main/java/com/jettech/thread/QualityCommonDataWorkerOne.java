package com.jettech.thread;

import com.jettech.db.adapter.*;
import com.jettech.domain.*;
import com.jettech.entity.QualityTestResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QualityCommonDataWorkerOne implements Runnable {

    static final Logger logger = LoggerFactory.getLogger(QualityCommonDataWorkerOne.class);
    protected volatile boolean _isRunning = true;
    protected QualityQueryModel testQuery;
    protected DbModel dataSource = null;
    //添加运行状态
    protected boolean state = true;
    AbstractAdapter adapter = null;
    //添加质量方法    20190412
    protected int dataCount = 0;
    protected int allDataCount = 0;
    protected BlockingQueue<QualityTestResultItem> queue = null;
    protected Integer qualityTestResultId;

    public QualityCommonDataWorkerOne(BlockingQueue<QualityTestResultItem> queue, QualityQueryModel testQuery) throws Exception {
        if (queue == null) {
            throw new Exception("queue is null,create DataWorker failed");
        } else {
            this.queue = queue;
        }
        if (testQuery == null) {
            throw new Exception("testQuery is null,create DataWorker failed");
        } else {
            this.testQuery = testQuery;
            this.dataSource = testQuery.getDataSource();
            this.qualityTestResultId = testQuery.getQualityTestResultId();
        }
        String testQueryName = testQuery.getName();
        if (testQuery.getDataSource() == null) {
            throw new Exception("testQuery[" + testQueryName + "]'s DataSource is null,create DataWorker failed");
        }
        String dataSourceName = testQuery.getDataSource().getName();
        this.adapter = AdapterFactory.create(dataSource.getDatabaseType());
        if (this.adapter == null) {
            String info = "testQuery:[" + testQueryName + "] dataSource[" + dataSourceName + "] create adapter failed.";
            throw new Exception(info);
        }
    }

    public void stop() {
        _isRunning = false;
    }

    @Override
    public void run() {
        Connection conn = null;
        AbstractAdapter adapter = null;
        String dbInfo = getDbInfo(testQuery.getDataSource());
        try {
            // 获取数据库连接
            adapter = AdapterFactory.create(dataSource.getDatabaseType());
            if (adapter == null) {
                logger.error("creat adapter failed. " + dbInfo);
                return;
            } else {
                logger.info("creat adapter success. " + dbInfo);
            }
        } catch (Exception e1) {
            logger.error("creat adapter error", e1);
        }

        try {
            // 获得数据库连接
            conn = adapter.getConnection(dataSource);
            if (conn == null || conn.isClosed()) {
                logger.error("create connection failed." + dbInfo);
                return;
            } else {
                logger.info("creat connection success. " + dbInfo);
            }

            // 获得准备执行的SQL
            String sql = getQuerySQL();
            PreparedStatement pStmt = null;
            pStmt = conn.prepareStatement(sql);
            ResultSet rs = pStmt.executeQuery();
            // 获取数据到Map
            getDataRows(rs);


            //求总数

            String sqlCount = "select count(*) from " + this.testQuery.getDataSchemaName()+"."+this.testQuery.getDataTableName();
            if(dataSource.getDatabaseType().getName() == "Oracle"){
                sqlCount = "select count(*) from " + this.testQuery.getDataSchemaName()+".\""+this.testQuery.getDataTableName()+"\"";
            }
            System.out.println("=====求总数===sqlCount=====>"+sqlCount);
            System.out.println("=====执行sql===sql=====>"+sql);
            pStmt = conn.prepareStatement(sqlCount);
            ResultSet rsCount = pStmt.executeQuery();
            while (rsCount.next())
            {
                allDataCount =  rsCount.getInt(1);
            }


            //    logger.info(String.format("[%s]获取数据行数:[%d]", this.testQuery.getDataSource().getName(), map.size()));
        } catch (Exception e) {
            state = false;
            logger.error("get data error.", e);
        } finally {
            logger.info("退出生产者线程！");
            adapter.closeConnection(conn);
            _isRunning = false;
        }
    }

    protected String getDbInfo(DbModel ds) {
        String dbInfo = "DatabaseType:" + ds.getDatabaseType().name();
        dbInfo += " url:" + ds.getUrl();
        dbInfo += " userName:" + ds.getUsername();
        dbInfo += " password:" + ds.getPassword();
        return dbInfo;
    }

    protected String getQuerySQL() {
        String sql = testQuery.getSqlText();
        logger.info(String.format("Query:[%s]\r\n\t\t\t获取数据SQL:[%s]", this.testQuery.getName(), sql));
        return sql;
    }

    protected void getDataRows(ResultSet rs) throws SQLException, InterruptedException {
        //  Map<String, DataField> map = new HashMap<>();
        List<DataField> list = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int ColNum = rsmd.getColumnCount();// 取得列的数量
        for (int i = 1; i <= ColNum; i++) {
            DataField col = new DataField();
            //  col.setTableName(rsmd.getTableName(i));// 表名称
            col.setColumnName(rsmd.getColumnName(i));// 名称
            //    col.setLabel(rsmd.getColumnLabel(i));// 标签
            //   col.setDataType(rsmd.getColumnTypeName(i));// 数据类型名称
            //    col.setDataLength(rsmd.getPrecision(i));// 长度？
            //  col.setScale(rsmd.getScale(i));// 小数精度?
            //   map.put(col.getColumnName().toUpperCase(), col);
            list.add(col);
        }
        if (testQuery != null) {
            this.testQuery.setQueryColumns(list);
        }

        while (rs.next()) {
            dataCount++;
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                String columnName = rs.getMetaData().getColumnName(i);
                if(columnName != testQuery.getTestFields().get(0).getName() && !columnName.equals(testQuery.getTestFields().get(0).getName())){
                    continue;
                }
                QualityTestResultItem item = new QualityTestResultItem();
                Object selectValue = rs.getObject(i);
                //插入id
                item.setIdNumber(rs.getObject(1).toString());
               //没有包的明细为1
                item.setSign(1);
                // 列名称统一转换为大写[这里根据不同数据库可能需要调整]
                //     String columnLabel = rs.getMetaData().getColumnLabel(i).toUpperCase().trim();
                if(selectValue != null && selectValue != ""){
                    item.setSelectValue(selectValue.toString());
                }else{
                    item.setSelectValue("null");
                }
                item.setResult("true value");
                item.setColumnName(columnName);
                item.setTestResultId(qualityTestResultId);
                queue.put(item);
             //   queue.add(item);
            }
        }

       /* while (rs.next()) {
            dataCount++;
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                QualityTestResultItem item = new QualityTestResultItem();
                Object selectValue = rs.getObject(i);
                String columnName = rs.getMetaData().getColumnName(i);
                // 列名称统一转换为大写[这里根据不同数据库可能需要调整]
                //     String columnLabel = rs.getMetaData().getColumnLabel(i).toUpperCase().trim();
                if(selectValue != null && selectValue != ""){
                    item.setSelectValue(selectValue.toString());
                }else{
                    item.setSelectValue("null");
                }
                item.setResult("failed value");
                item.setColumnName(columnName);
                item.setTestResultId(qualityTestResultId);
                //   queue.put(item);
                queue.add(item);
            }
        }*/





    }




}
