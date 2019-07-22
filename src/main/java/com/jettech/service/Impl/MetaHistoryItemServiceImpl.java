/**
 * 
 */
package com.jettech.service.Impl;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.MetaHistoryItem;
import com.jettech.repostory.MetaHistoryItemRepository;
import com.jettech.service.IMetaHistoryItemService;

/**
 *  @author Eason007
 *	@Description: TestFieldServiceImpl
 *  @date: 2019年2月3日 上午10:00:28 
 */

@Service
public class MetaHistoryItemServiceImpl implements IMetaHistoryItemService{

	@Autowired
	private MetaHistoryItemRepository metaHistoryItemRepository;
	
	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAll()
	 */
	@Override
	public List<MetaHistoryItem> findAll() {
		// TODO Auto-generated method stub
		return metaHistoryItemRepository.findAll();
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#saveAll(java.util.List)
	 */
	@Override
	public List<MetaHistoryItem> saveAll(List<MetaHistoryItem> list) {
		// TODO Auto-generated method stub
		return metaHistoryItemRepository.saveAll(list);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#save(java.lang.Object)
	 */
	@Override
	public void save(MetaHistoryItem entity) {
		// TODO Auto-generated method stub
		metaHistoryItemRepository.save(entity);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#delete(java.lang.Object)
	 */
	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		metaHistoryItemRepository.deleteById(id);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findById(java.lang.Object)
	 */
	@Override
	public MetaHistoryItem findById(Integer id) {
		// TODO Auto-generated method stub
		return metaHistoryItemRepository.getOne(id);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAllByPage(org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<MetaHistoryItem> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return metaHistoryItemRepository.findAll(pageable);
	}
	@Override
	public Page<MetaHistoryItem> getAllChangeByPage(int test_database_id,String tableName,Pageable pageable) throws SQLException {
		if(!tableName.equals("")) {
			return metaHistoryItemRepository.findByForeignKeyByPage(test_database_id,tableName,pageable);	
		}else {
			return metaHistoryItemRepository.findByDBByPage(test_database_id,pageable);	
		}
			 
	}
}
