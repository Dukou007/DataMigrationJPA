package com.jettech.service.Impl;

import com.jettech.entity.QualityRule;
import com.jettech.entity.QualitySuite;
import com.jettech.repostory.QualityRuleRepository;
import com.jettech.repostory.QualitySuiteRepository;
import com.jettech.service.IQualityRuleService;
import com.jettech.vo.QualityRuleVO;
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
public class QualityRuleServiceImpl implements IQualityRuleService {
	@Autowired
    QualityRuleRepository qualityRuleRepository;
	@Autowired
    QualitySuiteRepository qualitySuiteRepository;


	@Override
	public ResultVO add(QualityRuleVO qualityRuleVO) {
		QualityRule qualityRule=new QualityRule();
		BeanUtils.copyProperties(qualityRuleVO, qualityRule);
		qualityRuleRepository.save(qualityRule);
		return new ResultVO(true, StatusCode.OK, "添加或修改成功");
	}

	@Override
	public ResultVO delete(int id) {
		QualityRule qualityRule = qualityRuleRepository.findById(id).get();
		List<QualitySuite> qualitySuites=qualityRule.getQualitySuites();
		//要删除规则，首先删除规则所涉及的集合
		for(QualitySuite qualitySuite:qualitySuites){
			int qualitySuiteId=qualitySuite.getId();
			qualitySuite.setQualityRules(null);
			qualitySuiteRepository.deleteById(qualitySuiteId);
		}
		qualityRuleRepository.deleteById(id);
		return new ResultVO(true, StatusCode.OK, "删除成功");
		

	}

	@Override
	public ResultVO getOneById(int id) {
		QualityRuleVO qualityRuleVo = new QualityRuleVO();
		QualityRule qualityRule = qualityRuleRepository.findById(id).get();
		List<QualitySuite> qualitySuites=qualityRule.getQualitySuites();
		List<Integer> qualitySiteIds=new ArrayList<Integer>();
		for(QualitySuite qualitySuite:qualitySuites){
			qualitySiteIds.add(qualitySuite.getId());
		}
		BeanUtils.copyProperties(qualityRule, qualityRuleVo);
		qualityRuleVo.setQualitySuiteIds(qualitySiteIds);
		return new ResultVO(true, StatusCode.OK, "查询成功", qualityRuleVo);
	}


	@Override
	public ResultVO getAll(int pageNum, int pageSize) {
		Integer QualitySuiteId;
		Map<String,Object> map = new HashMap<String,Object>();
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        Page<QualityRule> pageQualityRule = qualityRuleRepository.findAll(pageRequest);
		List<QualityRuleVO> qualityRuleVos=new ArrayList<QualityRuleVO>();
		 for(QualityRule qualityRule : pageQualityRule){
			 QualityRuleVO qualityRuleVo=new QualityRuleVO();
			 BeanUtils.copyProperties(qualityRule, qualityRuleVo);
			 List<QualitySuite> qualitySuites=new ArrayList<QualitySuite>();
			 List<Integer> qualitySuiteIds=new ArrayList<Integer>();
			 qualitySuites=qualityRule.getQualitySuites();
			 for(QualitySuite qualitySuite:qualitySuites){
				 QualitySuiteId=qualitySuite.getId();
				 qualitySuiteIds.add(QualitySuiteId);
			 }
			 qualityRuleVo.setQualitySuiteIds(qualitySuiteIds);
			 qualityRuleVos.add(qualityRuleVo);
         }
        
         map.put("totalElements",pageQualityRule.getTotalElements());
         map.put("totalPages",pageQualityRule.getTotalPages());
         map.put("list",qualityRuleVos);
         return new ResultVO(true, StatusCode.OK, "查询成功", map);
    }
}
