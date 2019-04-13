package com.jettech.service.Impl;

import com.jettech.entity.QualityCodeMap;
import com.jettech.repostory.QualityCodeMapRepository;
import com.jettech.service.IQualityCodeMapService;
import com.jettech.vo.QualityCodeMapVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QualityCodeMapServiceImpl implements IQualityCodeMapService {

	@Autowired
	private QualityCodeMapRepository qualityCodeMapRepository;
	@Override
	public ResultVO insert(QualityCodeMapVO qualityCodeMapVO) {
		QualityCodeMap qualityCodeMap =new QualityCodeMap();
		BeanUtils.copyProperties(qualityCodeMapVO, qualityCodeMap);
		qualityCodeMapRepository.save(qualityCodeMap);
		return new ResultVO(true, StatusCode.OK, "保存或修改成功");
	}

	@Override
	public ResultVO delete(int id) {
		qualityCodeMapRepository.deleteById(id);
		return new ResultVO(true, StatusCode.OK, "删除成功");
				
	}

	@Override
	public ResultVO getOneById(int id) {
		QualityCodeMap qualityCodeMap = qualityCodeMapRepository.findById(id).get();
		QualityCodeMapVO qualityCodeMapVO =new QualityCodeMapVO();
		BeanUtils.copyProperties(qualityCodeMap, qualityCodeMapVO);
		return new ResultVO(true, StatusCode.OK, "查询成功", qualityCodeMapVO);
	}
	@Override
	public ResultVO getAll(int pageNum, int pageSize) {
		Map<String,Object> map = new HashMap<String,Object>();
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Page<QualityCodeMap> codeMap = qualityCodeMapRepository.findAll(pageRequest);
        List<QualityCodeMapVO> qualityCodeMapVos =new ArrayList<QualityCodeMapVO>();
        for(QualityCodeMap code:codeMap){
    		QualityCodeMapVO qualityCodeMapVO =new QualityCodeMapVO();
    		BeanUtils.copyProperties(code, qualityCodeMapVO);
    		qualityCodeMapVos.add(qualityCodeMapVO);
        }
         map.put("totalElements",codeMap.getTotalElements());
         map.put("totalPages",codeMap.getTotalPages());
         map.put("list", qualityCodeMapVos);
         return new ResultVO(true, StatusCode.OK, "查询成功", map);
	    }
}
