# **Spring事务管理**
## 事务的特性
事务: 事务是逻辑上的一种操作,同一个事务内的几个步骤,要么全部成功,要么全部失败。

事务的特性(ACDI): 原子性(Atomicity)、一致性(Consistency)、隔离性(Isolation)、持久性(Durability)

- 原子性: 事务不可分割
- 一致性: 事务执行前后数据保持完整一致
- 隔离性: 一个事务在执行的时候,不应该受到其他事务的打扰
- 持久性: 事务一旦结束,数据就永久保存到数据库
### 事务的隔离级别
什么是事务的隔离级别?我们知道,隔离性是事务的四大特性之一,表示多个并发事务之间的数据要互相隔离,隔离级别就是用来描述并发事务之间隔离程度的大小
* 脏读: 一个事务读到了,另一个事务未提交的数据
* 幻读: 一个事务读到了另一个事务已经提交的insert的数据,导致多次查询的结果不一致
* 不可重复读: 一个事务读到了另一个事务已经提交的update的数据,导致多次查询的结果不一致

#### 在Spring事务管理中,为我们定义了如下隔离级别:
* ISOLATION_DEFAULT: 使用数据库默认的隔离级别
* ISOLATION_READ_UNCOMMITTED: 最低的隔离级别,允许读取已经改变而没有提交的数据,可能会导致脏读,幻读或不可重复读
* ISOLATION_READ_COMMITTED: 允许读取事务已经提交的数据,可以阻止脏读,但是幻读和不可重复读仍有可能发生
* ISOLATION_REPEATABLE_READ: 对同一字段的多次读取结果都是一致的,除非数据事务本身改变,可以阻止脏读和不可重复读,但幻读仍有可能发生
* ISOLATION_SERIALIZABLE: 最高的隔离级别,完全服从ACID的隔离级别,确保不发生脏读,幻读,不可重复读,也是最慢的事务隔离级别,因为它通常是通过完全锁定事务相关的数据库表来实现的

> 在Oracle数据库中只支持READ COMMITTED和SERIALIZABLE 这两种隔离级别。所以Oracle不支持脏读，即Oralce不允许一个会话读取其他事务未提交的数据修改结果，从而方式了由于事务回滚发生的读取不正确。
### 事务的7种传播性及示例：
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
 调用MethodA时，环境中没有事务，所以开启一个新的事务.当在MethodA中调用MethodB时，环境中已经有了一个事务，所以methodB就加入当前事务

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
在这里，我把ts1称为外层事务，ts2称为内层事务。从上面的代码可以看出，ts2与ts1是两个独立的事务，互不相干。Ts2是否成功并不依赖于ts1。如果methodA方法在调用methodB方法后的doSomeThingB方法失败了，而methodB方法所做的结果依然被提交。而除了methodB之外的其它代码导致的结果却被回滚了。使用PROPAGATION_REQUIRES_NEW,需要使用JtaTransactionManager作为事务管理器。
 
- NOT_SUPPORTED:  总是非事务地执行，并挂起任何存在的事务。使用PROPAGATION_NOT_SUPPORTED,也需要使用JtaTransactionManager作为事务管理器。（代码示例同上，可同理推出）

- NEVER 总是非事务地执行，如果存在一个活动事务，则抛出异常

- NESTED如果一个活动的事务存在，则运行在一个嵌套的事务中. 如果没有活动事务, 则按TransactionDefinition.PROPAGATION_REQUIRED 属性执行。这是一个嵌套事务,使用JDBC 3.0驱动时,仅仅支持DataSourceTransactionManager作为事务管理器。需要JDBC 驱动的java.sql.Savepoint类。有一些JTA的事务管理器实现可能也提供了同样的功能。使用PROPAGATION_NESTED，还需要把PlatformTransactionManager的nestedTransactionAllowed属性设为true;而nestedTransactionAllowed属性值默认为false;





##spring事务管理的API
* PlatformTransactionManager (平台) 事务管理器 
* TransactionDefinition 事务定义信息(事务隔离级别、传播行为、超时、只读、回滚规则)
* TransactionStatus 事务运行状态

