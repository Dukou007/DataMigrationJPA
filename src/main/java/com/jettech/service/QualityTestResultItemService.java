package com.jettech.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.QualityTestResult;
import com.jettech.entity.QualityTestResultItem;

public interface QualityTestResultItemService extends IService<QualityTestResultItem, Integer> {

	Page<QualityTestResultItem> findTestResultItemByTestResultIDAndResultAndSelectValueAndColumnName(
			Integer testResultID, /*String selectValue,*/ String result, /*String columnName,*/ Pageable pageable);


	Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID,/*String result,*/ Pageable pageable);

	Page<QualityTestResultItem> findByTestIdAndSign(Integer testResultId,int sign, Pageable pageable);


}
