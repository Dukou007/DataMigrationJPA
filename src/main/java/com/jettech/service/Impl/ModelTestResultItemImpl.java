package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.ModelTestResultItem;
import com.jettech.repostory.ModelTestResultItemRepository;
import com.jettech.service.ModelTestResultItemService;

@Service
public class ModelTestResultItemImpl implements ModelTestResultItemService {

	@Autowired
	private ModelTestResultItemRepository repository;

	@Override
	public List<ModelTestResultItem> findAll() {
		return repository.findAll();
	}

	@Override
	public List<ModelTestResultItem> saveAll(List<ModelTestResultItem> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(ModelTestResultItem entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public ModelTestResultItem findById(Integer id) {
		Optional<ModelTestResultItem> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public void addBatch(List<ModelTestResultItem> itemList) {
		repository.saveAll(itemList);
	}

	@Override
	public Page<ModelTestResultItem> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public Page<ModelTestResultItem> findTestResultItemByTestResultID(Integer testResultID,String result, Pageable pageable) {
		Page<ModelTestResultItem> list = repository.findTestResultItemByTestResultID(testResultID,result, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public List<ModelTestResultItem> findByTestResultItemID(String ids) {
		ArrayList<ModelTestResultItem> list = new ArrayList<ModelTestResultItem>();
		if (ids != null) {
			String[] ResultItemIDs = ids.split(",");
			for (String i : ResultItemIDs) {
				ModelTestResultItem testResultItem = repository.findByTestResultItemID(i);
				list.add(testResultItem);
			}
			return list;
		}else {
			return null;
		}
		
		
		 
		 

	}

	@Override
	public Page<ModelTestResultItem> findTestResultItemLikeKeyValue(String keyValue,String testResultId, Pageable pageable) {
		Page<ModelTestResultItem> list = repository.findTestResultItemLikeKeyValue(keyValue,testResultId, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Page<ModelTestResultItem> findTestResultItemByResult(String result, Pageable pageable) {
		if (result != null) {
			Page<ModelTestResultItem> list = repository.findTestResultItemByResult(result, pageable);
			return list;
		} else {
			Page<ModelTestResultItem> list = repository.findAll(pageable);
			return list;
		}
	}

	@Override
	public List<ModelTestResultItem> findByTestResultID(String id) {
		if(StringUtils.isNotBlank(id)&&StringUtils.isNotEmpty(id)) {
			List<ModelTestResultItem>list=repository.findByTestResultID(id);
			return list;
		}else {
			return null;
		}
		
	}

	@Override
	public List<ModelTestResultItem> findByTestResultItemIDs(String ids) {
		ArrayList<ModelTestResultItem> list = new ArrayList<ModelTestResultItem>();
		if(StringUtils.isNotBlank(ids)){
			String[] testResultItemIDs = ids.split(",");
			for (String id : testResultItemIDs) {
				ModelTestResultItem testResultItem = repository.getOne(Integer.parseInt(id));
				list.add(testResultItem);
			}
			return list;
		}else {
			
			return null;
		}
	}

}
