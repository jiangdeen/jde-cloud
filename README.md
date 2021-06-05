

# winfo-cloud 组件使用说明

[toc]

## 项目结构
``` lua
winfo-cloud
├── winfo-auth2-server -- demo
├── winfo-common -- 公共依赖
    ├── winfo-common-core 核心依赖包括参数校验,包装返回和异常处理
    ├── winfo-common-datasource 多数据源支持
    ├── winfo-common-executor 定时任务执行器
    ├── winfo-common-i18n 国际化
    ├── winfo-common-log 日志
        ├──winfo-common-log-aop 切面日志
    ├── winfo-common-redis reids工具模块  
    ├── winf-common-swagger swagger依赖模块
├── winfo-drools  drools引擎规则 
├── winfo-model 业务模块
├── winfo-xxl-job-admin 定时任务调度中心
```

### 环境配置

#### jdk/maven 配置

> maven 3.6.0 及以上, jdk 1.8

```xml
<!-- maven 镜像配置 -->
<mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>
</mirror> 
```

#### spirngcloud版本依赖

```xml
<dependencies>
    <!-- SpringCloud 微服务 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>Hoxton.SR8</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>

    <!-- SpringCloud Alibaba 微服务 -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-alibaba-dependencies</artifactId>
        <version>2.2.5.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>

    <!-- SpringBoot 依赖配置 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>2.3.7.RELEASE</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencies>
```

### 多数据源

#### 概述

> 在项目中经常会用到一个项目有多个数据源的情况,比如三峡项目的第三方系统只读库,和我们自己的业务系统库
> 需要两个数据库的资源然后在代码层面做业务,所以多数据源的支持是非常必要的,在本项目中使用mybaits-plus
> 来切换多数据源.

#### 多数据源的使用
> 首先在要使用多数据源的模块中引入winfo-common-datasource模块

```xml
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-datasource</artifactId>
</dependency>
```
> 重要:使用Druid数据库连接池,需要去掉 DruidDataSourceAutoConfigure自动装配的类, 否则的话mybatis自动装配类不会生效  
> 有两种方法可以阻止IOC自动注入Bean,第一种是使用配置 spring.autoconfigure.exclude ='类的全路径'   
> 第二种就是使用注解@SpringBootApplication(exclude = {xxx.class, xxx.class})  
> 在切换的service曾增加注解 @DS("master"), master就是你在下面配置的数据源的名称.
```yaml
spring:
  autoconfigure:
    #自动化配置 例外处理
    exclude: com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure
  # dynamic-datasource-spring-boot-starter 动态数据源的配置内容
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    dynamic:
      primary: master # 设置默认的数据源或者数据源组，默认值即为 master
      datasource:
        # test 数据源配置
        master:
          name: druidDataSource
          type: com.alibaba.druid.pool.DruidDataSource
          driverClassName: oracle.jdbc.driver.OracleDriver
          #你自己的数据库IP地址和数据库名，账号以及密码
          url: jdbc:oracle:thin:@//192.168.0.160:1521/szmsa
          username: msacp
          password: msacp
        # test1 数据源配置
        test1:
          url: jdbc:mysql://192.168.1.120:3306/db_test1?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
          driver-class-name: com.mysql.cj.jdbc.Driver
          username: root
          password: root1234
      #dynamic - 公共配置
      druid:
        initialSize: 5
        minIdle: 5
        maxActive: 30
        maxWait: 60000
        timeBetweenEvictionRunsMillis: 60000
        minEvictableIdleTimeMillis: 300000
        validationQuery: SELECT * FROM DUAL
        testWhileIdle: true
        testOnBorrow: false
        testOnReturn: false
        poolPreparedStatements: true
        maxPoolPreparedStatementPerConnectionSize: 20
        filters: stat,wall,slf4j,config
        useGlobalDataSourceStat: true
        stat:
          log-slow-sql: true
          merge-sql: true
          slow-sql-millis: 10000
```

### 国际化

#### 概述
> 在开发应用程序的时候，经常会遇到支持多语言的需求，这种支持多语言的功能称之为国际化，
> 英文是internationalization，缩写为i18n（因为首字母i和末字母n中间有18个字母）。

#### 国际化使用

```xml
<!-- 引入依赖 -->
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-i18n</artifactId>
</dependency>
```
```java
@Configuration
public class LocaleConfig {
    /**
     * 默认解析器 其中locale表示默认语言
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return localeResolver;
    }

    /**
     * 默认拦截器 其中lang表示切换语言的参数名
     */
    @Bean
    public WebMvcConfigurer localeInterceptor() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                LocaleChangeInterceptor localeInterceptor = new LocaleChangeInterceptor();
                localeInterceptor.setParamName("lang");
                registry.addInterceptor(localeInterceptor);
            }
        };
    }
```
>在需要使用的模块下面/resource新建一个message,messages.properties 这个是默认的配置，
> 其他的则是不同语言环境下的配置，en_US 是英语(美国)，zh_CN 是中文简体，
> zh_TW 是中文繁体。因为上文配置了在请求参数后面加了一个lang这个参数,这个参数会控制去哪个配置文件
> 里面获取对应的值.




