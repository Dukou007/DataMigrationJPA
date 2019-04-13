package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import com.jettech.repostory.TestQueryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.service.TestQueryService;

@Service
public class TestQueryServiceImpl implements TestQueryService {

	@Autowired
	private TestQueryRepository repository;
	



	@Override
	public List<TestQuery> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestQuery> saveAll(List<TestQuery> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestQuery entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestQuery findById(Integer id) {
		Optional<TestQuery> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestQuery> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public List<TestQuery> findByCaseId(Integer testCaseId) {
		return repository.findByCaseId(testCaseId);
	}

	
	@Override
	public List<TestQuery> getByCaseId(Integer caseId) {
		// TODO Auto-generated method stub
		return null;
	}

	
	

	
}
