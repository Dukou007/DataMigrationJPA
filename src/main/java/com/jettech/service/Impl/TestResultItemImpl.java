package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestResultItem;
import com.jettech.repostory.TestResultItemRepository;
import com.jettech.service.ITestResultItemService;

@Service
public class TestResultItemImpl implements ITestResultItemService {

	@Autowired
	private TestResultItemRepository repository;

	@Override
	public List<TestResultItem> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestResultItem> saveAll(List<TestResultItem> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestResultItem entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestResultItem findById(Integer id) {
		Optional<TestResultItem> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public void addBatch(List<TestResultItem> itemList) {
		repository.saveAll(itemList);
	}

	@Override
	public Page<TestResultItem> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public Page<TestResultItem> findTestResultItemByTestResultID(Integer testResultID, String result,
			Pageable pageable) {
		Page<TestResultItem> list = repository.findTestResultItemByTestResultID(testResultID, result, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return new PageImpl<TestResultItem>(new ArrayList<TestResultItem>(), pageable, 0);
		}
	}

	@Override
	public List<TestResultItem> findByTestResultItemID(String ids) {
		ArrayList<TestResultItem> list = new ArrayList<TestResultItem>();
		if (StringUtils.isNotBlank(ids)) {
			String[] ResultItemIDs = ids.split(",");
			for (String i : ResultItemIDs) {
				TestResultItem testResultItem = repository.findByTestResultItemID(i);
				list.add(testResultItem);
			}
			return list;
		} else {
			return null;
		}

	}

	@Override
	public Page<TestResultItem> findTestResultItemLikeKeyValue(String keyValue, Pageable pageable) {
		Page<TestResultItem> list = repository.findTestResultItemLikeKeyValue(keyValue, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return new PageImpl<>(new ArrayList<TestResultItem>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResultItem> findTestResultItemByResult(String result, Pageable pageable) {
		if (StringUtils.isNotBlank(result)) {
			Page<TestResultItem> list = repository.findTestResultItemByResult(result, pageable);
			return list;
		} else {
			Page<TestResultItem> list = repository.findAll(pageable);
			return list;
		}
	}

	@Override
	public List<TestResultItem> findByTestResultID(String id) {
		if (StringUtils.isNotBlank(id) && StringUtils.isNotEmpty(id)) {
			List<TestResultItem> list = repository.findByTestResultID(id);
			return list;
		} else {
			return null;
		}

	}

	@Override
	public List<TestResultItem> findByTestResultItemIDs(String ids) {
		ArrayList<TestResultItem> list = new ArrayList<TestResultItem>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testResultItemIDs = ids.split(",");
			for (String id : testResultItemIDs) {
				TestResultItem testResultItem = repository.getOne(Integer.parseInt(id));
				list.add(testResultItem);
			}
			return list;
		} else {

			return null;
		}
	}

	@Override
	public Page<TestResultItem> findTestResultItemByTestResultId(Integer testResultId, Pageable pageable) {

		return repository.findBytestResultId(testResultId, pageable);
	}

	@Override
	public Page<TestResultItem> findByColumnName(String columnName, Pageable pageable) {
		Page<TestResultItem> list;
		if (columnName == null) {
			list = repository.findAll(pageable);
		} else {
			list = repository.findByColumnName(columnName,pageable);
		}
		if(list!=null) {
			return list;
		}else {
			return new PageImpl<TestResultItem>(new ArrayList<TestResultItem>(), pageable, 0);
		}
		
	}

	@Override
	public Page<TestResultItem> findByResultIdAndKeyvalueAndResultAndColumnname(Integer testResultId, String result,
			String keyValue, String columnName,Pageable pageable) {
		
		Page<TestResultItem>list=null;
		Specification<TestResultItem> specification = new Specification<TestResultItem>() {

			@Override
			public Predicate toPredicate(Root<TestResultItem> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotBlank(testResultId.toString())) {
					predicateList.add(cb.equal(root.get("testResultId"),testResultId));
				}
				if(StringUtils.isNotBlank(result)) {
					predicateList.add(cb.equal(root.get("result").as(String.class),result));
				}
				if(StringUtils.isNotBlank(keyValue)) {
					predicateList.add(cb.equal(root.get("keyValue").as(String.class),keyValue));
				}
				if(StringUtils.isNotBlank(columnName)) {
					predicateList.add(cb.equal(root.get("columnName").as(String.class),columnName));
				}
				
				return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
			}
		};
		list=this.repository.findAll(specification, pageable);
		return list;
	}

	
}
