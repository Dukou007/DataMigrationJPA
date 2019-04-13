package com.jettech;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.jettech.core.Jettech;
import com.jettech.util.SpringUtils;

@SpringBootApplication
@EnableJpaAuditing
public class DataMigrationJpaApplication {

	public static void main(String[] args) {
		boolean result = Jettech.execute();//验证证书的有效性
		if(result){
			ApplicationContext applicationContext = SpringApplication.run(DataMigrationJpaApplication.class, args);
			new SpringUtils().setApplicationContext(applicationContext);
		}
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		// 单个文件最大 KB,MB
		factory.setMaxFileSize("10MB");
		/// 设置总上传数据总大小
		factory.setMaxRequestSize("1024MB");
		return factory.createMultipartConfig();
	}

}
