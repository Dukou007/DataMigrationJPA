package com.jettech.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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

	/* private List<TestCase> testCases = new ArrayList<>(); */

	// 添加质量的关联表 20190321
	private List<QualityTestCase> qualityTestCases = new ArrayList<>();
	// private Set<QualityTestCase> qualityTestCases;
	private int type;// 集合类型，0迁移，1质量

	/**
	 * 最大并发数量,一个集合内同时执行案例的数量
	 */
	private Integer maxConcurrency;

	public TestSuite() {
		super();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JoinColumn(name = "product_id", nullable = false)
	@ManyToOne
	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	/*
	 * @OneToMany(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "test_suite_id", referencedColumnName = "id")
	 * // @ManyToMany(mappedBy="testSuites",fetch = FetchType.LAZY)
	 * //// @JoinColumn(name = "test_case_id") public List<TestCase>
	 * getTestCases() { return testCases; }
	 * 
	 * public void setTestCases(List<TestCase> testCases) { this.testCases =
	 * testCases; }
	 */

	/*
	 * @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	 * 
	 * @JoinTable(name = "suite_quality_case", joinColumns = @JoinColumn(name =
	 * "test_suite_id", referencedColumnName = "id"), inverseJoinColumns
	 * = @JoinColumn(name = "quality_test_case_id", referencedColumnName =
	 * "id")) public Set<QualityTestCase> getQualityTestCases() { return
	 * qualityTestCases; }
	 * 
	 * public void setQualityTestCases(Set<QualityTestCase> qualityTestCases) {
	 * this.qualityTestCases = qualityTestCases; }
	 */

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinTable(name = "suite_quality_case", joinColumns = @JoinColumn(name = "test_suite_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "quality_test_case_id", referencedColumnName = "id"))
	public List<QualityTestCase> getQualityTestCases() {
		return qualityTestCases;
	}

	public void setQualityTestCases(List<QualityTestCase> qualityTestCases) {
		this.qualityTestCases = qualityTestCases;
	}

	@Override
	public String toString() {
		return "TestSuite [name=" + name + ", product=" + product + ", qualityTestCases=" + qualityTestCases + ", type="
		        + type + "]";
	}

	public Integer getMaxConcurrency() {
		return maxConcurrency;
	}

	public void setMaxConcurrency(Integer maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
	}
}
