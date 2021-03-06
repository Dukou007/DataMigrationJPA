package com.jettech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.entity.Product;
import com.jettech.service.ProductService;
import com.jettech.vo.ProductVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "/product")
@Api(value = "ProductController|用于产品模块的服务")
public class ProductController {

	private static Logger log = LoggerFactory.getLogger(ProductController.class);

	@Autowired
	private ProductService productService;

	/**
	 * @Description: 根据所有产品查询并分页
	 * @tips: pageHelper不能使用;
	 * 
	 * @author:zhou_xiaolong in 2019年2月20日下午12:28:24
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllProductListByPage", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "根据所有产品查询并分页", notes = "返回的结果集封装在resultmap中")
	public ResultVO getAllProductListByPage(
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		Page<Product> productList;
		ArrayList<ProductVO> productVOList;
		try {
			productList = productService.findAllByPage(pageable);
			productVOList = new ArrayList<ProductVO>();
			if (productList.getSize() > 0) {
				for (Product product : productList) {
					if (product.getParent() != null) {
						ProductVO productVO = new ProductVO();
						BeanUtils.copyProperties(product, productVO);
						productVO.setParentID(product.getParent().getId());
						productVO.setParentName(product.getParent().getName());
						productVOList.add(productVO);
					} else {
						ProductVO productVO = new ProductVO();
						BeanUtils.copyProperties(product, productVO);
						productVO.setParentID(null);
						productVO.setParentName(null);
						productVOList.add(productVO);
					}
					resultMap.put("totalElements", productList.getTotalElements());
					resultMap.put("totalPages", productList.getTotalPages());
					resultMap.put("list", productVOList);

				}
			}

			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (BeansException e) {
			log.error("查询失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}

	/**
	 * @Description: 查询所有产品
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/findAllProduct", method = RequestMethod.POST)
	@ApiOperation(value = "查询所有产品", notes = "返回的结果集封装在resultmap中")
	public ResultVO findAllProduct() {
		List<Product> productList;
		List<ProductVO> productVOList = new ArrayList<ProductVO>();
		try {
			productList = productService.findAll();
			for (Product product : productList) {
				ProductVO vo = new ProductVO();
				BeanUtils.copyProperties(product, vo);
				productVOList.add(vo);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", productVOList);
		} catch (BeansException e) {
			log.error("查询失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}

	/**
	 * @Description: 新增子产品
	 * @tips:null
	 * 
	 * @author:zhou_xiaolong in 2019年2月20日下午3:19:56
	 */
	@ResponseBody
	@RequestMapping(value = "/addSubProduct", produces = {
			"application/json;charset=UTF-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "新增子产品", notes = "需要判断productID")
	@ApiImplicitParam(name = "productVO", value = "productVO实体", required = true, paramType = "ProductVO")
	public ResultVO addSubProduct(@RequestBody ProductVO productVO) {
		try {
			Product product = new Product();
			BeanUtils.copyProperties(productVO, product);
			if (productVO.getParentID() > 0) {
				Product parent = productService.findById(productVO.getParentID());
				product.setParent(parent);
				productService.save(product);
				return new ResultVO(true, StatusCode.OK, "新增成功");
			} else {
				return new ResultVO(false, StatusCode.ERROR, "新增失败");
			}
		} catch (BeansException e) {
			log.error("新增失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败");
		}
	}

	/**
	 * @Description: 查询所有产品列表
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月21日上午10:07:58
	 * @return
	 *//*
		 * @RequestMapping(value="findAllProduct",method=RequestMethod.GET) public
		 * ResultVO findAllProduct() { try { HashMap<String, Object> map = new
		 * HashMap<String,Object>(); List<Product> list = productService.findAll();
		 * ArrayList<ProductVO> productVOList = new ArrayList<ProductVO>(); for (Product
		 * product : list) { ProductVO productVO = new ProductVO(product);
		 * productVOList.add(productVO); } map.put("list", productVOList); return new
		 * ResultVO(true,StatusCode.OK,"查询成功",map); } catch (Exception e) {
		 * log.error("查询所有产品失败信息为：",e); return new
		 * ResultVO(false,StatusCode.ERROR,"查询失败"); } }
		 */

	/**
	 * @Description: 根据名称查找产品
	 * @Tips: null;
	 * @State: drop
	 * @author:zhou_xiaolong in 2019年2月19日下午12:35:42
	 */
	@ResponseBody
	@RequestMapping(value = "/findProductByProductName", produces = {
			"application/json;character=UTF-8" }, method = RequestMethod.GET)
	@ApiOperation(value = "根据产品名称查找产品", notes = "输入产品名称")
	@ApiImplicitParam(paramType = "String", name = "productName", value = "产品名称", required = true, dataType = "String")
	public ResultVO findProductByProductName(
			@RequestParam(value = "productName", defaultValue = "", required = true) String productName,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<Product> products = productService.findProductByProductName(productName, pageable);
			List<ProductVO> productVOs = new ArrayList<ProductVO>();
			if (products.getSize() > 0) {
				for (Product product : products) {
					ProductVO vo = new ProductVO();
					BeanUtils.copyProperties(product, vo);
					if (product.getParent() != null) {
						vo.setParentID(product.getParent().getId());
						vo.setParentName(product.getParent().getName());
					}
					productVOs.add(vo);
					resultMap.put("totalPages", products.getTotalPages());
					resultMap.put("totalElements", products.getTotalElements());
					resultMap.put("list", productVOs);
				}

				return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
			} else {
				return new ResultVO(false, StatusCode.ERROR, "查询失败");
			}
		} catch (BeansException e) {
			log.error("查询失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}

	}

	/**
	 * @Description: 所有父产品返回当前父产品下的所有子产品，包括父产品当前的字段.
	 * @Tips: 第一个parentID为null。此处写SQL语句时需谨慎;
	 * @State: drop
	 * @author:zhou_xiaolong in 2019年2月24日下午12:39:35
	 */
	/*
	 * @ResponseBody
	 * 
	 * @RequestMapping("/getAllProductAndSubProduct") public ResultVO
	 * getAllProductAndSubProduct() { JSONObject result = new JSONObject();
	 * ArrayList<ProductVO> voList = new ArrayList<ProductVO>(); List<Product>
	 * productList = productService.findAll();// father nodes try { for (Product
	 * product : productList) { ProductVO productVO = new ProductVO();
	 * BeanUtils.copyProperties(product, productVO);
	 * 
	 * productVO.setId(product.getId()); productVO.setName(product.getName());
	 * productVO.setCreateTime(product.getCreateTime());
	 * productVO.setCreateUser(product.getCreateUser());
	 * productVO.setEditTime(product.getEditTime());
	 * productVO.setEditUser(product.getEditUser()); // children nodes // if
	 * (product.getSubProducts() != null) { // List<Product> subProducts =
	 * product.getSubProducts(); // ArrayList<ProductVO> subProductVOs = new //
	 * ArrayList<ProductVO>(); // for (Product subProduct : subProducts) { //
	 * ProductVO subProductVO = new ProductVO(); //
	 * BeanUtils.copyProperties(subProduct, subProductVO); //
	 * subProductVO.setId(subProduct.getId()); //
	 * subProductVO.setParentID(product.getId()); //
	 * subProductVO.setParentName(product.getName()); //
	 * subProductVOs.add(subProductVO); // productVO.setSubProducts(subProductVOs);
	 * // } // // }
	 * 
	 * voList.add(productVO); result.put("state", "1"); // result.put("rows",
	 * voList); }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); result.put("state", "0"); }
	 * return new ResultVO(true, StatusCode.OK, "查询成功", voList); }
	 */

	/**
	 * @Description: 修改产品
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月19日下午12:41:00
	 */
	@ResponseBody
	@RequestMapping(value = "/updateProduct/{id}", produces = {
			"application/json;character/UTF-8" }, method = RequestMethod.PUT)
	@ApiOperation(value = "修改产品", notes = "修改名称即可")
	@ApiImplicitParams({
			@ApiImplicitParam(paramType = "Integer", name = "id", value = "产品ID", required = true, dataType = "Integer"),
			@ApiImplicitParam(paramType = "ProductVO", name = "productVO", value = "productVO实体", required = true, dataType = "ProductVO")

	})
	public ResultVO updateProduct(@PathVariable Integer id, @RequestBody ProductVO productVO) {
		try {
			if (id != productVO.getId()) {
				return new ResultVO(false, StatusCode.ERROR, "请保持id和被测系统的id一致");
			}
			Product p = productService.findById(id);
			if (p == null || p.equals("")) {
				return new ResultVO(false, StatusCode.ERROR, "要跟新的被测系统不存在，ID值：" + id);
			} else {
				productService.updateProduct(productVO);
				return new ResultVO(true, StatusCode.OK, "更新成功");
			}
		} catch (Exception e) {
			log.error("更新出错，被测系统的ID：" + id, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "更新失败" + e.getLocalizedMessage());
		}
	}

	/**
	 * @Description: 根据id删除产品
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月18日下午12:41:44
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteProduct/{ids}", method = RequestMethod.DELETE)
	@ApiOperation(value = "删除产品", notes = "根据id删除产品，可批量删除")
	@ApiImplicitParam(paramType = "Integer", name = "ids", value = "产品的ID", required = true, dataType = "Integer")
	public ResultVO deleteProduct(@PathVariable String ids) {
		try {
			if (StringUtils.isNotBlank(ids)) {
				String[] productIds = ids.split(",");
				for (String productId : productIds) {
					int id = Integer.parseInt(productId);
					Product p = productService.findById(id);
					if (p == null || p.equals("")) {
						return new ResultVO(false, StatusCode.ERROR, "案例的id为" + id + "的案例不存在");
					}
				}
			}
			productService.deleteProduct(ids);
			return new ResultVO(true, StatusCode.OK, "删除成功");
		} catch (Exception e) {
			log.error("删除失败：",e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "删除失败");
		}
	}

	/**
	 * @Description: 新增产品
	 * @Tips: null;
	 * @State: being used
	 * @author:zhou_xiaolong in 2019年2月24日下午3:28:49
	 */
	@ResponseBody
	@RequestMapping(value = "/addProduct", produces = {
			"application/json;character=utf-8" }, method = RequestMethod.POST)
	@ApiOperation(value = "新增产品", notes = "处理父产品需谨慎")
	@ApiImplicitParam(name = "productVO", value = "productVO实体", required = true, paramType = "ProductVO")
	public ResultVO addProduct(@RequestBody ProductVO productVO) {
		try {
			if(productVO.getName().trim()==null||productVO.getName().trim().equals("")) {
				return new ResultVO(false, StatusCode.ERROR, "请输入合理的被测系统名称");
			}
			Integer parentID = productVO.getParentID();
			if(parentID!=null) {
				Product parentProduct = productService.findById(parentID);
				if (parentProduct.getParent()!=null) {
					return new ResultVO(false, StatusCode.ERROR, "不可以新增第三级子系统");
				}
			}
			Product product = productService.findByName(productVO.getName());
			if (product != null) {
				return new ResultVO(false, StatusCode.ERROR, "新增失败，已存在相同名称的被测系统");
			}
			productService.addProduct(productVO);
			return new ResultVO(true, StatusCode.OK, "新增成功");
		} catch (Exception e) {
			log.error("新增报错：" + productVO, e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "新增失败" + e.getLocalizedMessage());
		}

	}

	/**
	 * 查询所有的父产品
	 * 
	 * @return
	 */
	@RequestMapping(value = "findAllParentProduct", method = RequestMethod.GET)
	public ResultVO findAllParentProduct(
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<Product> productList = productService.findByProductIdIsNull(pageable);
			ArrayList<ProductVO> voList = new ArrayList<ProductVO>();
			for (Product product : productList) {
				ProductVO vo = new ProductVO(product);
				voList.add(vo);
			}
			resultMap.put("totalPages", productList.getTotalPages());
			resultMap.put("totalElements", productList.getTotalElements());
			resultMap.put("list", voList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (Exception e) {
			log.error("查询所有的父产品报错：", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败");
		}
	}

	/**
	 **根据父产品的id查询所有的子产品
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "findProductByParentId", method = RequestMethod.GET)
	public ResultVO findProductByParentId(@RequestParam Integer parentId,
			@RequestParam(value = "pageNum", defaultValue = "1", required = false) Integer pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize) {
		HashMap<String, Object> resultMap = new HashMap<String, Object>();
		Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
			Page<Product> productList = productService.findProductByParentId(parentId, pageable);
			ArrayList<ProductVO> voList = new ArrayList<ProductVO>();
			for (Product product : productList) {
				ProductVO vo = new ProductVO(product);
				voList.add(vo);
			}
			resultMap.put("totalPages", productList.getTotalPages());
			resultMap.put("totalElements", productList.getTotalElements());
			resultMap.put("list", voList);
			return new ResultVO(true, StatusCode.OK, "查询成功", resultMap);
		} catch (Exception e) {
			log.error("查询所有的父产品报错：", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "查询失败" + e.getLocalizedMessage());
		}
	}

}
