package com.jettech.service;

import com.jettech.entity.QualityTestQuery;
import com.jettech.vo.ResultVO;

public interface IQualityTestQueryService extends IService<QualityTestQuery, Integer> {

    //添加于20190128
    public ResultVO findSourceByQueryId(Integer testQueryId);

    QualityTestQuery getOneById(Integer id);
}
