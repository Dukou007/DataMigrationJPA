package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jettech.entity.QualityRule;
import com.jettech.repostory.QualityRuleRepository;
import com.jettech.vo.QualitySuiteVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.QualitySuite;
import com.jettech.repostory.QualitySuiteRepository;
import com.jettech.service.IQualitySuiteService;

@Service
public class QualitySuiteServiceImpl implements IQualitySuiteService {

	//@Autowired
	//QualitySuiteRepository qualitySuiteRepository;
	@Autowired
	QualitySuiteRepository qualitySuiteRepository;
	@Autowired
	QualityRuleRepository qualityRuleRepository;

	@Override
	public List<QualitySuite> findAll() {
		return qualitySuiteRepository.findAll();
	}

	@Override
	public List<QualitySuite> saveAll(List<QualitySuite> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void save(QualitySuite entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub

	}

	@Override
	public QualitySuite findById(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<QualitySuite> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}





	@Override
	public ResultVO add(List<QualitySuiteVO> qualitySuiteVOs) {
		for(QualitySuiteVO qualitySuiteVO:qualitySuiteVOs){
			QualitySuite qualitySuite=new QualitySuite();
			BeanUtils.copyProperties(qualitySuiteVO, qualitySuite);
			String ids=qualitySuiteVO.getQualityRuleIds();
			if(ids!=null){
				List<QualityRule> qualityRules=new ArrayList<QualityRule>();
				String[] selectIds=ids.split(",");
				for(String id:selectIds){
					QualityRule q=qualityRuleRepository.findById(Integer.valueOf(id)).get();
					qualityRules.add(q);
				}
				qualitySuite.setQualityRules(qualityRules);
			}
			qualitySuiteRepository.save(qualitySuite);
		}

		return new ResultVO(true, StatusCode.OK, "添加或修改成功");
	}

	@Override
	public ResultVO delete(int id) {
		QualitySuite qualitySuite = qualitySuiteRepository.findById(id).get();;
		qualitySuite.setQualityRules(null);
		qualitySuiteRepository.save(qualitySuite);
		qualitySuiteRepository.deleteById(id);
		Map<String, Object> map = new HashMap<String, Object>();
		return new ResultVO(true, StatusCode.OK, "删除成功", map);
	}

	@Override
	public ResultVO getOneById(int id) {
		QualitySuiteVO qualitySuiteVo = new QualitySuiteVO();
		QualitySuite qualitySuite = qualitySuiteRepository.findById(id).get();
		List<QualityRule> qualityRules=qualitySuite.getQualityRules();
		StringBuilder qualityRuleIds=new StringBuilder();
		for (int i = 0; i < qualityRules.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == qualityRules.size() - 1) {
				qualityRuleIds.append(qualityRules.get(i).getId());
			} else {
				qualityRuleIds.append(qualityRules.get(i).getId());
				qualityRuleIds.append(",");
			}
		}
		BeanUtils.copyProperties(qualitySuite, qualitySuiteVo);
		qualitySuiteVo.setQualityRuleIds(qualityRuleIds.toString());
		return new ResultVO(true, StatusCode.OK, "查询成功", qualitySuiteVo);
	}
	@Override
	public ResultVO getAll(int pageNum,int pageSize) {
		Map<String,Object> map = new HashMap<String,Object>();
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		Page<QualitySuite> pageQualitySuite = qualitySuiteRepository.findAll(pageRequest);
		List<QualitySuiteVO> qualitySuiteVos=new ArrayList<QualitySuiteVO>();
		for(QualitySuite qualitySuite : pageQualitySuite){
			QualitySuiteVO qualitySuiteVo=new QualitySuiteVO();
			BeanUtils.copyProperties(qualitySuite, qualitySuiteVo);
			List<QualityRule> qualityRules=new ArrayList<QualityRule>();
			StringBuilder qualityRuleIds=new StringBuilder();
			qualityRules=qualitySuite.getQualityRules();
			for (int i = 0; i < qualityRules.size(); i++) {
				// 当循环到最后一个的时候 就不添加逗号,
				if (i == qualityRules.size() - 1) {
					qualityRuleIds.append(qualityRules.get(i).getId());
				} else {
					qualityRuleIds.append(qualityRules.get(i).getId());
					qualityRuleIds.append(",");
				}
			}
			qualitySuiteVo.setQualityRuleIds(qualityRuleIds.toString());
			qualitySuiteVos.add(qualitySuiteVo);
		}

		map.put("totalElements",pageQualitySuite.getTotalElements());
		map.put("totalPages",pageQualitySuite.getTotalPages());
		map.put("list",qualitySuiteVos);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}




}
