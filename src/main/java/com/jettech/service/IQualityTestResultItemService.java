package com.jettech.service;

import com.jettech.entity.QualityTestResultItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IQualityTestResultItemService {

	List<QualityTestResultItem> findAll();

	List<QualityTestResultItem> saveAll(List<QualityTestResultItem> list);

	QualityTestResultItem getOneById(Integer id);

	void delete(Integer id);

	void save(QualityTestResultItem entity);

	void addBatch(List<QualityTestResultItem> itemList);

	Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID, Pageable pageable);

	List<QualityTestResultItem> findByTestResultItemIDs(String ids);

	List<QualityTestResultItem> findByTestResultID(String id);

}
