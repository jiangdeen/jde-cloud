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

#### 事务的7种传播性及示例：
 - REQUIRED: 如果有事务则加入事务，如果没有事务，则创建一个新的（默认值）
   ```java
   public class demo{
           //事务属性 PROPAGATION_REQUIRED 
           methodA{ 
           　　…… 
           　　methodB(); 
           　　…… 
           }
            
           //事务属性 PROPAGATION_REQUIRED 
           methodB{ 
              …… 
           }
   }
   ```
    在调用methodB时，没有一个存在的事务，所以获得一个新的连接，开启了一个新的事务
    调用MethodA时，环境中没有事务，所以开启一个新的事务.当在MethodA中调用MethodB时，环境中已经有了一个事务，所以 methodB就加入当前事务

- SUPPORTS:  如果存在一个事务，支持当前事务。如果没有事务，则非事务的执行
```java
    //事务属性 PROPAGATION_REQUIRED 
    methodA(){ 
      methodB(); 
    }
     
    //事务属性 PROPAGATION_SUPPORTS 
    methodB(){ 
      …… 
    }
```
　单纯的调用methodB时，methodB方法是非事务的执行的。当调用methdA时,methodB则加入了methodA的事务中,事务地执行。

- MANDATORY 如果已经存在一个事务，支持当前事务。如果没有一个活动的事务，则抛出异常
```java
    //事务属性 PROPAGATION_REQUIRED 
    methodA(){ 
        methodB(); 
    }
     
    //事务属性 PROPAGATION_MANDATORY 
    methodB(){ 
        …… 
    }
```
当单独调用methodB时，因为当前没有一个活动的事务，则会抛出异常throw new IllegalTransactionStateException(“Transaction propagation ‘mandatory’ but no existing transaction found”);当调用methodA时，methodB则加入到methodA的事务中，事务地执行。

- REQUIRES_NEW 总是开启一个新的事务。如果一个事务已经存在，则将这个存在的事务挂起。   
```java
    //事务属性 PROPAGATION_REQUIRED 
    methodA(){ 
       doSomeThingA(); 
       methodB(); 
       doSomeThingB(); 
    }
     
    //事务属性 PROPAGATION_REQUIRES_NEW 
    methodB(){ 
       …… 
    }
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
加入了这个依赖之Web异常处理就会自动生效.
```

## 系统日志

### logAspect

#### 概念

在日常开发中日志是必不可少的,除了我们常用的服务日志以外,一般还需要接口调用日志,接口调用日志可以方便我们定位问题,查看请求记录等.

在本项目中定义了log注解,用来切入需要记录日志的接口,log标签可以注解在方法上,log注解定义四个属性域,分别用来保存模块名称,功能,操作人类别,是否保存参数.

```java
**
 * @Author: winfo-jiangde
 * @Description:
 * @Date: 2021/4/26 10:09
 * @Version: 1.0
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
 public @interface Log {
    /**
     * 模块
     */
     String title() default "";

    /**
     * 功能
     */
     BusinessType businessType() default BusinessType.OTHER;

    /**
     * 操作人类别
     */
     OperatorType operatorType() default OperatorType.MANAGE;

    /**
     * 是否保存请求的参数
     */
     boolean isSaveRequestData() default true;
}
```

然后配置一个织入点

```java
// 配置织入点
@Pointcut("@annotation(cc.winfo.common.log.annotation.Log)")
public void logPointCut() {
}
```

@AfterReturning 在请求处理完成后,会进入织入点,执行切面中的方法,如果需要在目标方法执行前进入织入点则需要用@Before,如果方法执行前后都需要进行某种操作可用@Around 注解进行操作

```java
/**
 * 处理完请求后执行
 *
 * @param joinPoint 切点
 */
@AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
    handleLog(joinPoint, null, jsonResult);
}
```

请求之后的处理方法就是,获取请求的地址,请求的ip,请求的模块等信息保存到数据库中,如果发生异常,也会把异常信息保存到数据库

