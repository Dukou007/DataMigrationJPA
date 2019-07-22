package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.CodeMap;
import com.jettech.repostory.CodeMapRepository;
import com.jettech.service.ICodeMapService;

@Service
public class CodeMapServiceImpl implements ICodeMapService {

	@Autowired
	private CodeMapRepository repository;

	@Override
	public List<CodeMap> findAll() {
		return repository.findAll();
	}

	@Override
	public List<CodeMap> saveAll(List<CodeMap> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(CodeMap entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public CodeMap findById(Integer id) {
		Optional<CodeMap> optional = repository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		} else
			return null;
	}

	@Override
	public Page<CodeMap> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

}
