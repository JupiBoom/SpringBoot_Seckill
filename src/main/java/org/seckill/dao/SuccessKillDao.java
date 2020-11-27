package org.seckill.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SuccessKilled;

//@Mapper
public interface SuccessKillDao {
    /**
     * 插入购买明细，可过滤重复，因为数据库中存在联合主键。
     * @param seckillId
     * @param userPhone
     * @return
     */
    int insertSuccessKilled(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);

    /**
     * 依据商品Id查询秒杀成功明细，并携带秒杀商品对象
     * @param seckillId
     * @param userPhone
     * @return
     */
    SuccessKilled queryBySeckillIdAndUserPhoneWithSeckill(@Param("seckillId") long seckillId, @Param("userPhone") long userPhone);
}
