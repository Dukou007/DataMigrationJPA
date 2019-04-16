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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "product")
@JsonIgnoreProperties(value={"transportOrders"})  
public class Product extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1104973635565476315L;

	private String name;

	private Product parent;

	private List<Product> subProducts = new ArrayList<>();

	private List<TestSuite> testSuites = new ArrayList<>();

	public Product() {
	}

	public Product(String name) {
		this();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_id")
	public Product getParent() {
		return parent;
	}

	public void setParent(Product parent) {
		this.parent = parent;
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", referencedColumnName = "id")
	public List<Product> getSubProducts() {
		return subProducts;
	}

	public void setSubProducts(List<Product> subProducts) {
		this.subProducts = subProducts;
	}

	@OneToMany(fetch = FetchType.LAZY,cascade=CascadeType.PERSIST)
	@JoinColumn(name = "product_id", referencedColumnName = "id")
	public List<TestSuite> getTestSuites() {
		return testSuites;
	}

	public void setTestSuites(List<TestSuite> testSuites) {
		this.testSuites = testSuites;
	}

}
