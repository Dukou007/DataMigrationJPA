package com.jettech.service.Impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.CodeMapItem;
import com.jettech.repostory.CodeMapItemRepository;
import com.jettech.service.ICodeMapItemService;

@Service
public class CodeMapItemServiceImpl implements ICodeMapItemService {

	@Autowired
	private CodeMapItemRepository repository;

	@Override
	public List<CodeMapItem> findAll() {
		return repository.findAll();
	}

	@Override
	public List<CodeMapItem> saveAll(List<CodeMapItem> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(CodeMapItem entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public CodeMapItem findById(Integer id) {
		Optional<CodeMapItem> optional = repository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		} else
			return null;
	}

	@Override
	public Page<CodeMapItem> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

}
