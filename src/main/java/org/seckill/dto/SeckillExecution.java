package org.seckill.dto;

import lombok.Data;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;

/**
 * 封装秒杀执行后的结果
 */
@Data
public class SeckillExecution {

    private long seckillId;
    // 秒杀执行结果状态
    private int state;
    // 状态表示
    private String stateInfo;
    //秒杀成功对象
    private SuccessKilled successKilled;

    // 秒杀成功时调用的构造器
    public SeckillExecution(long seckillId, SeckillStateEnum seckillStateEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
        this.successKilled = successKilled;
    }
    // 秒杀失败时调用的构造器
    public SeckillExecution(long seckillId, SeckillStateEnum seckillStateEnum) {
        this.seckillId = seckillId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
    }
}
