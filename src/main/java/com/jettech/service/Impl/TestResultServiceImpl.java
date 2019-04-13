package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.entity.TestRound;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestResultRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestReusltService;

@Service
public class TestResultServiceImpl implements ITestReusltService {

	@Autowired
	private TestResultRepository repository;

	@Autowired
	private TestCaseRepository testCaseRepository;

	@Override
	public TestResult saveOne(TestResult entity) {
		return repository.save(entity);
	}

	@Override
	public List<TestResult> findAll() {

		return repository.findAll();
	}

	@Override
	public List<TestResult> saveAll(List<TestResult> list) {

		return repository.saveAll(list);
	}

	@Override
	public void save(TestResult entity) {

		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		// int i = 1 / 0;
		repository.deleteById(id);
	}

	@Override
	public TestResult findById(Integer id) {
		Optional<TestResult> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestResult> findPage(TestResult testResult, int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return repository.findAll(pageRequest);
	}

	@Override
	public Page<TestResult> findResultListByCaseId(String caseId, Pageable pageable) {
		// return repository.findTestResultListByCaseId(caseId,pageable);
		return repository.findByCaseId(caseId, pageable);
		// return null;findByCaseId
	}

	@Override
	public Page<TestResult> findAllByPage(Pageable pageable) {
		Page<TestResult> list = repository.findAll(pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Page<TestResult> findTestResultByIdOrderStartTime(Integer testRoundID, Pageable pageable) {
		Page<TestResult> list = repository.findTestResultByIdOrderStartTime(testRoundID, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<TestResult>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable) {
		return null;
	}

	@Override
	public Page<TestResultItem> findByKeyValue(Integer keyValue, Pageable pageable) {
		Page<TestResultItem> list = repository.findByKeyValue(keyValue, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Page<TestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable) {
		Page<TestResult> list = repository.findTestResultByTestCaseID(caseID, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;

		} else {
			return new PageImpl<>(new ArrayList<TestResult>(), pageable, 0);

		}
	}

	@Override
	public Page<TestResult> findAll(Specification<TestResult> specification, Pageable pageable) {
		return repository.findAll(specification, pageable);
	}

	@Override
	public Page<TestResult> findTestResultByCaseName(String caseName, Pageable pageable) {
		Page<TestResult> list = null;
		if (caseName.equals("") || caseName == null) {
			list = repository.findAll(pageable);
		} else {
			list = repository.findCaseByNameLike(caseName,pageable);
//			System.out.println("dddddddddddddddddddddddd");
			}
		if (list != null) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResult> findAllByExecState(String state,Pageable pageable) {
		Page<TestResult>list=null;
		if(state==null||state.equals("")) {
			list=repository.findAll(pageable);
		}else {
//			list=repository.findAllByExecState(state);
		}
		return list;
	}

}
