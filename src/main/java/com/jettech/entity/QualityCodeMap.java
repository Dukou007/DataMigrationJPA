package com.jettech.entity;

import com.alibaba.fastjson.annotation.JSONField;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "quality_code_map")
public class QualityCodeMap extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6230519510814815417L;

	private String name;
	private List<QualityCodeMapItem> items;
	private QualityTestPoint qualityTestPoint;
	// private List<TestMap> testMaps;

//	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//	@JoinColumn(name = "code_map_id", referencedColumnName = "id")
//	@JSONField(serialize = false)
//	public List<TestMap> getTestMaps() {
//		return testMaps;
//	}
//
//	public void setTestMaps(List<TestMap> testMaps) {
//		this.testMaps = testMaps;
//	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "quality_code_map_id", referencedColumnName = "id")
	@JSONField(serialize = false)
	public List<QualityCodeMapItem> getItems() {
		return items;
	}

	public void setItems(List<QualityCodeMapItem> items) {
		this.items = items;
	}

	//@OneToOne(optional=false)
	@OneToOne(mappedBy = "qualityCodeMap")
	public QualityTestPoint getQualityTestPoint() {
		return qualityTestPoint;
	}

	public void setQualityTestPoint(QualityTestPoint qualityTestPoint) {
		this.qualityTestPoint = qualityTestPoint;
	}

}
