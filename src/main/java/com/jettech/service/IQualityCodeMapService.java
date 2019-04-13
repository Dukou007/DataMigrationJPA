package com.jettech.service;


import com.jettech.vo.QualityCodeMapVO;
import com.jettech.vo.ResultVO;


public interface IQualityCodeMapService {
	public ResultVO insert(QualityCodeMapVO qualityCodeMapVO);
	public ResultVO delete(int id);
	public ResultVO getOneById(int id);
	public ResultVO getAll(int pageNum, int pageSize);
}
