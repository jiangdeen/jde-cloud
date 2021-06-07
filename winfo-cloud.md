[toc]

## 事务管理

```
事务定义: 事务是逻辑上的一种操作,同一个事务内的几个步骤,要么全部成功,要么全部失败。

事务的特性(ACDI): 原子性(Atomicity)、一致性(Consistency)、隔离性(Isolation)、持久性(Durability)
- 原子性: 事务不可分割
- 一致性: 事务执行前后数据保持完整一致
- 隔离性: 一个事务在执行的时候,不应该受到其他事务的打扰
- 持久性: 事务一旦结束,数据就永久保存到数据库

事务的隔离级别: 什么是事务的隔离级别?我们知道,隔离性是事务的四大特性之一,表示多个并发事务之间的数据要互相隔离,隔离级别就是用来描述并发事务之间隔离程度的大小
* 脏读: 一个事务读到了,另一个事务未提交的数据
* 幻读: 一个事务读到了另一个事务已经提交的insert的数据,导致多次查询的结果不一致
* 不可重复读: 一个事务读到了另一个事务已经提交的update的数据,导致多次查询的结果不一致
```



### 概述

```
在Spring事务管理中,Spring为我们提供了 @Transaction注解来帮助我们管理事务
spring为我们定义了如下隔离级别:
* ISOLATION_DEFAULT: 使用数据库默认的隔离级别
* ISOLATION_READ_UNCOMMITTED: 最低的隔离级别,允许读取已经改变而没有提交的数据,可能会导致脏读,幻读或不可重复读
* ISOLATION_READ_COMMITTED: 允许读取事务已经提交的数据,可以阻止脏读,但是幻读和不可重复读仍有可能发生
* ISOLATION_REPEATABLE_READ: 对同一字段的多次读取结果都是一致的,除非数据事务本身改变,可以阻止脏读和不可重复读,但幻读仍有可能发生
* ISOLATION_SERIALIZABLE: 最高的隔离级别,完全服从ACID的隔离级别,确保不发生脏读,幻读,不可重复读,也是最慢的事务隔离级别,因为它通常是通过完全锁定事务相关的数据库表来实现的

在Oracle数据库中只支持READ COMMITTED和SERIALIZABLE 这两种隔离级别。所以Oracle不支持脏读，即Oralce不允许一个会话读取其他事务未提交的数据修改结果，从而方式了由于事务回滚发生的读取不正确。
```

#### spring事务管理的API

* PlatformTransactionManager (平台) 事务管理器 
* TransactionDefinition 事务定义信息(事务隔离级别、传播行为、超时、只读、回滚规则)
* TransactionStatus 事务运行状态

```java
public interface PlatformTransactionManager {
   
   //平台无关的获得事务的方法
   TransactionStatus getTransaction(@Nullable TransactionDefinition definition)
           throws TransactionException;

   //平台无关的事务提交方法
   void commit(TransactionStatus status) throws TransactionException;

   //平台无关的事务回滚方法
   void rollback(TransactionStatus status) throws TransactionException;

}
```

```wiki
    可以看出，PlatformTransactionManager是一个与任何事务策略分离的接口。PlatformTransactionManager接口有许多不同的实现类，应用程序面向与平台无关的接口编程，而对不同平台的底层支持由PlatformTransactionManager接口的实现类完成，故而应用程序无须与具体的事务API耦合。因此使用PlatformTransactionManager接口，可将代码从具体的事务API中解耦出来。
    
    在PlatformTransactionManager接口内，包含一个getTransaction（TransactionDefinition definition）方法，该方法根据一个TransactionDefinition参数，返回一个TransactionStatus对象。TransactionStatus对象表示一个事务，该事务可能是一个新的事务，也可能是一个已经存在的事务对象，这由TransactionDefinition所定义的事务规则所决定。
```

