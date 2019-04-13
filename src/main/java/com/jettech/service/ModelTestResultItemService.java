package com.jettech.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;

public interface ModelTestResultItemService  extends IService<ModelTestResultItem, Integer>{


	List<ModelTestResultItem> saveAll(List<ModelTestResultItem> list);

	void addBatch(List<ModelTestResultItem> itemList);

	Page<ModelTestResultItem> findTestResultItemByTestResultID(Integer testResultID,String result, Pageable pageable);

	List<ModelTestResultItem> findByTestResultItemID(String ids);

	Page<ModelTestResultItem> findTestResultItemLikeKeyValue(String keyValue,String testResultId, Pageable pageable);

	Page<ModelTestResultItem> findTestResultItemByResult(String result, Pageable pageable);

	List<ModelTestResultItem> findByTestResultID(String id);

	List<ModelTestResultItem> findByTestResultItemIDs(String ids);



}
