package com.jettech.controller;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jettech.EnumDatabaseType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.DB2Adapter;
import com.jettech.db.adapter.InformixAdapter;
import com.jettech.db.adapter.MySqlAdapter;
import com.jettech.db.adapter.OracleAdapter;
import com.jettech.domain.DbModel;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.MainSqlRecord;
import com.jettech.entity.MetaHistoryItem;
import com.jettech.entity.DataField;
import com.jettech.service.IMetaDataManageService;
import com.jettech.service.IMetaHistoryItemService;
import com.jettech.thread.CompareDataWorker;
import com.jettech.vo.DataSourceVO;
import com.jettech.vo.DataSourceVOO;
import com.jettech.vo.MainSqlRecordVO;
import com.jettech.vo.MetaHistoryItemVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.TestDatabaseVO;
import com.jettech.vo.TestFieldVO;
import com.jettech.vo.TestTableVO;
import com.jettech.vo.sql.SqlVo;
//@Controller
@RestController
@RequestMapping(value = "/metaData")
public class MetaDataController {
	private static Logger log = LoggerFactory.getLogger(MetaDataController.class);
	@Value("${file.filePath}")
	private String filePath;
	@Autowired
	private IMetaDataManageService metaDataManageService;
	@Autowired
	private IMetaHistoryItemService metaHistoryItemService;


