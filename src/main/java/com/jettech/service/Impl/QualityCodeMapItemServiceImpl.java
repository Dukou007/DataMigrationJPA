package com.jettech.service.Impl;

import com.jettech.entity.QualityCodeMap;
import com.jettech.entity.QualityCodeMapItem;
import com.jettech.repostory.QualityCodeMapItemRepository;
import com.jettech.repostory.QualityCodeMapRepository;
import com.jettech.service.IQualityCodeMapItemService;
import com.jettech.service.IQualityCodeMapService;
import com.jettech.vo.QualityCodeMapItemVO;
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
public class QualityCodeMapItemServiceImpl implements IQualityCodeMapItemService {
	@Autowired
	private IQualityCodeMapService codeMapService;
	@Autowired
	private QualityCodeMapRepository qualityCodeMapRepository;
	@Autowired
	private QualityCodeMapItemRepository qualityCodeMapItemRepository;

	@Override
	public ResultVO findAllByPage(int pageNum, int pageSize) {
		Map<String, Object> map = new HashMap<String, Object>();
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		Page<QualityCodeMapItem> codeMapItems = qualityCodeMapItemRepository
				.findAll(pageRequest);
		List<QualityCodeMapItemVO> qualityCodeMapItemVOS =new ArrayList<QualityCodeMapItemVO>();
		for(QualityCodeMapItem qualityCodeMapItem :codeMapItems){
			QualityCodeMapItemVO qualityCodeMapItemVO =new QualityCodeMapItemVO();
			BeanUtils.copyProperties(qualityCodeMapItem, qualityCodeMapItemVO);
			if(qualityCodeMapItem.getQualityCodeMap()!=null){
				qualityCodeMapItemVO.setCodeMapId(qualityCodeMapItem.getQualityCodeMap().getId());
			}
			qualityCodeMapItemVOS.add(qualityCodeMapItemVO);
		}
		
		map.put("totalElements", codeMapItems.getTotalElements());
		map.put("totalPages", codeMapItems.getTotalPages());
		map.put("list", qualityCodeMapItemVOS);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

	@Override
	public ResultVO save(QualityCodeMapItemVO qualityCodeMapItemVO) {
		QualityCodeMapItem qualityCodeMapItem = new QualityCodeMapItem();
		BeanUtils.copyProperties(qualityCodeMapItemVO, qualityCodeMapItem);
		if (qualityCodeMapItemVO.getCodeMapId() != null) {
			QualityCodeMap qualityCodeMap = qualityCodeMapRepository.findById(
					qualityCodeMapItemVO.getCodeMapId()).get();
			qualityCodeMapItem.setQualityCodeMap(qualityCodeMap);
		}
		qualityCodeMapItemRepository.save(qualityCodeMapItem);
		return new ResultVO(true, StatusCode.OK, "保存或修改成功");
	}

	@Override
	public ResultVO delete(Integer id) {
		qualityCodeMapItemRepository.deleteById(id);
		return new ResultVO(true, StatusCode.OK, "删除成功");

	}

	@Override
	public ResultVO getOneById(Integer id) {
		QualityCodeMapItemVO qualityCodeMapItemVO = new QualityCodeMapItemVO();
		QualityCodeMapItem qualityCodeMapItem = qualityCodeMapItemRepository.findById(id).get();
		BeanUtils.copyProperties(qualityCodeMapItem, qualityCodeMapItemVO);
		qualityCodeMapItemVO.setCodeMapId(qualityCodeMapItem.getQualityCodeMap().getId());
		return new ResultVO(true, StatusCode.OK, "查询成功", qualityCodeMapItemVO);
	}

	
	@Override
	public ResultVO getAllByCodeMapId(int id) {
		List<QualityCodeMapItem> qualityCodeMapItems = qualityCodeMapItemRepository
				.findByCodeMapId(id);
		List<QualityCodeMapItemVO> qualityCodeMapItemVOS =new ArrayList<QualityCodeMapItemVO>();
		for(QualityCodeMapItem qualityCodeMapItem : qualityCodeMapItems){
			QualityCodeMapItemVO qualityCodeMapItemVO =new QualityCodeMapItemVO();
			BeanUtils.copyProperties(qualityCodeMapItem, qualityCodeMapItemVO);
			qualityCodeMapItemVO.setCodeMapId(id);
			qualityCodeMapItemVOS.add(qualityCodeMapItemVO);
		}
		return new ResultVO(true, StatusCode.OK, "查询成功", qualityCodeMapItemVOS);
	}

}