### 数据校验

#### 概述
> 在日常开发中我们通需要对前端传过来的参数进行合法性校验,这里我们使用的是 @Validated和@Valid
> 来进行数据校验 
> 两者区别:@Validated：提供了一个分组功能，可以在入参验证时，根据不同的分组采用不同的验证机制
> @valid 没有分组功能 @Validated：可以用在类型、方法和方法参数上。但是不能用在成员属性（字段）上
@Valid：可以用在方法、构造函数、方法参数和成员属性（字段）上.



#### 使用方式
```xml
 <dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-core</artifactId>
 </dependency>
```
> 在需要校验的类上加上@Valid,然后在对应的字段上加上匹配规则,这样就可以达到自动校验了,校验不通过
> 抛出的异常会被异常类捕获,异常类会把校验中的异常处理成统一的格式然后返沪给前端.
```java
/**
 * @param BindingResult 从校验异常类中获取的对象
 * 把校验异常类中的内容取出整理成统一的格式返回给前端
 */
 private R paramValid(BindingResult bindingResult) {
        return R.fail(bindingResult.getAllErrors().stream().map(it -> {
                    FieldError is = (FieldError) it;
                    Map<String, String> map = new HashMap<>();
                    map.put("field", is.getField());
                    map.put("defaultMessage", is.getDefaultMessage());
                    return map;
                }).collect(Collectors.toList()),
                "参数校验异常"
        );
    }
```
#### 附录:校验注解
>约束注解名称	约束注解说明  
@Null		 	验证对象是否为空  
@NotNull		验证对象是否为非空  
@AssertTrue	 	验证 Boolean 对象是否为 true  
@AssertFalse	 	验证 Boolean 对象是否为 false  
@Min		 	验证 Number 和 String 对象是否大等于指定的值  
@Max			验证 Number 和 String 对象是否小等于指定的值  
@DecimalMin	 	验证 Number 和 String 对象是否大等于指定的值，小数存在精度  
@DecimalMax	 	验证 Number 和 String 对象是否小等于指定的值，小数存在精度  
@Size	 	 	验证对象（Array,Collection,Map,String）长度是否在给定的范围之内  
@Digits		 	验证 Number 和 String 的构成是否合法  
@Past	 	 	验证 Date 和 Calendar 对象是否在当前时间之前  
@Future	 	 	验证 Date 和 Calendar 对象是否在当前时间之后  
@Pattern	 	 	验证 String 对象是否符合正则表达式的规则  

>（三）Hibernate Validator 附加的约束注解定义  
Constraint		详细信息  
@Email	 		被注释的元素必须是电子邮箱地址  
@Length	 		被注释的字符串的大小必须在指定的范围内  
@NotEmpty	 	被注释的字符串的必须非空  
@Range	 		被注释的元素必须在合适的范围内  

### 缓存
#### 概述
>缓存在现代项目中是必不可少的一部分,经常需要查询的热点数据,请求并发高的数据都可以放到
> redis缓存中, 本项目缓存采用最流行的redis 作为缓存,redis缓存开源易用,生态成熟.

#### 使用方式
```xml
<!-- 引入依赖 -->
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-redis</artifactId>
</dependency>
```
```yaml
redis:
    host: 192.168.0.166
    port: 6379
    password: msaredis
    lettuce:
      pool:
        max-wait: 100000  # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-idle: 8       # 连接池中的最大空闲连接
        min-idle: 0       # 连接池中的最小空闲连接
        max-active: 20    # 连接池最大连接数（使用负值表示没有限制）
    timeout: 5000         # 连接超时时间（毫秒）
```
```java

package org.springframework.boot.autoconfigure.data.redis;

import java.net.UnknownHostException;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisOperations.class) 
@EnableConfigurationProperties(RedisProperties.class)
@Import({ LettuceConnectionConfiguration.class, JedisConnectionConfiguration.class })
public class RedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory)
            throws UnknownHostException {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}

```
>上面的代码是spring-redis包原生的配置类,里面配置了获取配置文件,注入redisTemplate操作助手等
>winfo-common-redis这个模块注入RedisService类,需要使用缓存的地方直接使用spirng @autoware 
> 获取RedisService类对象就可以使用这个对象来操作redis,非常的简单.


### 异常处理
#### 概述
> 异常