```java
public interface TransactionDefinition {

    // 对应上文事务的7种传播属性
   int PROPAGATION_REQUIRED = 0;
   int PROPAGATION_SUPPORTS = 1;
   int PROPAGATION_MANDATORY = 2;
   int PROPAGATION_REQUIRES_NEW = 3;
   int PROPAGATION_NOT_SUPPORTED = 4;
   int PROPAGATION_NEVER = 5;
   int PROPAGATION_NESTED = 6;

    // 对应上文的4种隔离级别 
   int ISOLATION_DEFAULT = -1; // 默认使用数据库默认的隔离级别
   int ISOLATION_READ_UNCOMMITTED = Connection.TRANSACTION_READ_UNCOMMITTED;
   int ISOLATION_READ_COMMITTED = Connection.TRANSACTION_READ_COMMITTED;
   int ISOLATION_REPEATABLE_READ = Connection.TRANSACTION_REPEATABLE_READ;
   int ISOLATION_SERIALIZABLE = Connection.TRANSACTION_SERIALIZABLE;

    // 事务的超时时间，默认-1 不设置超时
   int TIMEOUT_DEFAULT = -1;

    // 获取当前事务的传播行为
   int getPropagationBehavior();
    
    // 获取当前事务的隔离级别
   int getIsolationLevel();
   
   // 获取当前事务的超时时间
   int getTimeout();

    // 判断当前事务是否为只读事务
   boolean isReadOnly();
   
    // 获取事务名称
   @Nullable
   String getName();

}
```

```
TransactionDefinition 接口用于定义一个事务的规则,它包含了事务的一些静态属性,比如:事务传播行为、超时时间等。同时，Spring还为我们提供了一个默认的实现类:DefualtTransactionDefinition,该类适合于大多数情况。如果该类不能满足需求，可以通过实现TransactionDefinition接口来实现自定义事务。
TransactionDefinition 接口只提供了获取属性的方法，而没有提供相关设置属性的方法。因为，事务属性的设置完全是程序员控制的，
因此程序员可以自定义任何设置属性的方法，而且保存属性的字段也没有任何要求。唯一的要求的是，Spring 进行事务操作的时候，通过调用以上接口提供的方法必须能够返回事务相关的属性取值。
例如，TransactionDefinition 接口的默认的实现类 —— DefaultTransactionDefinition 就同时定义了一系列属性设置和获取方法。
```