```java
protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
    try {
        // 获得注解
        Log controllerLog = getAnnotationLog(joinPoint);
        if (controllerLog == null) {
            return;
        }

        // *========数据库日志=========*//
        SysOperLog operLog = new SysOperLog();
        operLog.setStatus(BusinessStatus.SUCCESS.ordinal());
        // 请求的地址
        String ip = IpUtils.getIpAddr(ServletUtils.getRequest());
        operLog.setOperIp(ip);
        // 返回参数
        operLog.setJsonResult(JSON.toJSONString(jsonResult));

        operLog.setOperUrl(ServletUtils.getRequest().getRequestURI());
        String username = null;
        if (StringUtils.isNotBlank(username)) {
            operLog.setOperName(username);
        }

        if (e != null) {
            operLog.setStatus(BusinessStatus.FAIL.ordinal());
            operLog.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
        }
        // 设置方法名称
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        operLog.setMethod(className + "." + methodName + "()");
        // 设置请求方式
        operLog.setRequestMethod(ServletUtils.getRequest().getMethod());
        // 处理设置注解上的参数
        getControllerMethodDescription(joinPoint, controllerLog, operLog);
        // 保存数据库
        asyncLogService.saveSysLog(operLog);
    } catch (Exception exp) {
        // 记录本地异常日志
        log.error("==前置通知异常==");
        log.error("异常信息:{}", exp.getMessage());
        exp.printStackTrace();
    }
}
```

![image-20210609115157621](E:\jde-work\winfo-cloud\image\image-20210609115157621.png)



#### 使用

引入依赖

```xml
<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-log-aop</artifactId>
</dependency>
```

在需要记录日志的地方加入@Log注解就可以了

```java
@Log(title = "参数管理", businessType = BusinessType.INSERT)
@ApiOperation("事务隔离级别")
@PostMapping("/demo")
public String addDemo(@RequestBody Demo demo) {
    return transactionalService.addDemo(demo);
}
```

## 数据权限

### shiro

#### 概述

Apache Shiro是一个功能强大且易于使用的Java安全框架，进行身份验证，授权，加密和会话管理，可用于保护任何应用程序 - 从命令行应用程序，移动应用程序到大型的Web应用和企业应用。

Shiro可以帮助我们完成：

- 身份验证 - 证明用户身份，通常称为用户“登录”。
- 授权 - 访问控制
- 加密 - 保护隐藏数据
- 会话管理 - 每个用户对时间敏感的状态

此外，Shiro还支持一些辅助功能，例如web应用的支持，单元测试、缓存和多线程支持等，而且Shiro的API也是非常简单；其基本功能点如下图所示：

![img](E:\jde-work\winfo-cloud\image\649729-20160428153225830-1522605657.png)

在这里对各个名字进行简单的解释:

**Authentication**：身份认证/登录，验证用户是不是拥有相应的身份；

**Authorization**：授权，即权限验证，验证某个已认证的用户是否拥有某个权限；即判断用户是否能做事情，常见的如：验证某个用户是否拥有某个角色。或者细粒度的验证某个用户对某个资源是否具有某个权限；

**Session Management**：会话管理，即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；会话可以是普通JavaSE环境的，也可以是如Web环境的；

**Cryptography**：加密，保护数据的安全性，如密码加密存储到数据库，而不是明文存储；

**Web Support**：Web支持，可以非常容易的集成到Web环境；

**Caching**：缓存，比如用户登录后，其用户信息、拥有的角色/权限不必每次去查，这样可以提高效率；

**Concurrency**：shiro支持多线程应用的并发验证，即如在一个线程中开启另一个线程，能把权限自动传播过去；

**Testing**：提供测试支持；

**Run As**：允许一个用户假装为另一个用户（如果他们允许）的身份进行访问；

**Remember Me**：记住我，这个是非常常见的功能，即一次登录后，下次再来的话不用登录了。



