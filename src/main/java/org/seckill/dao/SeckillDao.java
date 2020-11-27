package org.seckill.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

import java.util.Date;
import java.util.List;
import java.util.Map;

//@Mapper
public interface SeckillDao {

    /**
     * 减库存
     * @param seckillId
     * @param killTime
     * @return
     */
    int reduceNumber(@Param("seckillId") long seckillId, @Param("killTime") Date killTime);

    /**
     * 依据id进行秒杀商品查询
     * @param seckillId
     * @return
     */
    Seckill queryById(long seckillId);

    /**
     * 秒杀商品的批量查询
     * @param offset
     * @param limit
     * @return
     */
    List<Seckill> queryAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 调用存储过程来执行秒杀操作
     * @param map
     */
    void killByProcedure(Map<String,Object> map);
}
