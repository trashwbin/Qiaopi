package com.qiaopi.config;

//import com.qiaopi.interceptor.JwtTokenAdminInterceptor;
import com.qiaopi.interceptor.JwtTokenUserInterceptor;
import com.qiaopi.json.JacksonObjectMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

//    @Autowired
//    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;
    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;



    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {

        log.info("开始注册自定义拦截器...");
//        registry.addInterceptor(jwtTokenAdminInterceptor)
//                .addPathPatterns("/admin/**")
//                .excludePathPatterns("/admin/employee/login");

        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .addPathPatterns("/bottle/**")
                .addPathPatterns("/letter/**")
                .addPathPatterns("/card/**")
                .addPathPatterns("/font/**")
                .addPathPatterns("/paper/**")
                .addPathPatterns("/question/**")
                .addPathPatterns("/marketing/**")
                .addPathPatterns("/game/**")
//                .excludePathPatterns("/font/list")
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/getCode")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/sendCode")
                .excludePathPatterns("/user/resetPasswordByEmail")
                .excludePathPatterns("/user/sendResetPasswordCode")
                .excludePathPatterns("/user/getCountries")
                ;

    }



@Bean
public OpenAPI springShopOpenAPI() {
    return new OpenAPI()

            .info(new Info()
                    .title("侨批接口文档") // 接口文档标题
                    .description("这是基于Knife4j OpenApi3的接口文档") // 接口文档简介
                    .version("1.0")  // 接口文档版本
                    .contact(new Contact()
                            .name("侨批") //开发者
                            .email("noreply@qiaopi.com")
                    )
            )
            //还得是Chatgpt更胜一筹,将不能解析的HashMap
            .components(new Components().addSchemas("AjaxResult", new Schema<>().type("object")
                    .addProperty("code", new IntegerSchema().description("状态码").example(200))
                    .addProperty("msg", new StringSchema().description("返回消息").example("操作成功"))
                    .addProperty("data", new ObjectSchema().description("数据对象"))))
            ; // 开发者联系方式
}
    /**
     * 添加静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
/*    *//**
     * 扩展Spring MVC框架的消息转化器
     * 此方案导致knife4j不能正常显示
     * @param converters
     *//*
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        //创建一个消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器，对象转换器可以将Java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转化器加入容器中
        converters.add(0,converter);
    }    */
    /**
     * 扩展消息转换器,将日期类型从列表转换为时间戳
     * 这个是导致knife4j不能正常显示的罪魁祸首,特别要注意添加的位置
     * @param converters 消息转换器列表
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器...");
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
        converters.add(converters.size()-1,jackson2HttpMessageConverter);
    }

}

