package com.jettech.service;

import com.jettech.entity.QualityTestResult;
import org.springframework.data.domain.Page;

public interface IQualityTestReusltService extends IService<QualityTestResult, Integer> {
	QualityTestResult saveOne(QualityTestResult entity);

	// 分页查询，不带条件
	public Page<QualityTestResult> findPage(int pageNum, int pageSize);

	// 分页查询，带条件,必须实现JpaSpecificationExecutor<TestResult>接口
	public Page<QualityTestResult> findPage(QualityTestResult qualityTestResult, int pageNum, int pageSize);

	//迁移过来新加字段  20190318
	QualityTestResult getOneById(Integer id);
}
