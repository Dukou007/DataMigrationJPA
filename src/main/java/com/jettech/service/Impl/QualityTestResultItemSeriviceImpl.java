package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.jettech.repostory.QualityTestResultItemRepository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jettech.entity.QualityTestResultItem;
import com.jettech.repostory.QualityTestRepository;
import com.jettech.service.QualityTestResultItemService;

@Service
public class QualityTestResultItemSeriviceImpl implements QualityTestResultItemService {

	@Autowired
	QualityTestResultItemRepository quantityTestResultItemRepository;
	@Autowired
	QualityTestRepository qualityTestRepository;

	@Override
	public List<QualityTestResultItem> findAll() {
		
		return null;
	}

	@Override
	public List<QualityTestResultItem> saveAll(List<QualityTestResultItem> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(QualityTestResultItem entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public QualityTestResultItem findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<QualityTestResultItem> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

/*	@Override
	public Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID,String result, Pageable pageable) {
		Page<QualityTestResultItem> list = quantityTestResultItemRepository
				.findTestResultItemByTestResultID(testResultID,result, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}
	}*/

	@Override
	public Page<QualityTestResultItem> findTestResultItemByTestResultIDAndResultAndSelectValueAndColumnName(
			Integer testResultID,/* String selectValue,*/ String result, /*String columnName,*/ Pageable pageable) {
		Page<QualityTestResultItem>list=null;
		 Specification<QualityTestResultItem> specification = new Specification<QualityTestResultItem>() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<QualityTestResultItem> root, CriteriaQuery<?> query,
					CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(testResultID>0&&testResultID!=null) {
					predicateList.add(cb.equal(root.get("testResultId"), testResultID));
				}
				/*if(StringUtils.isNotBlank(selectValue)) {
					predicateList.add(cb.equal(root.get("selectValue"), selectValue));
				}*/
				if(StringUtils.isNotBlank(result)) {
					predicateList.add(cb.equal(root.get("result"), result));
				}
				/*if(StringUtils.isNotBlank(columnName)) {
					predicateList.add(cb.equal(root.get("columnName"), columnName));
				}*/
				
				 return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
			}
		};
		list=this.quantityTestResultItemRepository.findAll(specification,pageable);
		return list;
	}

	@Override
	public Page<QualityTestResultItem> findTestResultItemByTestResultID(Integer testResultID, /*String result,*/Pageable pageable) {
		Page<QualityTestResultItem> list=null;
		 Specification<QualityTestResultItem> specification = new Specification<QualityTestResultItem>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<QualityTestResultItem> root, CriteriaQuery<?> query,
						CriteriaBuilder cb) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					if(testResultID>0&&testResultID!=null) {
						predicateList.add(cb.equal(root.get("testResultId"), testResultID));
					}
					/*if(StringUtils.isNotBlank(result)) {
						predicateList.add(cb.equal(root.get("result"), result));
					}*/
					 return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
				}
			};
			list=this.quantityTestResultItemRepository.findAll(specification,pageable);
			return list;
	}

	

}
