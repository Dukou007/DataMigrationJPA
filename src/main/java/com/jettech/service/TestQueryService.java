package com.jettech.service;

import com.jettech.entity.TestQuery;

import java.util.List;

public interface TestQueryService extends IService<TestQuery, Integer> {

    //根据 caseId查询对应的 TestQuery 20190121
    List<TestQuery> getByCaseId(Integer caseId);

	/**
	 * @Description: 根据testCaseID查询testquery对象
	 * 
	 * 
	 * zhou_xiaolong in 2019年2月18日
	 */
	List<TestQuery> findByCaseId(Integer testCaseID);



}
