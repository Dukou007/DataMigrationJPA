package com.jettech.service.Impl;

import com.jettech.entity.TestRound;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.TestRoundRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.TestRoundService;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Service
public  class TestRoundServiceImpl implements TestRoundService {

	@Autowired
	TestRoundRepository repository;

	@Autowired
	TestSuiteRepository testSuiteRepository;
	
	
	@Override
	public List<TestRound> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestRound> saveAll(List<TestRound> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestRound entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestRound findById(Integer id) {
		Optional<TestRound> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestRound> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public List<TestRound> findBySuiteId(int SuiteId) {
		return repository.getAmountBySuiteId(SuiteId);
	}

	@Override
	public Page<TestRound> findByTestSuiteName(String suiteName, int pageNum, int pageSize) {
		return null;
	}

	@Override
	public Page<TestRound> findAllRoundBytestResultID(Integer testResultID, PageRequest pageable) {
		return null;
	}

	@Override
	public Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, PageRequest pageable) {
		Page<TestRound>list=repository.findAllRoundBytestSuiteID(testSuiteID,pageable);
		if(list.getSize()>0) {
			return list;
		}else {
			return new PageImpl<TestRound>(new ArrayList<TestRound>(), pageable, 0);
		}
	}

	@Override
	public TestRound selectTestRoundByTestSuiteId(Integer testSuiteId) {
		List<TestRound> test = repository.findByTestSuiteId(testSuiteId);
		TestRound testRound = new TestRound();
		if(test.size()>0){
			testRound = test.get(0);
		}
		return testRound;
	}

	@Override
	public Page<TestRound> findTestRoundBySuiteName(String suiteName, Pageable pageable) {
		Page<TestRound>list=null;
		if(suiteName.equals("")||suiteName==null) {
			list=repository.findAll(pageable);
		}else {
			Specification<TestRound> specification = new Specification<TestRound>() {

				@Override
				public Predicate toPredicate(Root<TestRound> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					if(StringUtils.isNotBlank(suiteName)) {
						predicateList.add(cb.like(root.get("testSuite").get("name"), "%"+suiteName+"%"));
					}
					return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
				}
			};
			list=this.repository.findAll(specification,pageable);
		}
		if(list.getSize()>0) {
			return list;
		}else {
			return new PageImpl<TestRound>(new ArrayList<>(), pageable, 0);
		}
	}






}
