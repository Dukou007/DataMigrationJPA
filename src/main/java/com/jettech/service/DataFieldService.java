/**
 * 
 */
package com.jettech.service;

import java.util.List;

import com.jettech.entity.DataField;

/**
 *  @author Eason007
 *	@Description: TestFieldService
 *  @date: 2019年2月3日 上午9:58:47 
 */
public interface DataFieldService extends IService<DataField, Integer> {
	List<DataField> findByTableName(String name);
}