```java
public interface TransactionStatus extends SavepointManager, Flushable {

   //是否是一个新的事物
   boolean isNewTransaction();

   //判断是否有回滚点
   boolean hasSavepoint();

   
   //将一个事务标识为不可提交的。在调用完setRollbackOnly()后只能被回滚
   //在大多数情况下，事务管理器会检测到这一点，在它发现事务要提交时会立刻结束事务。
   //调用完setRollbackOnly()后，数数据库可以继续执行select，但不允许执行update语句，因为事务只可以进行读取操作，任何修改都不会被提交。
   void setRollbackOnly();
   boolean isRollbackOnly();


    /** 
    1、调用flush方法等于在后面对OutputStream使劲的抽一鞭子，并命令“赶紧给我写入，我的水桶太满了”；2、写入数据量不大时，可以考虑不用。
    
    先来说说flush方法为了解决什么问题。我们都知道在Linux中，可写的句柄都是”文件“，并且，不管是Windows还是Linux都有提供相同名字的flush系统调用，而且操作系统在写文件时，先把要写的内容从用户缓冲区复制到内核缓冲区等待真正的写入到“文件”。java中的Flushable.flush()方法显然也是调用操作系统提供的接口。不管怎么调用，他们的原理都是一样的，比如要写4K大小的文件，操作系统有几种策略把字节写入到”文件“中：1、应用程序每写一个字节，操作系统马上把这个字节写入”文件“。2、应用程序写入字节后，操作系统不马上写入，而是先把它缓存起来，到达一定数量时才写入”文件“。3、应用程序写入字节后，没有到达可写的字节数量时，操作系统不写入，而是由应用程序控制。
    
    我们先来看第一种策略，这种策略对操作系统来讲，显然效率太低，不可取。第二种策略，为了弥补第一种策略的不足，达到一定数量时才写入，可以提高系统利用率，现代大部分操作系统也都这样实现的。那么问题来了，当写入一定数量的字节后，虽然还没有达到操作系统可写入的数量，但是应用程序有这个需求说，我得马上写入。那怎么办？为了应对这种策略，操作系统提供了flush系统调用，让应用程序可以控制何时马上写入文件。这也是第三种策略。
    
    说到这里，有的人可能有疑问，那应用程序写入字节数不足以达到操作系统要写入的数量，而且没有调用flush方法，那这些字节是不是就丢失了？答案是否定的，当打开一个文件句柄，不管写入多少字节的内容，在调用close方法时，系统会自动写入未写的内容，很多操作系统的close方法实现中就有调用flush方法的部分。
    
    此时我们再回过头来看看同事讲的两句话。第一句话的前半句是对的，至于“我的水桶太满了”，同事的意思是说，调用flush方法就是为了水桶太满，这显然违背事实的。水桶达到一定高度时操作系统会排光水而空出桶的空间以备继续接收水。至于第二句话，如果数据量不大，而急需把内容写到“文件”中，此时，必须调用flush方法，除非close掉文件句柄。
    
    最后要注意一点，当操作系统内核缓冲区中还有未写入的字节，而此时系统奔溃或者断电等情况，那么这部分内容也就丢失了。所以要不要调用flush方法，要看具体的需求，笔者认为大部分时候没有必要调用flush方法。频繁的调用flush方法会降低系统性能，举个极端的例子，每写入一个字节就调用一次，这显然就退化到了上面提到的第一种策略。
    */
   @Override
   void flush();

   //判断事物是否已经完成
   boolean isCompleted();

}

//接口继承了 SavepointManager 接口，因此封装了事物中回滚点的相关操作:
public interface SavepointManager {

	// 创建回滚点
	Object createSavepoint() throws TransactionException;

    // 回到回滚点
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

    // 释放回滚点
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
```



### 使用方式

```wiki
	在需要执行事务的地方加上 @Transactional 然后根据自己的需要配置好事务的隔离级别和传播性即可.
```

## 异常处理

### 概述

```wiki
	异常本质上是程序上的错误，包括程序逻辑错误和系统错误.比如使用空的引用、数组下标越界、内存溢出错误等，这些都是意外的情况，背离我们程序本身的意图.
```

#### 异常的分类

```wiki
Java 把异常当作对象来处理，并定义一个基类 java.lang.Throwable 作为所有异常的超类。
Java 包括三种类型的异常: 检查性异常(checked exceptions)、非检查性异常(unchecked Exceptions) 和错误(errors)。
本项目中的异常处理主要针对,非检查性异常.RuntimeException来进行异常处理!
```

#### 通用异常类

```wiki
通用异常类首先继承了RuntimeException异常类
```

```java
public class BaseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String defaultMessage;

    public BaseException(AppHttpStatus status) {
        this(null, String.valueOf(status.getStatus()), null, status.getMessage());
    }

    public BaseException(String module, String code, Object[] args, String defaultMessage) {
        this.module = module;
        this.code = code;
        this.args = args;
        this.defaultMessage = defaultMessage;
    }

    public BaseException(String module, String code, Object[] args) {
        this(module, code, args, null);
    }

    public BaseException(String module, String defaultMessage) {
        this(module, null, null, defaultMessage);
    }

    public BaseException(String code, Object[] args) {
        this(null, code, args, null);
    }

    public BaseException(String defaultMessage) {
        this(null, null, null, defaultMessage);
    }

    public String getModule() {
        return module;
    }

    public String getCode() {
        return code;
    }

    public Object[] getArgs() {
        return args;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
```

```
BaseException 提供了丰富的构造函数,在需要抛出异常的地方直接 throw new BaseException() 就可以抛出自定义异常了
```

