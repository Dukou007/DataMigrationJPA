package com.jettech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.QualityTestResultItem;

public interface QualityTestResultItemService extends IService<QualityTestResultItem, Integer>{

	Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID, String result, Pageable pageable);
	


}
