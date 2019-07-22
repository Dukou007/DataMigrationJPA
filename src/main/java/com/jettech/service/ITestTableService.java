/**
 * 
 */
package com.jettech.service;

import java.sql.SQLException;
import java.util.List;

import com.jettech.entity.DataTable;
import com.jettech.vo.ResultVO;

/**
 *  @author Eason007
 *	@Description: 该类的功能描述
 *  @date: 2019年2月3日 上午11:21:06 
 */
public interface ITestTableService extends IService<DataTable, Integer> {
    public List<DataTable> findByForeignKey(int dbId) throws SQLException ;
    
    //复制表
    public ResultVO copyDataTable(int id,String name);
    public void SetOneDataTable(int id);
    public List<DataTable> getTablesBySchemaID(int schemaID,String tableName);
    
    public DataTable findByName(String tableName);

	public List<DataTable> findBySchemaId(Integer id);
}