	/**
	 *      变更记录的分页接口
	 * @param testDatabaseId
	 * @param tableName
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllChangeByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllChangeByPage(@RequestParam(value = "testDatabaseId") int testDatabaseId,@RequestParam(value = "tableName" ,defaultValue="",required=false) String tableName,@RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		Map<String,Object> resultmap = new HashMap<String,Object>();
		List<MetaHistoryItemVO> arrvolist=new ArrayList<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {
        	Page<MetaHistoryItem>  milist= metaHistoryItemService.getAllChangeByPage(testDatabaseId,tableName, pageable);
        			//metaDataManageService.getAllChangeByPage(testDatabaseId,pageable);
	        for(MetaHistoryItem tt : milist) {
	        	MetaHistoryItemVO miVO = new MetaHistoryItemVO(tt);
	        	arrvolist.add(miVO);
	        }
	        resultmap.put("totalElements",milist.getTotalElements());
	        resultmap.put("totalPages",milist.getTotalPages());
	        resultmap.put("list",arrvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	
	
	
	
	
	/**
	 * 元数据管理通过指定一个数据库来同步数据库下指定表的所有字段
	 * @param String datasources
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/selOneDBSynMetaData",produces = { "application/json;charset=UTF-8" })
    public  String selOneDBSynMetaData(@RequestBody  TestDatabaseVO testDatabaseVO){
		
		
		DataSchema testDatabase = new DataSchema();
		BeanUtils.copyProperties(testDatabaseVO, testDatabase);
		
		
		JSONObject result = new JSONObject();
		Connection conn = null;
		AbstractAdapter adapter = null;
        try {
        	DataSource ds =testDatabase.getDataSource();
    		String schema = ds.getDefaultSchema();
    		String username = ds.getUserName();
    		String pwd = ds.getPassword();
    		String name = ds.getName();
    		EnumDatabaseType dbType = ds.getDatabaseType();
    		String host = ds.getHost();

    		String port = ds.getPort();
    		String url = ds.getUrl();
    		String driver = ds.getDriver();
    		String sid = ds.getSid();
			DbModel db = new DbModel();
			if (dbType.equals(EnumDatabaseType.Mysql)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.mysql.cj.jdbc.Driver");
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {
					db.setUrl("jdbc:mysql://" + host
					        + "/information_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
				}

				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("mysql");
				db.setDbtype(EnumDatabaseType.Mysql);

				adapter = new MySqlAdapter();
				conn = ((MySqlAdapter) adapter).getConnection(db);
			} else if (dbType.equals(EnumDatabaseType.Oracle)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("oracle.jdbc.driver.OracleDriver");
				}
				if (port == null || port.equals("")) {
					port = "1521";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:oracle:thin:@//" + host + ":" + port + "/"+sid);
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("oracle");
				db.setDbtype(EnumDatabaseType.Oracle);

				adapter = new OracleAdapter();
				conn = ((OracleAdapter) adapter).getConnection(db);
			}else if (dbType.equals(EnumDatabaseType.DB2)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.ibm.db2.jcc.DB2Driver");
				}
				if (port == null || port.equals("")) {
					port = "50000";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:db2://" + host + ":" + port + "/datatest");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("db2");
				db.setDbtype(EnumDatabaseType.DB2);

				adapter = new DB2Adapter();
				conn = ((DB2Adapter) adapter).getConnection(db);
			}else if (dbType.equals(EnumDatabaseType.Informix)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.informix.jdbc.IfxDriver");
				}
				if (port == null || port.equals("")) {
					port = "9088";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:informix-sqli://" + host + ":" + port + "/xydb:INFORMIXSERVER=ol_informix1170");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("informix");
				db.setDbtype(EnumDatabaseType.Informix);

				adapter = new InformixAdapter();
				conn = ((InformixAdapter) adapter).getConnection(db);
			}
    		
		    metaDataManageService.syncSchemaMetaExistTable(adapter, conn, ds,testDatabase);
	        result.put("result", "success");
	    //    ObjectMapper mapper = new ObjectMapper();
       //     return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	  //   result.put("result", "error");
	  //   return JSON.toJSONString(result);
	    }
        
      //  return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	/**
	 * 元数据管理
	 * @param String datasources
	 * @return
	 */
	/*@ResponseBody
    @RequestMapping(value="/setMetaDataByIds",produces = { "application/text;charset=UTF-8" })
    public  String setMetaDataByIds(@RequestBody String dataSourceIds){
		JSONObject result = new JSONObject();
        try {
        	System.out.println(dataSourceIds);
        	dataSourceIds=dataSourceIds.replace("\"", "");
        	String[] arr= dataSourceIds.split(",");
        	for(String str :arr){
        		metaDataManageService.GetMetaData(Integer.parseInt(str));
        	}
	        result.put("result", "success");
	    //    ObjectMapper mapper = new ObjectMapper();
        //    return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	  //   result.put("result", "error");
	  //   return JSON.toJSONString(result);
	    }
        
     //   return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}*/
	/**
	 * 拼接sql语句插入数据库
	 * @param SqlVo sqlvo
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getSql",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  String getSql(@RequestBody SqlVo sqlvo){
		JSONObject result = new JSONObject();
        try {
	        metaDataManageService.GetSql(sqlvo);
	        result.put("result", "success");
	    //    ObjectMapper mapper = new ObjectMapper();
        //    return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	  //   result.put("result", "error");
	 //    return JSON.toJSONString(result);
	    }
      //  return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	
	/**
	 * 从数据库里查询sql解析成json传到前端
	 * @param SqlVo sqlvo
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/parseSql",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  String parseSql(@RequestBody  MainSqlRecordVO  mVo){
		JSONObject result = new JSONObject();
        try {
        	
        	MainSqlRecord mr = new MainSqlRecord();
			BeanUtils.copyProperties(mVo, mr);
        	SqlVo arr= metaDataManageService.parseSql(mr);
	        result.put("result", arr);
	     //   ObjectMapper mapper = new ObjectMapper();
         //   return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	  //   result.put("result", "error");
	  //   return JSON.toJSONString(result);
	    }
        
     //   return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}

	/**
	 * 获得所有的数据源
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllDataSource",produces = { "application/json;charset=UTF-8" },method = RequestMethod.GET)
    public  String getAllDataSource(){
		JSONObject result = new JSONObject();
        try {
	        List<DataSource> arr= metaDataManageService.getAllDatasource();	    
	        List<DataSourceVOO> dsvolist=new ArrayList<DataSourceVOO>();
	        for(int i=0;i<arr.size();i++) {
	        	DataSourceVOO dataSourceVoo=new DataSourceVOO(arr.get(i).getId(),arr.get(i).getName()); 
	            dsvolist.add(dataSourceVoo);
	        }
	        result.put("result", dsvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	     result.put("result", "获取失败");
	    }
        
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	
	
	
	
	
	/**
	 * 获得某数据源的所有的数据库信息
	 * @param TestDatabaseVO td
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllDb",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  String getAllDb(@RequestBody  Map map){
		JSONObject result = new JSONObject();
		List<TestDatabaseVO> arrvolist=new ArrayList<>();
		List<DataSchema> arr=new ArrayList<>();
        try {
        //	int id=td.getDataSource().getId();        	
	        arr= metaDataManageService.getAllDb(Integer.parseInt(map.get("dataSourceId")+""));
	        for(int i=0;i<arr.size();i++) {
	        	TestDatabaseVO testDatabaseVO = new TestDatabaseVO(arr.get(i));
	        	arrvolist.add(testDatabaseVO);
	        }
	        result.put("result", arrvolist);
	     //   ObjectMapper mapper = new ObjectMapper();
         //   return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	   //  result.put("result", "error");
	   //  return JSON.toJSONString(result);
	    }
//        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(	            
//        		DataSource.class, "id","name");		
//        String json = JSON.toJSONString(arr,filter,SerializerFeature.DisableCircularReferenceDetect);
	//	return json;
     //   return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	
	
	/**
	 * 获得某数据库下所有的表信息
	 * @param TestTableVO tt
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllTable",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  String getAllTable(@RequestBody   Map map){
		JSONObject result = new JSONObject();
		List<TestTableVO> arrvolist=new ArrayList<>();
		List<DataTable> arr=new ArrayList<>();
        try {
	        arr= metaDataManageService.getAllTable(Integer.parseInt(map.get("testDatabaseId")+""));
	        for(int i=0;i<arr.size();i++) {
	        	TestTableVO testTableVO = new TestTableVO(arr.get(i));
	        	arrvolist.add(testTableVO);
	        }
	        result.put("result", arrvolist);
	    //    ObjectMapper mapper = new ObjectMapper();
       //     return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	 //    result.put("result", "error");
	 //    return JSON.toJSONString(result);
	    }
//        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(	            
//        		TestDatabase.class, "id","name");		
//        String json = JSON.toJSONString(arr,filter,SerializerFeature.DisableCircularReferenceDetect);
//		return json;
 //       return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	/**
	 * 获得某个表下的所有列信息
	 * @param TestFieldVO tf
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllField",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  String getAllField(@RequestBody  Map map){
		JSONObject result = new JSONObject();
		List<TestFieldVO> arrvolist=new ArrayList<>();
		List<DataField> arr=new ArrayList<>();
        try {
	        arr= metaDataManageService.getAllField(Integer.parseInt(map.get("testTableId")+""));
	        for(int i=0;i<arr.size();i++) {
	        	TestFieldVO testFieldVO = new TestFieldVO(arr.get(i));
	        	arrvolist.add(testFieldVO);
	        }
	        result.put("result", arrvolist);
//	        ObjectMapper objectMapper = new ObjectMapper();		
	        //序列化的时候序列对象的所有属性		
	      //  objectMapper.setSerializationInclusion(Include.ALWAYS);		
	        //取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式		
	     //   objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);		
	//        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));		


//
//            DeserializationConfig cfg = mapper.getDeserializationConfig();
//            SerializationConfig s=mapper.getSerializationConfig();
//            s.setSerializationInclusion(Inclusion.NON_NULL);
//            cfg.withDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
//            mapper.setSerializationConfig(s);
         //   mapper = mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  //      return objectMapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	//     result.put("result", "error");
//	     return JSON.toJSONString(result);
	    }
//        SimplePropertyPreFilter filter = new SimplePropertyPreFilter(	            
//        		TestTable.class, "id","name");		
//        String json = JSON.toJSONString(arr,filter,SerializerFeature.DisableCircularReferenceDetect);
//		return json;
        
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);

       
	}
	
	/**
	 * 获取某个表的所有字段信息的方法
	 * @param testTableId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllFieldByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllFieldByPage(@RequestParam(value = "testTableId") int testTableId, @RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		Map<String,Object> resultmap = new HashMap<String,Object>();
		List<TestFieldVO> arrvolist=new ArrayList<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		try {
		Page<DataField> tflist= metaDataManageService.getAllFieldByPage(testTableId,pageable);
        for(DataField tf : tflist) {
        	TestFieldVO testFieldVO = new TestFieldVO(tf);
        	arrvolist.add(testFieldVO);
        }
       
        resultmap.put("totalElements",tflist.getTotalElements());
        resultmap.put("totalPages",tflist.getTotalPages());
        resultmap.put("list",arrvolist);
	    }catch(Exception e) {
		     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	/**
	 * 获得某数据库下所有表的分页接口 
	 * @param testDatabaseId
	 * @param tableName
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	
	@ResponseBody
    @RequestMapping(value="/getAllTableByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllTableByPage(@RequestParam(value = "testDatabaseId") int testDatabaseId,@RequestParam(value = "tableName" ,defaultValue="",required=false) String tableName, @RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		Map<String,Object> resultmap = new HashMap<String,Object>();
		List<TestTableVO> arrvolist=new ArrayList<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {
        	Page<DataTable>   ttlist= metaDataManageService.getSelTableByPage(testDatabaseId,tableName,pageable);
	        for(DataTable tt : ttlist) {
	        	TestTableVO testTableVO = new TestTableVO(tt);
	        	arrvolist.add(testTableVO);
	        }
	        resultmap.put("totalElements",ttlist.getTotalElements());
	        resultmap.put("totalPages",ttlist.getTotalPages());
	        resultmap.put("list",arrvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	

	/**
	 * 获得某数据源的所有的数据库信息
	 * @param dataSourceId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllDbByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllDbByPage(@RequestParam(value = "dataSourceId") int dataSourceId, @RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		Map<String,Object> resultmap = new HashMap<String,Object>();
		List<TestDatabaseVO> arrvolist=new ArrayList<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {     	
        	Page<DataSchema>    tdlist= metaDataManageService.getAllDbByPage(dataSourceId,pageable);
	        for(DataSchema td : tdlist) {
	        	TestDatabaseVO testDatabaseVO = new TestDatabaseVO(td);
	        	arrvolist.add(testDatabaseVO);
	        }
	        resultmap.put("totalElements",tdlist.getTotalElements());
	        resultmap.put("totalPages",tdlist.getTotalPages());
	        resultmap.put("list",arrvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	
	/**
	 * 获得所有的数据源
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllDataSourceByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllDataSourceByPage(@RequestParam(value = "dataSourceName" ,defaultValue="",required=false) String dataSourceName, @RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		JSONObject result = new JSONObject();
		Map<String,Object> resultmap = new HashMap<String,Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {
	        Page<DataSource> dslist= metaDataManageService.getAllDSByPage(dataSourceName, pageable);//findAllPage(pageNum,pageSize);	    
	        List<DataSourceVO> dsvolist=new ArrayList<DataSourceVO>();
	        for(DataSource   ds  : dslist) {
	        	DataSourceVO dataSourceVO = new DataSourceVO(ds);
	            dsvolist.add(dataSourceVO);
	        }
	        resultmap.put("totalElements",dslist.getTotalElements());
	        resultmap.put("totalPages",dslist.getTotalPages());
	        resultmap.put("list",dsvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	
	/**
	 * 获得某数据源的所有的数据库信息
	 * @param dataSourceId
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/getAllDatabaseByPage",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
    public  ResultVO getAllDatabaseByPage(@RequestParam(value = "DBName" ,defaultValue="",required=false) String DBName,@RequestParam(value = "pageNum",defaultValue="1",required=false) int pageNum,
            @RequestParam(value = "pageSize",defaultValue="10",required=false) int pageSize){
		Map<String,Object> resultmap = new HashMap<String,Object>();
		List<TestDatabaseVO> arrvolist=new ArrayList<>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {     	
        	Page<DataSchema>    tdlist= metaDataManageService.getAllDatabaseByPage(DBName,pageable);
	        for(DataSchema td : tdlist) {
	        	TestDatabaseVO testDatabaseVO = new TestDatabaseVO(td);
	        	arrvolist.add(testDatabaseVO);
	        }
	        resultmap.put("totalElements",tdlist.getTotalElements());
	        resultmap.put("totalPages",tdlist.getTotalPages());
	        resultmap.put("list",arrvolist);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}
	
	
	/**
	 *     导入一个数据字典
	 * @param String dbName
	 * @param String tableName
	 * @param String filePath
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/setOneDict",produces = { "application/json;charset=UTF-8" })
    public  String setOneDict(@RequestBody  Map map){
		JSONObject result = new JSONObject();
        try {
		    metaDataManageService.uploadDictExcel(String.valueOf(map.get("filePath")));
	        result.put("result", "success");
	   //     ObjectMapper mapper = new ObjectMapper();
      //      return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	    	System.out.println(e.getMessage());
	       e.getLocalizedMessage();
	     result.put("result", "error");
	  //   return JSON.toJSONString(result);
	    }
        
       // return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	
	/**
	 * 获取服务器文件的路径
	 * @param file
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/getFilePath", method = RequestMethod.POST, headers = "content-type=multipart/form-data")
	public JSONObject getFilePath(@RequestParam("file") MultipartFile file,
	        HttpServletRequest request) {

		JSONObject result = new JSONObject();

		// 文件名
		String fileName = file.getOriginalFilename();
		System.out.println("文件名： " + fileName);

		// 文件后缀
		String suffixName = fileName.substring(fileName.lastIndexOf("."));
		System.out.println("文件后缀名： " + suffixName);

		// 重新生成唯一文件名，用于存储数据库
		String newFileName = UUID.randomUUID().toString() + suffixName;
		System.out.println("新的文件名： " + newFileName);

		// 创建文件
		File dest = new File(filePath + fileName);

		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				log.info("临时文件:" + dest.getAbsolutePath());
				result.put("success", true);
				result.put("data", map);
			} else {
				String msg = "临时文件不存在:" + dest.getAbsolutePath();
				log.info(msg);
				result.put("success", false);
				result.put("data", map);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return (JSONObject) result.put("success", false);
		}
	}
	
	/**
	 *     比较数据字典和数据库表的不同，存入库中
	 * @param String datasources
	 * @return
	 */
	@ResponseBody
    @RequestMapping(value="/compareDictAndModel",produces = { "application/json;charset=UTF-8" })
    public  String compareDictAndModel(@RequestBody  Map map){
		JSONObject result = new JSONObject();
        try {
		    metaDataManageService.compareDictAndModel(Integer.parseInt(map.get("db1Id")+""),Integer.parseInt(map.get("db2Id")+""));
	        result.put("result", "success");
	   //     ObjectMapper mapper = new ObjectMapper();
      //      return mapper.writeValueAsString(result);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	   //  result.put("result", "error");
	  //   return JSON.toJSONString(result);
	    }
        
       // return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
	}
	
