package com.jettech.repostory;

import com.jettech.entity.TestRound;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TestRoundRepository extends JpaRepository<TestRound, Integer>,JpaSpecificationExecutor<TestRound> {

    //根据 test_suite_id 查询所有数据 20190117  新方法
    @Query(value = "select * from test_round  where test_suite_id=?1",nativeQuery=true)
    List<TestRound> getAmountBySuiteId(int suiteId);

    /**
     * 根据测试集名称模糊查询所有的轮次 20190121
     * @param suiteId
     * @return
     */
    
    @Query(value = "select s.* from test_round s  where s.test_suite_id in (select t.id from test_suite t where t.name like ?1 )",nativeQuery=true)
    Page<TestRound> findBysuiteNameLike(String suiteName, Pageable pageable);
/*
	@Query(value = "select s.* from test_case s  where s.test_suite_id in (select t.id from test_suite t where t.name like CONCAT('%',:suiteName,'%') ) order by ?#{#pageable}",nativeQuery=true)
	Page<TestCase> findBysuiteName(@Param("suiteName") String suiteName,Pageable pageable);
 */
    @Query(value="SELECT * FROM test_round t WHERE t.test_suite_id=?1 ORDER BY t.start_time DESC",countQuery="SELECT COUNT(*) FROM test_round t WHERE t.test_suite_id=?1",nativeQuery=true)
	Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, PageRequest pageable);

    //添加质量方法   20190318
    List<TestRound> findByTestSuiteId(Integer testSuiteId);
}
