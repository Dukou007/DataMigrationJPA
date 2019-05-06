package com.jettech.repostory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.TestSuite;
@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, Integer> {
	List<TestSuite> findByName(String name);

	@Query(value = "select t from TestSuite t where t.name=?1 and t.product.name=?2")
	List<TestSuite> findByName(@Param("testSuiteName") String testSuiteName, @Param("productName") String productName);

	@Query(value = "select t.* from test_suite t where t.name like '%?1%'", nativeQuery = true)
	List<TestSuite> findByNameLikeName(String SuiteName);

	// 模糊查询加分页 新加 20190123
	@Transactional(timeout=30000)
	Page<TestSuite> findByNameLike(String name, Pageable pageable);

	/**
	 * @Description: 根据产品名称查询
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月21日下午6:33:44
	 */
	@Query(value="SELECT t.* FROM test_suite t WHERE 1=1 AND t.name LIKE %?1%",nativeQuery=true)
	List<TestSuite> finTestSuiteByName(String name);

	/**
	 * @Description: 根据名称查找测试集并分页
	 * @tips:
	 * @author: zhou_xiaolong in 2019年2月22日下午4:14:10
	 */
	
	@Query(value = "SELECT *  FROM test_suite t WHERE 1=1 AND t.name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_suite  where  name like CONCAT('%',?1,'%') ",nativeQuery = true)
	Page<TestSuite> getAllTestSuiteByPage(String name, Pageable pageable);

	/**
	 * @Description: 根据产品的ID查找测试集并分页
	 * @Tips: null;
	 * @State: being used
	 * @author: zhou_xiaolong in 2019年2月24日下午5:40:02
	 */
	 @Query(value = "select * from test_suite t where t.product_id =?1", countQuery = "SELECT count(*) FROM test_suite t where t.product_id =?1",nativeQuery = true)
	Page<TestSuite> getAllTestSuiteByProductID(Integer productID, Pageable pageable);

	/**
	 * @Description: 查找所有的测试集并分页
	 * @Tips: null;
	 * @State: being used 
	 * @author: zhou_xiaolong in 2019年2月24日下午7:17:38
	 */
	 @Query(value = "select * from test_suite ", countQuery = "select count(*) from test_suite ",nativeQuery = true) 
	Page<TestSuite> getTestSuiteList(Pageable pageable);

	/**
	 * 根据testSuiteName查询TestSuite  来自质量  20190320
	 * @param testSuiteName
	 * @return
	 */
	 @Query(value="SELECT * FROM test_suite t WHERE t.name=?1" ,nativeQuery=true)
	TestSuite findBySuiteName(String testSuiteName);
	 
	 
	List<TestSuite> findByProductId(Integer productId);

	@Query(value="SELECT * FROM `test_suite` t WHERE t.`name`=?1 AND t.product_id=?2",nativeQuery=true)
	List<TestSuite> findByNameAndProductId(String name, Integer productID);
	@Query(value = "SELECT *  FROM test_suite  WHERE 1=1 and type=?2 AND name LIKE CONCAT('%',?1,'%') ", countQuery = "select count(*) from test_suite  where type=?2 and  name like CONCAT('%',?1,'%') ",nativeQuery = true)
	Page<TestSuite> findByNameAndTypeByPage(String name,int type, Pageable pageable);

	List<TestSuite> findByProductIdAndType(Integer productId,Integer type);
}
