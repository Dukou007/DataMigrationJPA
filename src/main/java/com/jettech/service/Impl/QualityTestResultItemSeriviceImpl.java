package com.jettech.service.Impl;

import java.util.List;

import com.jettech.repostory.QualityTestResultItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.QualityTestResultItem;
import com.jettech.repostory.QualityTestRepository;
import com.jettech.service.QualityTestResultItemService;

@Service
public class QualityTestResultItemSeriviceImpl implements QualityTestResultItemService {

	@Autowired
	QualityTestResultItemRepository quantityTestResultItemRepository;
	@Autowired
	QualityTestRepository qualityTestRepository;

	@Override
	public List<QualityTestResultItem> findAll() {
		
		return null;
	}

	@Override
	public List<QualityTestResultItem> saveAll(List<QualityTestResultItem> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(QualityTestResultItem entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public QualityTestResultItem findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<QualityTestResultItem> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID,String result, Pageable pageable) {
		Page<QualityTestResultItem> list = quantityTestResultItemRepository
				.findTestResultItemByTestResultID(testResultID,result, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}
	}

}
