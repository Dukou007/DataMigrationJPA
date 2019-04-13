package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.TestQueryFieldRepository;
import com.jettech.service.BaseService;
import com.jettech.service.TestQueryFieldService;

@Service
public class TestQueryFieldServiceImpl extends BaseService<TestQuery, Integer> implements TestQueryFieldService {

	@Autowired
	TestQueryFieldRepository repository;

	@Override
	public List<TestQueryField> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestQueryField> saveAll(List<TestQueryField> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestQueryField entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestQueryField findById(Integer id) {
		Optional<TestQueryField> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestQueryField> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

}