#### Web接口异常处理

```wiki
	restful 接口如果出现异常,一般情况下会把接口的堆栈信息抛出来,返回到前端,这种情况十分不安全,有些黑客请求的时候故意输入一些错误的参数导致接口报错,通过返回接口的堆栈信息分析你程序的漏洞进而机型攻击.
	这个时候我们就想到通过try catch 来捕获所有异常然后统一返回提示,而不是程序内部的堆栈信息,其实不用这个麻烦,spring3.2以后为我们提供了一套注解,来解决上述问题.
@ControllerAdvice 注解，可以用于定义@ExceptionHandler 来处理我们所遇到的异常情况,其定义如下:
```

```java
/**
 * @description: 异常切面类 <br>
 * @date: 2019/8/29 10:08 <br>
 * @author: winfo-jiangde <br>
 * @version: 1.0 <br>
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    /**
     * 捕获处理全局异常
     *
     * @param ex
     * @return ResultVO 返回自定义类型
     */
    @ExceptionHandler(value = Exception.class)
    public R globalException(Exception ex) {
        log.error("globalException:" + ex.getMessage(), ex);
        return R.fail(ex.getMessage());
    }


    /**
     * 捕获 BaseException
     *
     * @param ex
     * @return ResultVO 包装返回类
     */
    @ExceptionHandler(value = BaseException.class)
    public R applicationRuntimeException(BaseException ex) {
        log.error("CoreRuntimeException:" + ex.getMessage(), ex);
        return R.fail(ex.getMessage());
    }
}
```

```
由此可以看出我们只需要在后台用logback打印出异常信息,而对前端返回的则是异常的提示,避免了程序内部结构泄露的风险
```

### 使用方式

```xml
<!-- 加入依赖 -->
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-core</artifactId>
</dependency>
```

```wiki
加入了这个依赖之后上述问题就自行解决了
```



## 系统日志

### logAspect

## 数据权限

### shiro

### spring-security

## 多数据源

```
在项目中经常会用到一个项目有多个数据源的情况,比如三峡项目的第三方系统只读库,和我们自己的业务系统库需要两个数据库的资源然后在代码层面做业务,所以多数据源的支持是非常必要的,在本项目中使用mybaits-plus来切换多数据源.
```

### mybatis-puls多数据源

#### 概述

```wiki
MyBatis-Plus（简称 MP）是一个MyBatis的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。
特性:
	无侵入：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑
    损耗小：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作
    强大的 CRUD 操作：内置通用 Mapper、通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求
    支持 Lambda 形式调用：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错
    支持主键自动生成：支持多达 4 种主键策略（内含分布式唯一 ID 生成器 - Sequence），可自由配置，完美解决主键问题
    支持 ActiveRecord 模式：支持 ActiveRecord 形式调用，实体类只需继承 Model 类即可进行强大的 CRUD 操作
    支持自定义全局通用操作：支持全局通用方法注入（ Write once, use anywhere ）
    内置代码生成器：采用代码或者 Maven 插件可快速生成 Mapper 、 Model 、 Service 、 Controller 层代码，支持模板引擎，更有超多自定义配置等您来使用
    内置分页插件：基于 MyBatis 物理分页，开发者无需关心具体操作，配置好插件之后，写分页等同于普通 List 查询
    分页插件支持多种数据库：支持 MySQL、MariaDB、Oracle、DB2、H2、HSQL、SQLite、Postgre、SQLServer 等多种数据库
    内置性能分析插件：可输出 Sql 语句以及其执行时间，建议开发测试时启用该功能，能快速揪出慢查询
    内置全局拦截插件：提供全表 delete 、 update 操作智能分析阻断，也可自定义拦截规则，预防误操作
我们要介绍的就是mybatis-plus 扩展特性多数据源配置.
```

#### 使用方式

```
引入依赖
```

