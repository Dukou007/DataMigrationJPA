
package com.jettech;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 *  @Description: 接口文档
 *	@author zhou_xiaolong in 2019年2月24日 下午4:12:42 
 *  
 */
@Configuration
@EnableSwagger2
public class Swagger2 {
	
	@Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jettech"))
                .paths(PathSelectors.any())
                .build();
    }

	/**
	 * @Description: chengshang
	 * @Tips: null;
	 * @State: being used 
	 * @author:zhou_xiaolong in 2019年2月24日下午4:22:33
	 */
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Spring Boot中使用Swagger2构建RESTful APIs")
                .description("数据迁移项目")
                .termsOfServiceUrl("http://192.168.3.53:8011")
                .contact("sunf")
                .version("1.0")
                .build();
    }

}
