/**
 * 
 */
package com.jettech.service;

import java.sql.SQLException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.MetaHistoryItem;

/**
 *  @author wangli
 *	@Description: IMetaHistoryItemService
 *  @date: 2019年3月4日下 午13:41
 */
public interface IMetaHistoryItemService extends IService<MetaHistoryItem, Integer> {
	public Page<MetaHistoryItem> getAllChangeByPage(int test_database_id,String tableName,Pageable pageable) throws SQLException ;
}
