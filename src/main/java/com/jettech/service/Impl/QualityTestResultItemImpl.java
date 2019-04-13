package com.jettech.service.Impl;

import com.jettech.entity.QualityTestResultItem;
import com.jettech.repostory.QualityTestResultItemRepository;
import com.jettech.service.IQualityTestResultItemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QualityTestResultItemImpl implements IQualityTestResultItemService {

	@Autowired
	private QualityTestResultItemRepository repository;

	@Override
	public List<QualityTestResultItem> findAll() {
		return repository.findAll();
	}

	@Override
	public List<QualityTestResultItem> saveAll(List<QualityTestResultItem> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(QualityTestResultItem entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public QualityTestResultItem getOneById(Integer id) {
		return repository.getOne(id);
	}

	@Override
	public void addBatch(List<QualityTestResultItem> itemList) {
		repository.saveAll(itemList);
	}

	@Override
	public Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID, Pageable pageable) {
		Page<QualityTestResultItem> list = repository.findTestResultItemByTestResultID(testResultID,"", pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public List<QualityTestResultItem> findByTestResultItemIDs(String ids) {
		ArrayList<QualityTestResultItem> list = new ArrayList<QualityTestResultItem>();
		if(StringUtils.isNotBlank(ids)){
			String[] testResultItemIDs = ids.split(",");
			for (String id : testResultItemIDs) {
				QualityTestResultItem qualityTestResultItem = repository.getOne(Integer.parseInt(id));
				list.add(qualityTestResultItem);
			}
			return list;
		}else {
			
			return null;
		}
	}

	@Override
	public List<QualityTestResultItem> findByTestResultID(String id) {
		ArrayList<QualityTestResultItem> list= repository.findByTestResultID(id);
		if(list.size()>0) {
			return list;
		}else {
			
			return null;
		}
	}

}