	/**
	 * 比较两个数据字典或两个数据模型或一个数据字典一个数据模型的表或字段及属性的不同，存入库中
	 * @param map
	 * @return
	 */

		@ResponseBody
	    @RequestMapping(value="/compareTwoModel",produces = { "application/json;charset=UTF-8" })
	    public  String compareTwoModel(@RequestBody  Map map){
			JSONObject result = new JSONObject();
	        try {
			    CompareDataWorker worker = new CompareDataWorker(Integer.parseInt(map.get("leftdbId")+""),Integer.parseInt(map.get("rightdbId")+""));
				Thread compareThread = new Thread(worker);
				compareThread.setName("Do:compareTwoModel");
				compareThread.start();
			    
			    result.put("result", "success");
		   //     ObjectMapper mapper = new ObjectMapper();
	      //      return mapper.writeValueAsString(result);
		    }catch(Exception e) {
		     e.getLocalizedMessage();
		   //  result.put("result", "error");
		  //   return JSON.toJSONString(result);
		    }
	        
	       // return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue);  
	        return JSONObject.toJSONString(result,SerializerFeature.WriteMapNullValue,SerializerFeature.WriteDateUseDateFormat);
		}
		/**
		 *测试填写的sql能否再某个数据源的运行
		 * @return
		 */
		@ResponseBody
		@RequestMapping(value="/TestSqlDBLinkDataSource",produces = { "application/json;charset=UTF-8" },method = RequestMethod.POST)
		public  ResultVO TestSqlDBLinkDataSource(@RequestParam Integer dataSourceId,@RequestParam String sqlText){
			try {
				ResultVO re=metaDataManageService.GetSqlDBLink(dataSourceId,sqlText);
				return re;
			}catch(Exception e) {
				e.getLocalizedMessage();
				return new ResultVO(false, StatusCode.ERROR, "测试失败"+ e.getLocalizedMessage());
			}
		}
		
}
