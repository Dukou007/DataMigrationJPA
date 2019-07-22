package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jettech.BizException;
import com.jettech.entity.Product;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ProductService;
import com.jettech.vo.ProductVO;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	ProductRepository repository;
	
	@Autowired
	TestSuiteRepository testSuiteRepository;

	@Override
	public List<Product> findAll() {
		return repository.findAll();
	}

	@Override
	public List<Product> saveAll(List<Product> list) {
		return repository.saveAll(list);
	}

	@Override
	@Transactional
	public void save(Product entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public Product findById(Integer id) {
		Optional<Product> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Product getByName(String name) {
		List<Product> list = repository.findByName(name);
		Product entity = null;
		if (list == null || list.size() == 0) {
			entity = new Product(name);
			entity = repository.save(entity);
		} else {
			entity = list.get(0);
		}

		return entity;
	}

	@Override
	public List<Product> findSubProductList(Integer parentID) {
		return repository.findSubProductList(parentID);
	}

	@Override
	public Page<Product> findAllByPage(Pageable pageable) {
		Page<Product> list = repository.findAll(pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}

	}
	
	@Override
	public List<Product> findAllByParentID() {
		return repository.findAllByParentID();
	}

	@Override
	public List<Product> findSubProductListByParentID() {
		return repository.findSubProductListByParentID();
	}

	@Override
	public List<Product> findRootPruducts() {
		return repository.findRootPruducts();
	}
	
	@Override
	public Page<Product> findProductByProductName(String productName, Pageable pageable) {
		Page<Product> list=repository.findProductByProductName(productName,pageable);
		if(list.getSize()>0) {
			return list;
		}else {
			return new PageImpl<Product>(new ArrayList<Product>(), pageable, 0);
		}
	}

	@Override
	public Page<Product> findByProductIdIsNull(Pageable pageable) {
		Page<Product> list = repository.findByProductIdIsNull(pageable);
		if (list != null) {
			return list;
		} else {
			return new PageImpl<Product>(new ArrayList<Product>(), pageable, 0);
		}
	}

	@Override
	public Page<Product> findProductByParentId(Integer parentId, Pageable pageable) throws BizException {
		Page<Product> list = null;
		Product product = repository.getOne(parentId);
		if (product == null || product.equals("")) {
			throw new BizException("不存在父对象");
		} else {
			list = repository.findProductByParentId(parentId, pageable);
		}
		if (list != null) {
			return list;
		} else {
			return new PageImpl<Product>(new ArrayList<Product>(), pageable, 0);
		}
	}

	@Override
	@Transactional
	public void addProduct(ProductVO productVO) throws BizException {
		Product p = new Product();
		// subProduct
		if (productVO.getParentID() != null && productVO.getParentID() > 0) {
			Product parentProduct = repository.getOne(productVO.getParentID());
			if (parentProduct == null | parentProduct.equals("")) {
				throw new BizException("父产品不存在");
			} else {
				BeanUtils.copyProperties(productVO, p);
				parentProduct.getSubProducts().add(p);
				p.setParent(parentProduct);
				// 新增时，创建时间为当前时间，修改时间为null
				p.setCreateTime(new Date());
				p.setEditTime(null);
				repository.save(p);
				repository.saveAndFlush(parentProduct);
			}
		} else {
			// parentProduct
			BeanUtils.copyProperties(productVO, p);
			// 新增时，创建时间为当前时间，修改时间为null
			p.setCreateTime(new Date());
			p.setEditTime(null);
			p.setParent(null);
			repository.save(p);
		}

	}

	@Override
	@Transactional
	public void updateProduct(ProductVO productVO) throws BizException {
		Integer pid = productVO.getId();
		Product p = repository.getOne(pid);
		if (p == null || p.equals("")) {
			throw new BizException("要更新的案例不存在");
		} else if (p.getParent() != null && !p.getParent().equals("")) {
			Product parentProduct = p.getParent();
			// 修改时，创建时间不变，修改时间为当前时间
			if(productVO.getCreateTime()==null) {
				p.setCreateTime(p.getCreateTime());
			}else {
				p.setCreateTime(p.getCreateTime());
			}
			if(productVO.getEditTime()==null) {
				p.setEditTime(new Date());
			}else {
				p.setEditTime(new Date());
			}
			if(productVO.getCreateUser()==null) {
				p.setCreateUser(p.getCreateUser());
			}else {
				p.setCreateUser(productVO.getCreateUser());
			}
			if(productVO.getEditUser()==null) {
				p.setEditUser(p.getEditUser());
			}else {
				p.setEditUser(productVO.getEditUser());
			}
			if(productVO.getName()==null) {
				p.setName(p.getName());
			}else {
				p.setName(productVO.getName());
			}
			p.setParent(parentProduct);
			repository.save(p);
			repository.saveAndFlush(parentProduct);
		} else {
			// 修改时，创建时间不变，修改时间为当前时间
			if(productVO.getCreateTime()==null) {
				p.setCreateTime(p.getCreateTime());
			}else {
				p.setCreateTime(p.getCreateTime());
			}
			if(productVO.getEditTime()==null) {
				p.setEditTime(new Date());
			}else {
				p.setEditTime(new Date());
			}
			if(productVO.getCreateUser()==null) {
				p.setCreateUser(p.getCreateUser());
			}else {
				p.setCreateUser(productVO.getCreateUser());
			}
			if(productVO.getEditUser()==null) {
				p.setEditUser(p.getEditUser());
			}else {
				p.setEditUser(productVO.getEditUser());
			}
			if(productVO.getName()==null) {
				p.setName(p.getName());
			}else {
				p.setName(productVO.getName());
			}
//			BeanUtils.copyProperties(productVO, p);
			p.setParent(null);
			repository.save(p);
		}

	}

	@Override
	@Transactional
	public void deleteProduct(String ids) throws BizException {
		if(StringUtils.isNotBlank(ids)) {
			//切割id
			String[] productIds = ids.split(",");
			for (String productId : productIds) {
				//得到每一个的id
				int id = Integer.parseInt(productId);
				//得到对应的product
				Product p = repository.findById(id).get();
				//产品不存在
				if(p==null|p.equals("")) {
					throw new BizException("id为"+id+"的对象不存在");
					//产品是子产品，需要将testsuite的productid设置为null，然后删除
				}else if(p.getParent()!=null&&!p.getParent().equals("")){
					Product parent = p.getParent();
					List<TestSuite> testSuites = p.getTestSuites();
					for (TestSuite testSuite : testSuites) {
						testSuite.setProduct(null);
						testSuiteRepository.save(testSuite);
					}
					p.setTestSuites(null);
					repository.deleteById(id);
					//产品是父产品，先找出子产品，将子产品的testsuite的id设置为null，将子产品删除；找出父产品对应的testsuite，置外键为null，然后删除。
				}else {
					// 得到子产品
					List<Product> subProducts = p.getSubProducts();
					//如果子产品为空，得到对应的suite，如果suite不为空，值productid为null；
					if(subProducts==null||subProducts.size()==0) {
						List<TestSuite> testSuites = p.getTestSuites();
						if(testSuites!=null&&testSuites.size()>0) {
							for (TestSuite testSuite : testSuites) {
								testSuite.setProduct(null);
								testSuiteRepository.save(testSuite);
							}	
						}
						p.setTestSuites(null);
						repository.save(p);
						repository.delete(p);
					}else {
						// 得到每一个子产品，然后循环遍历其中的testsuite集合，去每一个集合，将其productID设为null，然后删除每一个子产品。再删除父产品
						for (Product product : subProducts) {
							List<TestSuite> testSuites = product.getTestSuites();
							for (TestSuite testSuite : testSuites) {
								testSuite.setProduct(null);
								testSuiteRepository.save(testSuite);
							}
							product.setTestSuites(null);
							repository.delete(product);
						}
						repository.delete(p);
					}
				}
			}
		}
		
	}

	@Override
	public Product findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Product> findByNameLike(String name) {
		// TODO Auto-generated method stub
		return repository.findByNameLike(name);
	}

	
}
