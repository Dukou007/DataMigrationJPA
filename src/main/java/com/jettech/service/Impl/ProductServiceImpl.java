package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.Product;
import com.jettech.repostory.ProductRepository;
import com.jettech.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ProductRepository repository;

	@Override
	public List<Product> findAll() {
		return repository.findAll();
	}

	@Override
	public List<Product> saveAll(List<Product> list) {
		return repository.saveAll(list);
	}

	@Override
	@Transactional
	public void save(Product entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public Product findById(Integer id) {
		Optional<Product> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Product getByName(String name) {
		List<Product> list = repository.findByName(name);
		Product entity = null;
		if (list == null || list.size() == 0) {
			entity = new Product(name);
			entity = repository.save(entity);
		} else {
			entity = list.get(0);
		}

		return entity;
	}

	@Override
	public List<Product> findSubProductList(Integer parentID) {
		return repository.findSubProductList(parentID);
	}

	@Override
	public Page<Product> findAllByPage(Pageable pageable) {
		Page<Product> list = repository.findAll(pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}

	}
	
	@Override
	public List<Product> findAllByParentID() {
		return repository.findAllByParentID();
	}

	@Override
	public List<Product> findSubProductListByParentID() {
		return repository.findSubProductListByParentID();
	}

	@Override
	public List<Product> findRootPruducts() {
		return repository.findRootPruducts();
	}
	
	@Override
	public Page<Product> findProductByProductName(String productName, Pageable pageable) {
		Page<Product> list=repository.findProductByProductName(productName,pageable);
		if(list.getSize()>0) {
			return list;
		}else {
			return new PageImpl<Product>(new ArrayList<Product>(), pageable, 0);
		}
	}

	
}
