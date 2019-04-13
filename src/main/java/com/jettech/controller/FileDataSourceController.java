package com.jettech.controller;


import java.util.Map;

import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.jettech.service.IFileDataSourceService;
import com.jettech.vo.FileDataSourceVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@RestController
@RequestMapping(value = "/fileDataSource")
public class FileDataSourceController {
	private static Logger log = LoggerFactory.getLogger(FileDataSourceController.class);
	@Autowired
	private IFileDataSourceService fileDataSourceService;
	/**
	 * 添加操作
	 * @param
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/add")
	public ResultVO add(@RequestBody FileDataSourceVO fileDataSourceVO) {
       try{
    	   return fileDataSourceService.add(fileDataSourceVO);
		}catch (Exception e) {
			log.error("增加或修改filedataSource:失败", e);
		      return new ResultVO(false, StatusCode.ERROR, "添加或修改失败");
		    }
		
	}
	
	/**
	 * 删除操作
	 * 
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/delete")
	public ResultVO delete(@PathParam(value = "id") Integer id) {
		return fileDataSourceService.delete(id);
	}
	/**
	 * 分页查询文件型数据源
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllByPage(@RequestParam(value = "name") String name,@RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		return fileDataSourceService.getAllByPage(name,pageable);
	}
/**
 * 文件的预览
 */
	@ResponseBody
    @RequestMapping(value="/preview",produces = { "application/json;charset=UTF-8" })
    public  String preview(@RequestBody  Map map){
		return fileDataSourceService.preview(String.valueOf(map.get("filePath")),String.valueOf(map.get("characterSet")),Integer.parseInt(map.get("row")+""));
	}

	/**
	 * 测试连接方法 ftp sftp ssh 20190312
	 * @param fileDataSourceVO
	 * @return
	 */
	@ResponseBody
	/*@RequestMapping(method = RequestMethod.GET, value = "/testConnect")*/
	@RequestMapping(method = RequestMethod.POST, value = "/testConnect")
	public ResultVO testConnect(@RequestBody FileDataSourceVO fileDataSourceVO) {
		try {
			if( fileDataSourceVO == null ){
				return new ResultVO(false, StatusCode.ERROR, "参数不可为空");
			}
			//@RequestBody FileDataSourceVO fileDataSourceVO
			return fileDataSourceService.testConnect(fileDataSourceVO);
		} catch (Exception e) {
			log.error("连接测试:"+fileDataSourceVO.toString()+" 测试异常",e);
			return new ResultVO(true, StatusCode.OK, "连接失败");
		}

	}

	/**
	 * 文件的复制
	 * @param testDatabaseVO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/copyFileDataSource")
	public ResultVO copyFileDataSource(@PathParam(value = "id") Integer id,@PathParam(value = "name") String name) {
		return fileDataSourceService.copyFileDataSource(id, name);
	} 

}