**1）Subject：**认证主体

代表当前系统的使用者，就是用户，在Shiro的认证中，认证主体通常就是userName和passWord，或者其他用户相关的唯一标识。

**2）SecurityManager：**安全管理器

Shiro架构中最核心的组件，通过它可以协调其他组件完成用户认证和授权。实际上，SecurityManager就是Shiro框架的控制器。

**3）Realm：**域对象

定义了访问数据的方式，用来连接不同的数据源，如：关系数据库，配置文件等等。

#### shiro项目的搭建

引入依赖

```xml

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-redis</artifactId>
</dependency>

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-core</artifactId>
</dependency>

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-datasource</artifactId>
</dependency>

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-swagger</artifactId>
</dependency>

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-log-aop</artifactId>
</dependency>

<dependency>
    <groupId>cc.winfo</groupId>
    <artifactId>winfo-common-i18n</artifactId>
</dependency>


<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Shiro核心配置

```java
/**
 * Shiro 配置文件
 */
@Configuration
public class ShiroConfig {
    /**
     * Session Manager：会话管理
     * 即用户登录后就是一次会话，在没有退出之前，它的所有信息都在会话中；
     * 会话可以是普通JavaSE环境的，也可以是如Web环境的；
     */
    @Bean("sessionManager")
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //设置session过期时间
        sessionManager.setGlobalSessionTimeout(60 * 60 * 1000);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        // 去掉shiro登录时url里的JSESSIONID
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        return sessionManager;
    }

    /**
     * SecurityManager：安全管理器
     */
    @Bean("securityManager")
    public SecurityManager securityManager(UserRealm userRealm, SessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setRealm(userRealm);
        return securityManager;
    }

    /**
     * ShiroFilter是整个Shiro的入口点，用于拦截需要安全控制的请求进行处理
     */
    @Bean("shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        shiroFilter.setSecurityManager(securityManager);
        shiroFilter.setLoginUrl("/userLogin");
        shiroFilter.setUnauthorizedUrl("/");
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/userLogin", "anon");
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }

    /**
     * 管理Shiro中一些bean的生命周期
     */
    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    /**
     * 扫描上下文，寻找所有的Advistor(通知器）
     * 将这些Advisor应用到所有符合切入点的Bean中。
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }

    /**
     * 匹配所有加了 Shiro 认证注解的方法
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
```

域对象配置

```java
/**
 * Shiro 认证实体
 */
