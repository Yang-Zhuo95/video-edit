package com.ulearning.video.common.swagger;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger配置类
 */
@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
//@Profile({"dev", "test"})
public class SwaggerConfig {

    // 定义分隔符,配置Swagger多包
    private static final String splitor = ";";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(this.apiInfo())
                .select()
                .apis(basePackage("com.ulearning.video" + splitor
                ))
                .paths(PathSelectors.any())
                .build();
                // .globalOperationParameters(getParameterList());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()

                .title("视频编辑服务api")

                .description("一个可以测试的接口文档")

                .version("0.0.1")

                .termsOfServiceUrl("www.baidu.com")

                .license("LICENSE")

                .licenseUrl("www.baidu.com")

                .contact(new Contact("视频编辑", "", ""))

                .build();

    }

    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(true);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            // 循环判断匹配
            for (String strPackage : basePackage.split(splitor)) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }

    /**
     * 方法描述：添加header参数配置
     * @return
     * @author Huangjin
     * @version 2021年8月5日下午5:18:00
     */
    // private List<Parameter> getParameterList() {
    //     ParameterBuilder token = new ParameterBuilder();
    //     List<Parameter> parameters = new ArrayList<Parameter>();
    //     token.name("AUTHORIZATION").description("登录凭证").modelRef(new ModelRef("String")).parameterType("header").required(false).build();
    //     parameters.add(token.build());
    //     return parameters;
    // }
}
