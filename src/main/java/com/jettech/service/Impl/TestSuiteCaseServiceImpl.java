package com.jettech.service.Impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestSuiteCase;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.service.TestSuiteCaseService;
@Service
public class TestSuiteCaseServiceImpl implements TestSuiteCaseService {

	
	
	@Autowired
	private TestSuiteCaseRepository testSuiteCaseRepository;
	


	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}



	@Override
	public Integer[] findCaseIdsBysuiteId(Integer suiteId) {
		// TODO Auto-generated method stub
		return testSuiteCaseRepository.findCaseIdsBysuiteId(suiteId);
	}

	@Override
	public List<TestSuiteCase> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<TestSuiteCase> saveAll(List<TestSuiteCase> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(TestSuiteCase entity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TestSuiteCase findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Page<TestSuiteCase> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public Integer CountCase(Integer suiteId) {
		
		return testSuiteCaseRepository.CountCase(suiteId);
	}



	@Override
	@Transactional
	public void deleteRelationBySuiteId(int id) {
		// TODO Auto-generated method stub
		testSuiteCaseRepository.deleteRelationBySuiteId(id);
	}

}