```xml
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-datasource</artifactId>
</dependency>
```

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
          url: jdbc:oracle:thin:@//192.168.0.160:1521/szmsa
          driver-class-name: oracle.jdbc.driver.OracleDriver
          username: szmsa
          password: szmsa

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

```
 需要注意的是如果使用druid连接池需要在spring注入bean的时候排除DruidDataSourceAutoConfigure类
 使用@DS("test1") 注解,可以在Service曾可以选择使用哪个数据源.
```



## 代码生成

## 定时任务

```
在企业级应用中，会经常指定一些计划任务，即在某个时间点做某件事情，核心是以时间为关注点，即在一个特定的时间点，系统执行特定的一个操作，常见的任务调度框架有Quartz和SpringTask等。
```

### spring-task

#### 概述

```
springtask是spring3之后spring自带支持的定时任务
特点:
使用非常简单，配置好之后只需要在实现类上增加注解即可。避免复杂配置
spring自家产品，除spring相关的包外不需要额外引入额外jar包
支持注解和配置文件两种形式
支持线程池调度
```

#### 使用

```
在启动类上加上@EnableScheduling注解,再要执行的定时任务的方法上@Scheduled 注解,@Scheduled 可以使用cron表达式,也可以使用fixedDelay来指定固定的秒数来执行.
```

```java
@Component
@EnableScheduling
public class Mytask {

    @Scheduled(fixedDelay = 5000)
    public void run(){
        System.out.println("=======================1");
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void run1(){
        System.out.println("========================2");
    }

}
```

### Quartz

#### 概述

#### 使用

## 在线接口文档

```
 在前后端分离开发中，为了减少与其它团队的沟通成本，一般都会构建一份 **RESTful API** 文档来描述所有的接口信息。但传统的方式有许多弊端，不仅编写文档工作量巨大，而且维护不方便，测试也不方便（需要借助第三方工具，如 **Postman** 来测试）
  为解决这些问题，衍生除了很多在线文档解决方案,比较优秀的有swagger和yapi。
```

### swagger2

#### 概述

```
  **Swagger 2** 是一个开源软件框架，可以帮助开发人员设计、构建、记录和使用 **RESTful Web** 服务，它将代码和文档融为一体，可以完美解决文档编写繁琐、维护不方便等问题。使得开发人员可以将大部分精力集中到业务中，而不是繁杂琐碎的文档中。
  本项目中提供了一个swagger依赖,在依赖中已经定义好了swagger版本以及一些配置,方面后面进行业务开发的同学直接使用.
```

