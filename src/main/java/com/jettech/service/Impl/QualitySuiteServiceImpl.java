package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import com.jettech.entity.QualityRule;
import com.jettech.entity.QualityTestQuery;
import com.jettech.repostory.QualityRuleRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.vo.QualitySuiteVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static Logger log = LoggerFactory.getLogger(QualitySuiteServiceImpl.class);

	@Autowired
	QualitySuiteRepository qualitySuiteRepository;
	@Autowired
	QualityRuleRepository qualityRuleRepository;
	@Autowired
    QualityTestQueryRepository qualityTestQueryRepository;
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
	@Transactional
	public ResultVO delete(int id) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			QualitySuite qualitySuite = qualitySuiteRepository.findById(id).get();;
			List<QualityTestQuery> qualityTestQuerys=qualityTestQueryRepository.findByQualitySuiteId(qualitySuite.getId());
			if(qualityTestQuerys.isEmpty()){
				qualitySuite.setQualityRules(null);
				qualitySuiteRepository.save(qualitySuite);
				qualitySuiteRepository.deleteById(id);
				return new ResultVO(true, StatusCode.OK, "删除成功", map);
			}else{
				return new ResultVO(false, StatusCode.ERROR, "此集合关联案例禁止删除");
			}
		} catch (Exception e) {
			log.info("异常为"+e);
			return new ResultVO(false, StatusCode.ERROR, "删除失败");

		}
		
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
			QualitySuiteVO qualitySuiteVo=new QualitySuiteVO(qualitySuite);
			qualitySuiteVos.add(qualitySuiteVo);
		}

		map.put("totalElements",pageQualitySuite.getTotalElements());
		map.put("totalPages",pageQualitySuite.getTotalPages());
		map.put("list",qualitySuiteVos);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}




}
