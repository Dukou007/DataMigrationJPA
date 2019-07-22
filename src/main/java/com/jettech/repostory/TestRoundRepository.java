package com.jettech.repostory;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import com.jettech.entity.TestSuite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.jettech.entity.TestRound;
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
	Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, Pageable pageable);

    //添加质量方法   20190318
    List<TestRound> findByTestSuiteId(Integer testSuiteId);

    @Modifying
    @Transactional
    @Query(value = "update test_round u set u.success_count = ?2, u.version = u.version + 1,u.end_time = ?3 where u.id = ?1 and u.version = ?4", nativeQuery = true)
    int updateWithVersion(int id, int successCount, Date endTime , int version);
    //查询表名
    @Query(value = "select distinct  b.name  from test_round  t inner join test_suite e "
    		+ "on e.id=t.test_suite_id inner join  suite_quality_case c  on c.test_suite_id=e.id "
    		+ "inner join  quality_test_case s on s.id =c.quality_test_case_id "
    		+"inner join quality_test_query y on y.id=s.quality_test_query_id"
			+"	inner join test_table b on b.id=y.test_table_id "
    		+ "where t.id=?2 and t.test_suite_id=?1 order by b.name",nativeQuery=true)
    List<String> findBysuiteIdAndRoundId(int testSuiteId, int testRoundId);


    //查询本表名的案例个数
    @Query(value = "select count(1) from quality_test_case r ,test_round d ,suite_quality_case e ,quality_test_query y ,test_table b"
    		+ " where d.test_suite_id=e.test_suite_id and e.quality_test_case_id=r.id and  y.id=r.quality_test_query_id"
    		+" and b.id=y.test_table_id and b.name=?3"
    		+ " and d.id=?2 and d.test_suite_id=?1",nativeQuery=true)
    int findBysIdAndRIdAndTName(int testSuiteId, int testRoundId,String tableName);

    //查询本表名的案例的失败个数
    @Query(value = "select count(1) from quality_test_result t ,suite_quality_case c where "
    		+ "c.quality_test_case_id=t.test_case_id  and test_case_id in(select s.id from "
    		+" quality_test_case s ,quality_test_query y,test_table b where y.id=s.quality_test_query_id "
    		+ "and y.test_table_id=b.id and b.name=?3) and test_round_id=?2 and t.result=0 and c.test_suite_id=?1",nativeQuery=true)
    int getCountBysIdAndRIdAndTName(int testSuiteId, int testRoundId,String tableName);
  

    int deleteByTestSuite(TestSuite testSuite);

}
