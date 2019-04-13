package com.jettech.service;

import com.jettech.entity.QualitySuite;
import com.jettech.entity.QualityTestQuery;
import com.jettech.vo.ResultVO;

import java.util.Map;

public interface IQualityTestPointService {
    public void createCase(Object obj, QualityTestQuery qualityTestQuery, String dbName, String talbeName, String fieldName, QualitySuite qualitySuite);
  

    public ResultVO tableCheckNull(Map<Object, Object> map);
    public ResultVO tableCheckRange(Map<Object, Object> map);
    public ResultVO tableCheckLength(Map<Object, Object> map);
    public ResultVO tableCheckUnique(Map<Object, Object> map);
    public ResultVO tableCheckNotIn(Map<Object, Object> map);
    public ResultVO tableCheckNotF(Map<Object, Object> map);
    public ResultVO tableCheckNonAmountRange(Map<Object, Object> map);
    public ResultVO checkPoint(Map<Object, Object> map);

}
