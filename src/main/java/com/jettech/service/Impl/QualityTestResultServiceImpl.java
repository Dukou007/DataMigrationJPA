package com.jettech.service.Impl;

import com.jettech.entity.QualityTestResult;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.service.IQualityTestReusltService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QualityTestResultServiceImpl implements IQualityTestReusltService {

	@Autowired
	private QualityTestResultRepository repository;

	@Override
	public QualityTestResult saveOne(QualityTestResult entity) {
		return repository.save(entity);
	}

	@Override
	public List<QualityTestResult> findAll() {

		return repository.findAll();
	}

	@Override
	public List<QualityTestResult> saveAll(List<QualityTestResult> list) {

		return repository.saveAll(list);
	}

	@Override
	public void save(QualityTestResult entity) {

		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		// int i = 1 / 0;
		repository.deleteById(id);
	}


	@Override
	public QualityTestResult getOneById(Integer id) {

		return repository.findById(id).get();
	}

	@Override
	public Page<QualityTestResult> findPage(int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return repository.findAll(pageRequest);
	}

	@Override
	public Page<QualityTestResult> findPage(QualityTestResult qualityTestResult, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QualityTestResult findById(Integer id) {
		return repository.getOne(id);
	}

	@Override
	public Page<QualityTestResult> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

}
