/**
 * 
 */
package com.jettech.service.Impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.DataField;
import com.jettech.entity.FileDataSource;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.service.ITestFieldService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

/**
 *  @author Eason007
 *	@Description: TestFieldServiceImpl
 *  @date: 2019年2月3日 上午10:00:28 
 */

@Service
public class DataFieldServiceImpl implements ITestFieldService{

	@Autowired
	private DataFieldRepository testFieldRepository;
	
	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAll()
	 */
	@Override
	public List<DataField> findAll() {
		// TODO Auto-generated method stub
		return testFieldRepository.findAll();
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#saveAll(java.util.List)
	 */
	@Override
	public List<DataField> saveAll(List<DataField> list) {
		// TODO Auto-generated method stub
		return testFieldRepository.saveAll(list);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#save(java.lang.Object)
	 */
	@Override
	public void save(DataField entity) {
		// TODO Auto-generated method stub
		testFieldRepository.save(entity);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#delete(java.lang.Object)
	 */
	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		testFieldRepository.deleteById(id);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findById(java.lang.Object)
	 */
	@Override
	public DataField findById(Integer id) {
		// TODO Auto-generated method stub
		return testFieldRepository.getOne(id);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAllByPage(org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<DataField> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return testFieldRepository.findAll(pageable);
	}

	@Override
	public List<DataField> findByTableName(String name) {
		return testFieldRepository.findByTableName(name);
	}
	@Override
	public List<DataField> findByForeignKey(int test_table_id){
		return testFieldRepository.findByForeignKey(test_table_id);
	}

	/**
	 * 添加质量方法  20190318
	 * @param id
	 * @return
	 */
	@Override
	public List<DataField> findAllByTableId(Integer id) {
		List<DataField> testFields = testFieldRepository.findByForeignKey(id);
		return testFields;
	}

	@Override
	public ResultVO copyDataField(Integer id, String name) {
		DataField dataField=testFieldRepository.findById(id).get();
		DataField existdataField=testFieldRepository.findByName(name);
		if(existdataField!=null){
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		DataField copyDataField=new DataField();
		BeanUtils.copyProperties(dataField, copyDataField);
		copyDataField.setCreateTime(new Date());
		copyDataField.setId(null);
		copyDataField.setName(name);
		testFieldRepository.save(copyDataField);
		return new ResultVO(true, StatusCode.OK, "复制成功");
	}


}