### PlatformTransactionManager 接口           
    Spring事务策略是通过PlatformTransactionManager体现的，该接口是Spring事务策略的核心。该接口源码如下：
    
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
    可以看出，PlatformTransactionManager是一个与任何事务策略分离的接口。PlatformTransactionManager接口有许多不同的实现类，应用程序面向与平台无关的接口编程，而对不同平台的底层支持由PlatformTransactionManager接口的实现类完成，故而应用程序无须与具体的事务API耦合。因此使用PlatformTransactionManager接口，可将代码从具体的事务API中解耦出来。
    
    在PlatformTransactionManager接口内，包含一个getTransaction（TransactionDefinition definition）方法，该方法根据一个TransactionDefinition参数，返回一个TransactionStatus对象。TransactionStatus对象表示一个事务，该事务可能是一个新的事务，也可能是一个已经存在的事务对象，这由TransactionDefinition所定义的事务规则所决定。

### TransactionDefinition 接口
    TransactionDefinition 接口用于定义一个事务的规则,它包含了事务的一些静态属性,比如:事务传播行为、超时时间等。同时，Spring还为我们提供了一个默认的实现类:DefualtTransactionDefinition,该类适合于大多数情况。如果该类不能满足需求，可以通过实现TransactionDefinition接口来实现自定义事务。
    TransactionDefinition 接口包含方法和属性，如下所示：
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

    // 获取当前事务的船舶行为
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
TransactionDefinition 接口只提供了获取属性的方法，而没有提供相关设置属性的方法。因为，事务属性的设置完全是程序员控制的，
因此程序员可以自定义任何设置属性的方法，而且保存属性的字段也没有任何要求。唯一的要求的是，Spring 进行事务操作的时候，通过调用以上接口提供的方法必须能够返回事务相关的属性取值。
例如，TransactionDefinition 接口的默认的实现类 —— DefaultTransactionDefinition 就同时定义了一系列属性设置和获取方法。

#### 事务超时
所谓事务超时，就是指一个事务所允许执行的最长时间，如果超过该时间限制但事务还没有完成，则自动回滚事务。
在 TransactionDefinition 中以 int 的值来表示超时时间，其单位是秒。


#### 事务只读
事务的只读属性是指，对事务性资源进行只读操作或者是读写操作。
所谓事务性资源就是指那些被事务管理的资源，比如数据源、 JMS 资源，
以及自定义的事务性资源等等。如果确定只对事务性资源进行只读操作，那么我们可以将事务标志为只读的，
以提高事务处理的性能。在 TransactionDefinition接口中，以 boolean 类型来表示该事务是否只读。

#### 事务的回滚规则
通常情况下，如果在事务中抛出了未检查异常（继承自 RuntimeException 的异常），则默认将回滚事务。如果没有抛出任何异常，或者抛出了已检查异常，则仍然提交事务。这通常也是大多数开发者希望的处理方式，也是 EJB 中的默认处理方式。但是，我们可以根据需要人为控制事务在抛出某些未检查异常时任然提交事务，或者在抛出某些已检查异常时回滚事务。

### TransactionStatus 接口

PlatformTransactionManager.getTransaction(…) 方法返回一个 TransactionStatus 对象，
该对象可能代表一个新的或已经存在的事务（如果在当前调用堆栈有一个符合条件的事务）。
TransactionStatus 接口提供了一个简单的控制事务执行和查询事务状态的方法。该接口的源代码如下
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
    今天写这篇文章是为了纪念同事讲得两句话：1、调用flush方法等于在后面对OutputStream使劲的抽一鞭子，并命令“赶紧给我写入，我的水桶太满了”；2、写入数据量不大时，可以考虑不用。
    
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

```
接口继承了 SavepointManager 接口，因此封装了事物中回滚点的相关操作:

```java
public interface SavepointManager {

	// 创建回滚点
	Object createSavepoint() throws TransactionException;

    // 回到回滚点
	void rollbackToSavepoint(Object savepoint) throws TransactionException;

    // 释放回滚点
	void releaseSavepoint(Object savepoint) throws TransactionException;

}
```

### 基于注解的声明式事务
- @Transactional 注解的内容:
```java
public @interface Transactional {

    // 指定事务管理器名称
	@AliasFor("transactionManager")
	String value() default "";
	@AliasFor("value")
	String transactionManager() default "";

	// 事务传播属性
	Propagation propagation() default Propagation.REQUIRED;

    // 事务隔离级别
	Isolation isolation() default Isolation.DEFAULT;

	// 超时时间
	int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    // 是否是只读事务
	boolean readOnly() default false;

	// 指定事务回滚的异常类数组
	Class<? extends Throwable>[] rollbackFor() default {};

	// 指定事务回滚的异常类名字数组
	String[] rollbackForClassName() default {};

	// 忽略 异常不用回滚的类，和类型名称
	Class<? extends Throwable>[] noRollbackFor() default {};
	String[] noRollbackForClassName() default {};

}
```






