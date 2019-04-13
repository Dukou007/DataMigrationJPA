/**
 * 
 */
package com.jettech.service.Impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.EnumOptType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.MetaHistoryItem;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.service.IDataSourceService;
import com.jettech.service.ITestTableService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;

/**
 * @author Eason007
 * @Description: TestTableServiceImpl
 * @date: 2019年2月3日 上午11:24:31
 */
@Service
public class DataTableServiceImpl implements ITestTableService {

	@Autowired
	private DataTableRepository testTableRepository;
	@Autowired
	private DataFieldRepository testFieldRepository;
	@Autowired
	private IDataSourceService dataSourceService;

	private static Logger logger = LoggerFactory
			.getLogger(DataTableServiceImpl.class);
	private boolean updateDB = false;
	private boolean updateTable = false;
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jettech.service.IService#findAll()
	 */
	@Override
	public List<DataTable> findAll() {
		// TODO Auto-generated method stub
		return testTableRepository.findAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jettech.service.IService#saveAll(java.util.List)
	 */
	@Override
	public List<DataTable> saveAll(List<DataTable> list) {
		// TODO Auto-generated method stub
		return testTableRepository.saveAll(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jettech.service.IService#save(java.lang.Object)
	 */
	@Override
	public void save(DataTable entity) {
		// TODO Auto-generated method stub
		testTableRepository.save(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jettech.service.IService#delete(java.lang.Object)
	 */
	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		testTableRepository.deleteById(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jettech.service.IService#findById(java.lang.Object)
	 */
	@Override
	public DataTable findById(Integer id) {
		// TODO Auto-generated method stub
		return testTableRepository.getOne(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jettech.service.IService#findAllByPage(org.springframework.data.domain
	 * .Pageable)
	 */
	@Override
	public Page<DataTable> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return testTableRepository.findAll(pageable);
	}

	@Override
	public List<DataTable> findByForeignKey(int dbId) throws SQLException {
		List<DataTable> table = testTableRepository.findByForeignKey(dbId);
		return table;
	}

	@Override
	public ResultVO copyDataTable(int id, String name) {
		DataTable dataTable = testTableRepository.findById(id).get();
		DataTable exitsdataTable = testTableRepository.findByName(name);
		if (exitsdataTable != null) {
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		DataTable copyDataTable = new DataTable();
		BeanUtils.copyProperties(dataTable, copyDataTable);
		copyDataTable.setCreateTime(new Date());
		copyDataTable.setName(name);
		copyDataTable.setId(null);
		// copyDataTable.setVersion(0);
		copyDataTable = testTableRepository.save(copyDataTable);
		List<DataField> exitsdataFields = dataTable.getDataFields();
		if (exitsdataFields.size() != 0) {
			List<DataField> copydataFields = new ArrayList<DataField>();
			for (DataField dataField : exitsdataFields) {
				DataField copyDataFiled = new DataField();
				BeanUtils.copyProperties(dataField, copyDataFiled);
				copyDataFiled.setDataTable(copyDataTable);
				copyDataFiled.setTalbeName(name);
				copyDataFiled.setId(null);
				copyDataFiled.setCreateTime(new Date());
				copyDataFiled.setVersion(0);
				copydataFields.add(copyDataFiled);
			}
			testFieldRepository.saveAll(copydataFields);
		}

		return new ResultVO(true, StatusCode.OK, "复制成功");

	}

	@Override
	public void SetOneDataTable(int id) {
		// 找到这张表所对应的源
		DataTable findDataTable = testTableRepository.findById(id).get();
		if (findDataTable != null) {
			DataSource dataSource = findDataTable.getDataSchema()
					.getDataSource();
			if (dataSource != null) {
				SycData sycData = dataSourceService.getAdapterAndConnection(
						dataSource.getDatabaseType(), dataSource.getDriver(),
						dataSource.getUrl(), dataSource.getPort(),
						dataSource.getHost(), dataSource.getUserName(),
						dataSource.getPassword(), dataSource.getSid());
				dataSourceService.syncOneTableAndFiled(sycData.getAdapter(), sycData.getConn(),
						findDataTable);
			} else {
				logger.info("这张表" + findDataTable.getName() + "没有数据源，不能进行同步");
			}
		} else {
			logger.info("这张表" + findDataTable.getName() + "不存在，不能同步");
		}

	}

	
}
