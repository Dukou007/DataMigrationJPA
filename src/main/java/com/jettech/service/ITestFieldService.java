/**
 * 
 */
package com.jettech.service;

import java.util.List;

import com.jettech.entity.DataField;
import com.jettech.vo.ResultVO;

/**
 *  @author Eason007
 *	@Description: TestFieldService
 *  @date: 2019年2月3日 上午9:58:47 
 */
public interface ITestFieldService extends IService<DataField, Integer> {
	List<DataField> findByTableName(String name);
	public List<DataField> findByForeignKey(int test_table_id);

	//添加质量方法 20190318
	List<DataField> findAllByTableId(Integer id);
	ResultVO copyDataField(Integer id,String name);
}
