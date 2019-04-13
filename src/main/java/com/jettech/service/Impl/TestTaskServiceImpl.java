package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestSuite;
import com.jettech.entity.TestTask;
import com.jettech.repostory.TestTaskRepository;
import com.jettech.service.ITestTaskService;

@Service
public class TestTaskServiceImpl implements ITestTaskService {

	@Autowired
	TestTaskRepository repository;

	@Override
	public List<TestTask> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestTask> saveAll(List<TestTask> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestTask entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestTask findById(Integer id) {
		Optional<TestTask> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestTask> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
