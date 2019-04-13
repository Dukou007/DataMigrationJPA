package com.jettech.service;



import com.jettech.vo.QualityCodeMapItemVO;
import com.jettech.vo.ResultVO;

public interface IQualityCodeMapItemService {
	public ResultVO findAllByPage(int pageNum, int pageSize);
	public ResultVO save(QualityCodeMapItemVO qualityCodeMapItemVO);
	public ResultVO getOneById(Integer id);
	public ResultVO delete(Integer id);
	public ResultVO getAllByCodeMapId(int id);
}
