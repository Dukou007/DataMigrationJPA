package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.ModelTestResult;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.repostory.ModelTestResultRepository;
import com.jettech.repostory.TestResultRepository;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ModelTestResultService;

@Service
public class ModelTestResultServiceImpl implements ModelTestResultService {

	@Autowired
	private ModelTestResultRepository repository;

	@Override
	public ModelTestResult saveOne(ModelTestResult entity) {
		return repository.save(entity);
	}

	@Override
	public List<ModelTestResult> findAll() {

		return repository.findAll();
	}

	@Override
	public List<ModelTestResult> saveAll(List<ModelTestResult> list) {

		return repository.saveAll(list);
	}

	@Override
	public void save(ModelTestResult entity) {

		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		// int i = 1 / 0;
		repository.deleteById(id);
	}

	@Override
	public ModelTestResult findById(Integer id) {
		Optional<ModelTestResult> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<ModelTestResult> findPage(ModelTestResult testResult, int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return repository.findAll(pageRequest);
	}

	@Override
	public Page<ModelTestResult> findResultListByCaseId(String caseId,Pageable pageable) {
		//return repository.findTestResultListByCaseId(caseId,pageable);
		return repository.findByCaseId(caseId,pageable);
		//return null;findByCaseId
	}

	@Override
	public Page<ModelTestResult> findAllByPage(Pageable pageable) {
		Page<ModelTestResult>list= repository.findAll(pageable);
		if(list.getSize()>0&&list!=null) {
			return list;
		}else {
			return null;
		}
	}

	

	@Override
	public Page<ModelTestResult> findTestResultByIdOrderStartTime(Integer testRoundID, Pageable pageable) {
		Page<ModelTestResult>list=repository.findTestResultByIdOrderStartTime(testRoundID,pageable);
		
		if(list.getSize()>0&&list!=null) {
			return list;
		}else {
			return null;
		}
		
	}

	@Override
	public Page<ModelTestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable) {
		return null;
	}

	@Override
	public Page<ModelTestResultItem> findByKeyValue(Integer keyValue, Pageable pageable) {
		Page<ModelTestResultItem>list=repository.findByKeyValue(keyValue,pageable);
		if(list.getSize()>0&&list!=null) {
			return list;
		}else {
			return null;
		}
	}

	@Override
	public Page<ModelTestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable) {
		Page<ModelTestResult>list=repository.findTestResultByTestCaseID(caseID,pageable);
		if(list.getSize()>0&&list!=null) {
			return list;
		}else {
			return null;
		}
	}




}
