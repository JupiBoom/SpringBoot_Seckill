**商品秒杀项目**

技术栈

前端：BootStrap, jQuery

后端：Spring Boot, Thymeleaf, MyBaits, Redis, MySQL, Maven



## 1.  秒杀设计

### 1.1 业务分析

秒杀系统业务流程如下:

![img](https://img-blog.csdnimg.cn/img_convert/6629fd43bd1899ef653337e733001954.png)

以上是整个秒杀系统的业务流程，可以发现它其实是针对库存做的系统。而本项目仅对秒杀系统中的**秒杀功能**进行实现。

若用户成功秒杀商品，那么在系统中的操作是：

1. 减库存。

2. 记录秒杀成功的购买明细。

![img](https://img-blog.csdnimg.cn/img_convert/2b98d2502e16f1108ac6dfd8d3176336.png)

我们需要将减库存和记录购买明细的两步操作置于事务中运行，以防止出现商品的超卖和少卖情况。



### 1.2 难点分析

高并发请求对数据库产生的压力以及如何高效处理行级锁的竞争。

例如当用户A秒杀id为10的商品时，此时MySQL需要进行的操作是:

1. 开启事务。

2. 更新商品的库存信息。（获取行级锁）

3. 添加用户的购买明细，包括用户秒杀的商品id以及唯一标识用户身份的信息如电话号码等。

4. 提交事务。（释放行级锁）

若此时有另一用户B也在秒杀这件id为10的商品，他就需要等待到用户A成功秒杀到这件商品，并且MySQL成功提交了事务，他才能拿到这个id为10的商品的行级锁从而进行秒杀。但在高并发的情况下，同一时间有大量用户都在等待竞争行级锁。



### 1.3 功能实现

本项目实现了秒杀的一些功能:

1. 秒杀接口的暴露。

2. 执行秒杀的操作。

3. 相关查询。如列表查询，详情页查询。



### 1.4 数据库设计

seckill ——秒杀商品表

```sql
CREATE TABLE `seckill` (
  `seckill_id` bigint(20) NOT NULL,
  `name` varchar(120) NOT NULL COMMENT '商品名称',
  `number` int(11) NOT NULL COMMENT '库存数量',
  `start_time` timestamp NOT NULL COMMENT '秒杀开始时间',
  `end_time` timestamp NOT NULL COMMENT '秒杀结束时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`seckill_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_end_time` (`end_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀商品表'
```

success_killed ——秒杀成功明细表

```sql
CREATE TABLE `success_killed` (
  `seckill_id` bigint(20) NOT NULL,
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态表示： -1为无效，0为成功'
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`seckill_id`,`user_phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀成功明细表'
```

在秒杀成功明细表中设置联合主键防止同一用户对同一商品的多次秒杀。



### 1.5 DAO层设计

秒杀商品表的DAO：

1. 减库存（id，nowTime）
2. 由id查询商品
3. 查询商品列表

秒杀成功明细表的DAO：

1. 插入秒杀成功明细
2. 查询明细SucceesKilled对象并携带Seckill对象 (seckillId, userPhone)



### 1.6 Service层设计

#### 1.6.1 接口暴露逻辑

若当前时间还没有到秒杀时间或者已经超过秒杀时间，秒杀处于关闭状态，那么返回秒杀的开始时间和结束时间，而不返回秒杀地址。若当前时间处在秒杀时间内，返回暴露地址（秒杀商品的id、用给接口加密的md5）。

本项目采用MD5加密的方式对秒杀地址（seckill_id）进行加密，暴露给前端用户。用户执行秒杀时将传递seckill_id和md5参数，后端程序对seckill_id和salt进行MD5计算验证，若得到的值与传递的md5参数不一致，则表示地址被篡改了，拒绝执行秒杀。

#### 1.6.2 执行秒杀逻辑

在事务中进行：

1. 验证md5，看地址是否被篡改。

2. 增加秒杀成功明细，判断是否该明细被重复插入，即用户是否重复秒杀。
3. 减库存，判断时间和库存是否满足要求。

最后秒杀成功，得到购买明细信息，commit。



### 1.7 Web层设计

![img](https://img-blog.csdnimg.cn/img_convert/479aae98eae1bac6a3c8e33eac0e6c26.png)



## 2.  优化设计

### 2.1 前端控制优化

#### 2.1.1 秒杀接口暴露控制

若直接将秒杀接口地址直接写入网页文件中，用户可以通过查看源码的方式发现秒杀接口地址。那么就可能存在某些恶意用户使用第三方脚本提前进行自动秒杀的情况，这对其他用户是不公平的。而通过秒杀接口暴露控制让用户只能在秒杀时间内得到秒杀地址，且必须拿到加密后的md5才能进行秒杀，能够在一定程度上杜绝恶意秒杀行为的发生。

#### 2.1.2 秒杀按钮防重复

用户点击秒杀按钮后，按钮将置灰，从而避免同一用户短时间内重复提交秒杀请求。



### 2.2 动静态数据分离优化

#### 2.2.1 CND 缓存

将详情页的静态资源部署在CDN节点中，使得用户在访问静态资源或者详情页时不需要访问我们的系统的。

#### 2.2.2 Redis 缓存

原本秒杀商品的信息查询是通过主键直接去数据库查询的，这将增加数据库的访问压力。

我们可以将秒杀商品信息缓存在Redis中，在查询秒杀商品信息时，先去Redis缓存中查询，以此降低数据库的压力。如果在缓存中查询不到，再去数据库中查询，并将查询到的数据放入Redis缓存中，之后同样的数据就可以在缓存中直接查询到。



### 2.3 事务竞争优化

<img src="https://img-blog.csdnimg.cn/img_convert/5cafa6b6076271212c96bb6b7d2123e6.png" alt="img" style="zoom:150%;" />

**串行化操作，大量的堵塞**

#### 2.3.1 瓶颈分析

​                                                        <img src="https://img-blog.csdnimg.cn/img_convert/a2fdaa7c69fe8ad3b415b7489b4f8fff.png" alt="img" style="zoom:150%;" />

Java客户端控制事务的流程：

update减库存，网络延迟，获取行锁，update执行结果返回，可能出现的GC；然后执行insert，网络延迟，insert执行结果返回，出现的GC，最后commit或者rollback，释放行锁。之后，第二个等待行锁的线程才有可能拿到这个数据行的锁，再去执行update减库存。

优化方向：

1. 减少行级锁持有时间；
2. 减少网络延迟和可能的GC所占用的时间。

#### 2.3.2 简单优化

![img](https://img-blog.csdnimg.cn/img_convert/8ad1f08f4993a12b58214a6617fa4884.png)

将原本先update（减库存）再进行insert（插入购买明细）的步骤改成：先insert再update。

原因是update会给行加锁，insert并不会加锁，即插入是可以并行，而更新由于会加行级锁是串行的。如果更新操作在前，那么就需要执行完更新和插入以后事务提交或回滚才释放锁。而如果插入在前，更新在后，那么只有在更新时才会加行锁，在更新完且事务提交或回滚就会释放锁。这也意味着加锁和释放锁之间只有一次的网络延迟和GC，也减少了持有行级锁的时间。

#### 2.3.3 深度优化

将执行秒杀操作时的insert和update全部放到MySQL服务端的**存储过程**里，而Java客户端直接调用这个存储过程，这样就可以减小网络延迟和可能的GC的影响。另外由于使用了存储过程，也就使用不到Spring的事务管理了。

### 2.4 优化总结

![img](https://img-blog.csdnimg.cn/img_convert/9479769f9b7a11a54cc9bdc88d1f5f75.png)