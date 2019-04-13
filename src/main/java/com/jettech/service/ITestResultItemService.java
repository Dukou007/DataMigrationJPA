package com.jettech.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.TestResultItem;

public interface ITestResultItemService  extends IService<TestResultItem, Integer>{


	List<TestResultItem> saveAll(List<TestResultItem> list);

	void addBatch(List<TestResultItem> itemList);

	Page<TestResultItem> findTestResultItemByTestResultID(Integer testResultID, String result, Pageable pageable);

	List<TestResultItem> findByTestResultItemID(String ids);

	Page<TestResultItem> findTestResultItemLikeKeyValue(String keyValue, Pageable pageable);

	Page<TestResultItem> findTestResultItemByResult(String result, Pageable pageable);

	List<TestResultItem> findByTestResultID(String id);

	List<TestResultItem> findByTestResultItemIDs(String ids);

	Page<TestResultItem> findTestResultItemByTestResultId(Integer testResultId,Pageable pageable);


}
