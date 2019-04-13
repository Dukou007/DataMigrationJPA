package com.jettech.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.pagehelper.PageInfo;

public interface IService<T, K> {

	public List<T> findAll();

	public List<T> saveAll(List<T> list);

	public void save(T entity);

	public void delete(K id);

//	public T getOneById(K id);
	
	public T findById(K id);
	
    Page<T> findAllByPage(Pageable pageable);

	
}