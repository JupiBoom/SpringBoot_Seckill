package org.seckill.application;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// @SpringBootApplication=@Configuration+@EnableAutoConfiguration+@ComponentScan，
// 其中扫描包的范围为启动类所在包和子包，不包括第三方的jar包。
// 如果我们需要扫描通过maven依赖添加的jar，我们就要单独使用@ComponentScan注解扫描第三方包。
// 如果@SpringBootApplication和@ComponentScan注解共存，那么@SpringBootApplication注解的扫描的作用将会被覆盖，
// 也就是说不能够自动扫描启动类所在包以及子包了，而必须要在@ComponentScan注解配置本工程所有需要扫描的包范围。
@SpringBootApplication
@ComponentScan({"org.seckill.web","org.seckill.service"})
@MapperScan("org.seckill.dao")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
