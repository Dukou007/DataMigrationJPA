package com.jettech.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jettech.entity.Product;

public interface ProductService extends IService<Product, Integer> {


	Product getByName(String productName);
	
	List<Product> findSubProductList(Integer parentID);


	/**
	 * @Description: 该函数的功能描述
	 *	List<Product>
	 */
	List<Product> findAllByParentID();

	/**
	 * @Description: 该函数的功能描述
	 *	List<Product>
	 */
	List<Product> findSubProductListByParentID();

	List<Product> findRootPruducts();


	/**
	 * @Description: 根据产品名称查找产品并分页
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月24日下午11:58:38
	 */
	Page<Product> findProductByProductName(String productName, Pageable pageable);


}
