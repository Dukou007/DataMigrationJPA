package com.jettech.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "test_suite")
public class TestSuite extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7111923967906970273L;

	private String name;

	private Product product;

	/*private List<TestCase> testCases = new ArrayList<>();*/

	//添加质量的关联表 20190321
	private List<QualityTestCase> qualityTestCases = new ArrayList<>();
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JoinColumn(name = "product_id", nullable = true)
	@ManyToOne
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

/*	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_suite_id", referencedColumnName = "id")
	// @ManyToMany(mappedBy="testSuites",fetch = FetchType.LAZY)
	//// @JoinColumn(name = "test_case_id")
	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}*/

	//添加质量关联关系 20190321
	//@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST) //,optional = true
	//@JoinColumn(name = "quality_test_suite_id", referencedColumnName = "id")
	@OneToMany(mappedBy="testSuite",fetch = FetchType.LAZY)
	public List<QualityTestCase> getQualityTestCases() {
		return qualityTestCases;
	}

	public void setQualityTestCases(List<QualityTestCase> qualityTestCases) {
		this.qualityTestCases = qualityTestCases;
	}

	@Override
	public String toString() {
		return "TestSuite [name=" + name + ", product=" + product + ", qualityTestCases=" + qualityTestCases + "]";
	}

	

	



}
