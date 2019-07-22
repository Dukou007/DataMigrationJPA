package com.jettech.service;

import com.jettech.vo.QualityRuleVO;
import com.jettech.vo.ResultVO;

public interface IQualityRuleService {
	public ResultVO add(QualityRuleVO qualityRuleVO);
	public ResultVO delete(int id) throws Exception;
	public ResultVO getOneById(int id);
	public ResultVO getAll(int pageNum, int pageSize);
}