@Component
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private SysLoginMapper sysLoginMapper;

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    /**
     * 授权(验证权限时调用)
     * 获取用户权限集合
     */
    @Override
    public AuthorizationInfo doGetAuthorizationInfo
    (PrincipalCollection principals) {
        SysLogin user = (SysLogin) principals.getPrimaryPrincipal();
        if (user == null) {
            throw new UnknownAccountException("账号不存在");
        }
        List<String> permsList;
        // 默认用户拥有最高权限
        List<SysPermission> menuList = sysPermissionMapper.getSysPermission();
        permsList = new ArrayList<>(menuList.size());
        for (SysPermission menu : menuList) {
            permsList.add(menu.getPerms());
        }
        // 用户权限列表
        Set<String> permsSet = new HashSet<>();
        for (String perms : permsList) {
            if (StringUtils.isEmpty(perms)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(perms.trim().split(",")));
        }
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证(登录时调用)
     * 验证用户登录
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken authToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authToken;
        //查询用户信息
        SysLogin user = sysLoginMapper.getUserByName(token.getUsername());
        //账号不存在
        if (user == null) {
            throw new UnknownAccountException("账号或密码不正确");
        }
        //账号锁定
        if (user.getDisAble().intValue() == 0) {
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo
                (user, user.getPassword(),
                        ByteSource.Util.bytes(user.getUsername()),
                        getName());
        return info;
    }

    @Override
    public void setCredentialsMatcher(CredentialsMatcher credentialsMatcher) {
        HashedCredentialsMatcher shaCredentialsMatcher = new HashedCredentialsMatcher();
        shaCredentialsMatcher.setHashAlgorithmName(ShiroUtils.hashAlgorithmName);
        shaCredentialsMatcher.setHashIterations(ShiroUtils.hashIterations);
        super.setCredentialsMatcher(shaCredentialsMatcher);
    }
```

核心工具类

```java

/**
 * Shiro工具类
 */
public class ShiroUtils {
    /**  加密算法 */
    public final static String hashAlgorithmName = "SHA-256";
    /**  循环次数 */
    public final static int hashIterations = 16;

    public static String sha256(String password, String salt) {
        return new SimpleHash(hashAlgorithmName, password, salt, hashIterations).toString();
    }

    // 获取一个测试账号 admin
    public static void main(String[] args) {
        // 3743a4c09a17e6f2829febd09ca54e627810001cf255ddcae9dabd288a949c4a
        System.out.println(sha256("admin","123")) ;
        String hashAlgorithmName = "SHA-256";
        String credentials = "123456";
        Object obj = new SimpleHash(hashAlgorithmName, credentials, "jiangdeen", 16);
        System.out.println(obj);
    }


    /**
     * 获取会话
     */
    public static Session getSession() {
        return SecurityUtils.getSubject().getSession();
    }
    
    /**
     * Subject：主体，代表了当前“用户”
     */
    public static Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    public static SysLogin getUserEntity() {
        return (SysLogin)SecurityUtils.getSubject().getPrincipal();
    }

    public static String getUserId() {
        return getUserEntity().getUsername();
    }

    public static void setSessionAttribute(Object key, Object value) {
        getSession().setAttribute(key, value);
    }

    public static Object getSessionAttribute(Object key) {
        return getSession().getAttribute(key);
    }

    public static boolean isLogin() {
        return SecurityUtils.getSubject().getPrincipal() != null;
    }

    public static void logout() {
        SecurityUtils.getSubject().logout();
    }
```

数据库表

![image-20210610105836415](E:\jde-work\winfo-cloud\image\image-20210610105836415.png)

controller接口,登录登出:

```java
/**
 * Shrio 测试方法控制层
 */
@RestController
@RequestMapping("/login")
public class ShiroController {
    private static Logger LOGGER = LoggerFactory.getLogger(ShiroController.class);

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    /**
     * 登录测试
     * http://localhost:7011/userLogin?userName=admin&passWord=admin
     */
    @RequestMapping("/userLogin")
    public String userLogin(
            @RequestParam(value = "userName") String userName,
            @RequestParam(value = "passWord") String passWord) {
        try {
            Subject subject = ShiroUtils.getSubject();
            UsernamePasswordToken token = new UsernamePasswordToken(userName, passWord);
            subject.login(token);
            LOGGER.info("登录成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "登录成功";
    }

    /**
     * 服务器每次重启请求该接口之前必须先请求上面登录接口
     * http://localhost:7011/menu/list 获取所有菜单列表
     * 权限要求：sys:user:shiro
     */
    @RequestMapping("/menu/list")
    @RequiresPermissions("sys:user:shiro")
    public List list() {
        return sysPermissionMapper.getSysPermission();
    }

    /**
     * 用户没有该权限，无法访问
     * 权限要求：ccc:ddd:bbb
     */
    @RequestMapping("/menu/list2")
    @RequiresPermissions("ccc:ddd:bbb")
    public List list2() {
        return sysPermissionMapper.getSysPermission();
    }

    /**
     * 退出测试
     */
    @RequestMapping("/userLogOut")
    public String logout() {
        ShiroUtils.logout();
        return "success";
    }
}
```

![image-20210610110134359](E:\jde-work\winfo-cloud\image\image-20210610110134359.png)

访问有数权限接口

![image-20210610110300179](E:\jde-work\winfo-cloud\image\image-20210610110300179.png)

```json
{
  "code": 10000,
  "msg": "SUCCESS",
  "data": [
    {
      "id": "1",
      "pid": "0",
      "name": "权限菜单",
      "url": "/role",
      "perms": "user:list,user:create,sys:user:shiro",
      "type": "1",
      "icon": null,
      "orderNum": 1
    }
  ]
}
```

访问无数据权限接口

![image-20210610110417190](E:\jde-work\winfo-cloud\image\image-20210610110417190.png)

```json
{
  "code": -1,
  "msg": "Subject does not have permission [ccc:ddd:bbb]",
  "data": null
}
```

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

#### 概念

Drools 是一个基于Charles Forgy’s的RETE算法的，易于访问企业策略、易于调整以及易于管理的开源业务规则引擎，符合业内标准，速度快、效率高。

业务分析师人员或审核人员可以利用它轻松查看业务规则，从而检验是否已编码的规则执行了所需的业务规则。

Drools 是用Java语言编写的开放源码规则引擎，使用Rete算法对所编写的规则求值。

Drools允许使用声明方式表达业务逻辑。可以使用非XML的本地语言编写规则，从而便于学习和理解。

并且，还可以将Java代码直接嵌入到规则文件中，这令Drools的学习更加吸引人。

- 事实（Fact）：对象之间及对象属性之间的关系
- 规则（rule）：是由条件和结论构成的推理语句，一般表示为if…Then。一个规则的if部分称为LHS，then部分称为RHS。
- 模式（module）：就是指IF语句的条件。这里IF条件可能是有几个更小的条件组成的大条件。模式就是指的不能在继续分割下去的最小的原子条件。

Drools通过事实、规则和模式相互组合来完成工作，drools在开源规则引擎中使用率最广，但是在国内企业使用偏少，保险、支付行业使用稍多。

在本项目中我只是搭建了一个drools demo,通过运行和打印的结果来找他们直接段关系和语法规则.

#### 使用

引入依赖

```xml
<!-- https://mvnrepository.com/artifact/org.drools/drools-core -->
<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-core</artifactId>
    <drools>6.5.0.Final</drools>
</dependency>

<dependency>
    <groupId>org.drools</groupId>
    <artifactId>drools-compiler</artifactId>
    <drools>6.5.0.Final</drools>
</dependency>
```

```java
/**
 * Created by Youdmeng on 2020/1/7 0007.
 */
@Configuration
public class KiaSessionConfig {

    private static final String RULES_PATH = "rules/";

    @Bean
    public KieFileSystem kieFileSystem() throws IOException {
        KieFileSystem kieFileSystem = getKieServices().newKieFileSystem();
        for (Resource file : getRuleFiles()) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
        }
        return kieFileSystem;
    }

    private Resource[] getRuleFiles() throws IOException {

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        final Resource[] resources = resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "**/*.*");
        return resources;

    }

    @Bean
    public KieContainer kieContainer() throws IOException {

        final KieRepository kieRepository = getKieServices().getRepository();
        kieRepository.addKieModule(() -> kieRepository.getDefaultReleaseId());

        KieBuilder kieBuilder = getKieServices().newKieBuilder(kieFileSystem());
        kieBuilder.buildAll();
        return getKieServices().newKieContainer(kieRepository.getDefaultReleaseId());

    }

    private KieServices getKieServices() {
        return KieServices.Factory.get();
    }


    @Bean
    public KieBase kieBase() throws IOException {
        return kieContainer().getKieBase();
    }

    @Bean
    public KieSession kieSession() throws IOException {
        return kieContainer().newKieSession();
    }
}
```

​	![image-20210607175338500](E:\jde-work\winfo-cloud\image\image-20210607175338500.png)

drl文件解析:

```java
package cc.winfo.drools.collt
dialect "java"
import cc.winfo.drools.bean.Sensor
import cc.winfo.drools.bean.People
import java.util.List

rule "accumulate"
     no-loop false
  when
    $avg : Number() from accumulate(Sensor(temp >= 5 && $temp : temp),average($temp))
  then
    System.out.println("accumulate成功执行，平均温度为：" + $avg);
end

rule "diyaccumulate"
    when
        People(drlType == "diyaccumulate")
        $avg: Number() from accumulate(People($age: age,drlType == "diyaccumulate"),
        init(int $total = 0, $count = 0;),
        action($total += $age; $count++;),
        result($total/$count))

    then
        System.out.println("Avg: " + $avg);
end
```

package: 是一系列 rule 的一个命名空间，这个空间中所有的`rule` 名字都是唯一的,package-name命名必须遵守java命名规范.

Drools 文件中的 import 语句和 Java 的 `import` 语句类似，引入指定对象的路径及全称

一个规则通常包括三个部分：

```java
rule "name"
    attributes
    when
        LHS
    then
        RHS
end
```

- 属性部分（attribute），非必须，最好写在一行，关于**规则属性**部分，
- 条件部分（LHS） 定义当前规则的条件，如 when Message(); 判断当前workingMemory中是否存在Message对象。
- 结果部分（RHS） 即当前规则条件满足后执行的操作，可以直接调用Fact对象的方法来操作应用。这里可以写普通java代码

**属性部分:**

​	**no-loop：** `定义当前的规则是否不允许多次循环执行，默认是false`

​    **lock-on-active:**  lock-on-active true 通过这个标签，可以控制当前的规则只会被执行一次，因为一个规则的重复执行不一定是本身触发的，也可能是其他规则触发的，所以这个是no-loop的加强版.

   **date-expires：**设置规则的过期时间，默认的时间格式：“日-月-年”

   **date-effective**：设置规则的生效时间，时间格式同上。

   **duration**：规则定时，duration 3000，3秒后执行规则

   **salience**：优先级，数值越大越先执行，这个可以控制规则的执行顺序。

**条件部分:**

​	**when**：规则条件开始。条件可以单个，也可以多个，多个条件一次排列

   **操作符**：`>`、`>=`、`<`、`<=`、`==`、`!=`、`contains`、`not contains`、`memberOf`、`not memberOf`、`matches`、`not matches`

​	**memberOf：**判断某个Fact属性值是否在某个集合中，与contains不同的是他被比较的对象是一个集合，而contains被比较的对象是单个值或者对象

​	**matches**：正则表达式匹配

**结果部分:**

​	当规则条件满足，则进入规则结果部分执行，结果部分可以是纯java代码

前面简单的我就不多做介绍了,现在以一个规则为例,介绍一下drools 语法

```java
 	@Test
    public void people() {
        People people = new People();
        people.setName("达");
        people.setSex(1);
        people.setDrlType("people");
        session.insert(people);//插入
        session.fireAllRules();//执行规则
    }


rule "man"
    when
        $p : People(sex == 1 && drlType == "people")
    then
        System.out.println($p.getName() + "是男孩");
end


rule "accumulate"
     no-loop false
  when
    $avg : Number() from accumulate(Sensor(temp >= 5 && $temp : temp),average($temp))
  then
    System.out.println("accumulate成功执行，平均温度为：" + $avg);
end
/**
*  Accumulate 是一种更灵活和更强大的collect，在某种意义上他提供了不仅可以实现collect的功能还提供了额外的功能。Accumulate允*	许规则迭代对象集合，为每个元素执行自定义操作，最后返回结果对象。
*  accumulate 操作的对象可以是一个集合,Sensor类中 temp属性大于等于5的把temp赋值给$temp,average就是求$temp的平均值,然后赋*  值给$avg,这个其实不是一个条件,运行任何规则都会打印出平均温度.
*/
```

![image-20210609100534858](E:\jde-work\winfo-cloud\image\image-20210609100534858.png)