```java
@Configuration
@EnableSwagger2
@EnableAutoConfiguration
@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
public class SwaggerAutoConfiguration
{
    /**
     * 默认的排除路径，排除Spring Boot默认的错误处理路径和端点
     */
    private static final List<String> DEFAULT_EXCLUDE_PATH = Arrays.asList("/error", "/actuator/**");

    private static final String BASE_PATH = "/**";

    @Bean
    @ConditionalOnMissingBean
    public SwaggerProperties swaggerProperties()
    {
        return new SwaggerProperties();
    }

    @Bean
    public Docket api(SwaggerProperties swaggerProperties)
    {
        // base-path处理
        if (swaggerProperties.getBasePath().isEmpty())
        {
            swaggerProperties.getBasePath().add(BASE_PATH);
        }
        // noinspection unchecked
        List<Predicate<String>> basePath = new ArrayList<Predicate<String>>();
        swaggerProperties.getBasePath().forEach(path -> basePath.add(PathSelectors.ant(path)));

        // exclude-path处理
        if (swaggerProperties.getExcludePath().isEmpty())
        {
            swaggerProperties.getExcludePath().addAll(DEFAULT_EXCLUDE_PATH);
        }
        List<Predicate<String>> excludePath = new ArrayList<>();
        swaggerProperties.getExcludePath().forEach(path -> excludePath.add(PathSelectors.ant(path)));

        //noinspection Guava
        return new Docket(DocumentationType.SWAGGER_2)
                .host(swaggerProperties.getHost())
                .apiInfo(apiInfo(swaggerProperties)).select()
                .apis(RequestHandlerSelectors.basePackage(swaggerProperties.getBasePackage()))
                .paths(Predicates.and(Predicates.not(Predicates.or(excludePath)), Predicates.or(basePath)))
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts())
                .pathMapping("/");
    }

    /**
     * 安全模式，这里指定token通过Authorization头请求头传递
     */
    private List<ApiKey> securitySchemes()
    {
        List<ApiKey> apiKeyList = new ArrayList<ApiKey>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", "header"));
        return apiKeyList;
    }

    /**
     * 安全上下文
     */
    private List<SecurityContext> securityContexts()
    {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(PathSelectors.regex("^(?!auth).*$"))
                        .build());
        return securityContexts;
    }

    /**
     * 默认的全局鉴权策略
     *
     * @return
     */
    private List<SecurityReference> defaultAuth()
    {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }

    private ApiInfo apiInfo(SwaggerProperties swaggerProperties)
    {
         return new ApiInfoBuilder()
             .title(swaggerProperties.getTitle())
             .description(swaggerProperties.getDescription())
             .license(swaggerProperties.getLicense())
             .licenseUrl(swaggerProperties.getLicenseUrl())
             .termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
             .contact(new Contact(swaggerProperties.getContact().getName(), swaggerProperties.getContact().getUrl(), swaggerProperties.getContact().getEmail()))
             .version(swaggerProperties.getVersion())
             .build();
    }
}
```

```
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({ SwaggerAutoConfiguration.class })
public @interface EnableCustomSwagger2{

}
```

```wiki
这里主要配置了全局token,方便开发者调试.
```

![image-20210607153559024](E:\jde-work\winfo-cloud\image\image-20210607153559024.png)

![image-20210607153751136](E:\jde-work\winfo-cloud\image\image-20210607153751136.png)

![image-20210607153816823](E:\jde-work\winfo-cloud\image\image-20210607153816823.png)

#### 使用

```
在这里我已经配置好了swagger配置类和依赖,大家在使用的时候直接引入我的依赖就可以了
```

```xml
<!-- swagger 模块 -->
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-swagger</artifactId>
</dependency>
```

需要在启动类上加上我自定义的注解, 千万别加错了了 **@EnableCustomSwagger2**

```java
@SpringBootApplication
@EnableCustomSwagger2
@MapperScan(basePackages = "cc.winfo.model.demo.mapper")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### yapi

## 国际化支持

### i18n

#### 概述

​	国际化，也叫 i18n，为啥叫这个名字呢？因为国际化英文是 internationalization ，在 i 和 n 之间有 18 个字母，所以叫 i18n。我们的应用如果做了国际化就可以在不同的语言环境下，方便的进行切换，最常见的就是中文和英文之间的切换，国际化这个功能也是相当的常见。

本项目对国际化进行了自定支持需要在每个接口后面传一个参数lang,其定义下:

```java
/**
 * @Author: winfo-jiangde
 * @Description:
 * @Date: 2021/5/19 14:40
 * @Version: 1.0
 */
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

}
```

#### 使用

1.引入依赖

```xml
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-i18n</artifactId>
</dependency>
```

2.配置国际化文件地址,也可以默认,默认在resource目录下

```yaml
spring:
  messages:
    basename: messages #你的国际化配置文件目录
```

![image-20210607172332231](E:\jde-work\winfo-cloud\image\image-20210607172332231.png)

```properties
messages.properties
who_am_i=姜得恩

messages_en_US.properties
who_am_i=jiangden
```

![image-20210607172608804](E:\jde-work\winfo-cloud\image\image-20210607172608804.png)

![image-20210607172706708](E:\jde-work\winfo-cloud\image\image-20210607172706708.png)

## 工作流引擎

### Activiti

### flowable

## 规则引擎

### Drools