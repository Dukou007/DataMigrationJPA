package com.jettech.repostory;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {


	List<Product> findByName(String name);

	@Query(value = "select * from Product t where t.parent_id =?1", nativeQuery = true)
	List<Product> findSubProductList(Integer parentID);

	@Query(value = "SELECT  B.parent_id,GROUP_CONCAT(B.id) id,GROUP_CONCAT(B.name) `name` FROM " + 
			"(" + 
			"	SELECT D.id parent_id,D.`name` PNAME,A.id,A.`name` FROM" + 
			"	(" + 
			"		SELECT P.id,P.`name`,P.parent_id FROM product P WHERE parent_id IN(SELECT ID FROM product)" + 
			"	) A" + 
			"	INNER JOIN (SELECT C.id,C.`name` FROM  product C WHERE C.parent_id IS NULL) D" + 
			"	ON A.parent_id=D.id" + 
			") B" + 
			"GROUP BY parent_id",nativeQuery = true)
	List<Product> findAllByParentID();

	/**
	 * @Description: 查询父产品为null的子产品（product的最顶层）
	 * @author zhou_xiaolong 
	 */
	@Query(value="SELECT * FROM product WHERE parent_id is null",nativeQuery=true)
	List<Product> findSubProductListByParentID();

	@Query(value = "select * from Product t where t.parent_id is null", nativeQuery = true)
	List<Product> findRootPruducts();

	/**
	 * @Description:  根据产品名称查找产品
	 * @tips:null
	 * @author:zhou_xiaolong in 2019年2月19日下午2:20:19
	 * @param pageable 
	 */
	@Query(value="select * FROM product p WHERE p.parent_id is null AND p.name LIKE CONCAT('%',?1,'%') ",countQuery="select count(*) from product p where p.parent_id is null and p.name like CONCAT('%',?1,'%')",nativeQuery=true)
	Page<Product> findProductByProductName(String productName, Pageable pageable);

	@Transactional(timeout = 30000)
	@Query(value = "SELECT * FROM product WHERE parent_id is null", countQuery = "SELECT count(*) FROM product WHERE parent_id is null", nativeQuery = true)
	Page<Product> findByProductIdIsNull(Pageable pageable);

	@Transactional(timeout = 30000)
	@Query(value = "SELECT * FROM product  WHERE parent_id=?1", countQuery = "SELECT count(*) FROM product  WHERE parent_id=?1", nativeQuery = true)
	Page<Product> findProductByParentId(Integer parentId, Pageable pageable);

	
	@Transactional(timeout = 30000)
	@Query(value = "SELECT * FROM product  WHERE name=?1",  nativeQuery = true)
	Product findByProductName(String name);

	List<Product> findByNameLike(String name);
	@Query(value="select * from  product p join test_suite ts on p.id = ts.product_id join test_suite_case tsc on ts.id =  tsc.suite_id\r\n" + 
			" join test_case tc on tc.id = tsc.case_id \r\n" + 
			"where tc.id=?1",nativeQuery = true)
	List<Product> findProductByCaseId(int caseId);

}
