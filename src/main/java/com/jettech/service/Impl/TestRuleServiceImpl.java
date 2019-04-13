/**
 * 
 */
package com.jettech.service.Impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestRule;
import com.jettech.repostory.TestRuleRepository;
import com.jettech.service.TestRuleService;

/**
 *  @author Eason007
 *	@Description: TestRuleServiceImpl
 *  @date: 2019年2月2日 上午11:21:04 
 */
@Service
public class TestRuleServiceImpl implements TestRuleService {

	private TestRuleRepository testRuleRepository;
	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAll()
	 */
	@Override
	public List<TestRule> findAll() {
		// TODO Auto-generated method stub
		return testRuleRepository.findAll();
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#saveAll(java.util.List)
	 */
	@Override
	public List<TestRule> saveAll(List<TestRule> list) {
		// TODO Auto-generated method stub
		return testRuleRepository.saveAll(list);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#save(java.lang.Object)
	 */
	@Override
	public void save(TestRule entity) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#delete(java.lang.Object)
	 */
	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findById(java.lang.Object)
	 */
	@Override
	public TestRule findById(Integer id) {
		// TODO Auto-generated method stub
		return testRuleRepository.getOne(id);
	}

	/* (non-Javadoc)
	 * @see com.jettech.service.IService#findAllByPage(org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<TestRule> findAllByPage(Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

}
