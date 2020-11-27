-- 数据库初始化脚本

-- 创建数据库
create database seckill;

-- 使用数据库
use seckill;

-- 创建秒杀库存表
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

-- 初始化数据
insert into
    seckill (name,number,start_time,end_time)
values
    ("100元秒杀笔记本",100,"2020-11-16 00:00:00","2020-11-16 01:00:00"),
    ("50元秒杀自行车",100,"2020-11-16 00:00:00","2020-11-16 01:00:00"),
    ("1元秒杀手机",100,"2020-11-16 00:00:00","2020-11-16 01:00:00"),
    ("0元秒杀饮水机",100,"2020-11-16 00:00:00","2020-11-16 01:00:00");

-- 秒杀成功明细表
-- 用户登录认证相关的信息
CREATE TABLE `success_killed` (
  `seckill_id` bigint(20) NOT NULL,
  `user_phone` bigint(20) NOT NULL COMMENT '用户手机号',
  `state` tinyint(4) NOT NULL DEFAULT '0' COMMENT '状态表示： -1为无效，0为成功'
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`seckill_id`,`user_phone`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='秒杀成功明细表'