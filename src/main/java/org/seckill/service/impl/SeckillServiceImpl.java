package org.seckill.service.impl;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKillDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillExeption;
import org.seckill.service.RedisService;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.thymeleaf.util.MapUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


// @Component是泛指， 而 @Service、@Controller、@Dao是特指具有某种功能的@Component
@Service
public class SeckillServiceImpl implements SeckillService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKillDao successKillDao;

    @Autowired
    private RedisService redisService;

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    @Override
    public Seckill getSeckillById(long seckillId) {
        // 优化点：缓存优化
        /**
         * get from cache
         * if null
         *      get db
         *      put into cache
         */
        Seckill seckill = redisService.getSeckill(seckillId);
        if (seckill==null){
            seckill = seckillDao.queryById(seckillId);
            if (seckill==null) {
                return null;
            }
            else {
                redisService.setSeckill(seckillId, seckill,30, TimeUnit.MINUTES);
                return seckill;
            }
        }
        return seckill;
    }

    @Override
    public Exposer exposeSeckillUrl(long seckillId) {
        Seckill seckill = getSeckillById(seckillId);
        if (seckill==null){
            return new Exposer(false,seckillId);
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime()<startTime.getTime()
            || nowTime.getTime()>endTime.getTime()){
            return new Exposer(false, seckillId, nowTime.getTime(),
                    startTime.getTime(),endTime.getTime());
        }
        // 转换特定字符出的过程，转换后不可逆
        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);
    }


    private String getMD5(long seckillId){
        // md5的盐值字符串，用于混淆md5
        String salt = "asfasfsadfwefsdvqwgqbqegq";
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法
     * 1.开发团队达成一致约定，明确标注事务方法的编程风格
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作RPC/HTTP请求。若需要其他请求，将其剥离到事务方法外部
     * 3.不是所有的方法都需要事务，如只有一条修改操作或只读操作等是不需要事务控制的。
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillCloseException, RepeatKillException, SeckillExeption {
        if (md5==null || !md5.equals(getMD5(seckillId))){
            throw new SeckillExeption("seckill data rewrite");
        }
        try {
            // 执行秒杀逻辑：1.减库存  2.记录购买行为
            Date nowTime = new Date();
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if (updateCount <= 0) {
                // 减库存失败
                throw new SeckillCloseException("seckill is closed");
            } else {
                // 减库存成功，记录购买行为
                int record = successKillDao.insertSuccessKilled(seckillId, userPhone);
                if (record <= 0) {
                    // 记录购买行为失败，重复秒杀
                    throw new RepeatKillException("seckill repeated");
                } else {
                    // 记录购买行为成功
                    SuccessKilled successKilled =
                            successKillDao.queryBySeckillIdAndUserPhoneWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        }
        catch (SeckillCloseException e1){
            throw e1;
        }
        catch (RepeatKillException e2) {
            throw e2;
        }
        catch (Exception e3){
            logger.error(e3.getMessage());
            // 所有编译器异常都转化为运行期异常
            throw new SeckillExeption("seckill inner error:"+e3.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5==null || !md5.equals(getMD5(seckillId))){
            return new SeckillExecution(seckillId,SeckillStateEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String,Object> map = new HashMap<>();
        map.put("seckillId",seckillId);
        map.put("phone",userPhone);
        map.put("killTime",killTime);
        map.put("result",-2);
        // 执行存储过程
        try{
            seckillDao.killByProcedure(map);
            // 取出result的值
            Integer result = (Integer) map.get("result");
            if (result==1) {
                SuccessKilled sk =
                        successKillDao.queryBySeckillIdAndUserPhoneWithSeckill(seckillId,userPhone);
                return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,sk);
            } else {
                return new SeckillExecution(seckillId,SeckillStateEnum.getEnum(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId,SeckillStateEnum.INNER_ERROR);
        }
    }
}
