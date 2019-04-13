package com.jettech.service;

import com.jettech.vo.QualitySuiteVO;
import com.jettech.vo.ResultVO;

import java.util.List;

public interface IQualitySuiteService {
	public ResultVO add(List<QualitySuiteVO> qualitySuiteVOs);
	public ResultVO delete(int id);
	public ResultVO getOneById(int id);
	public ResultVO getAll(int pageNum, int pageSize);
}
