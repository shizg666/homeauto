package com.landleaf.homeauto.center.device.common.msg.cache;

import com.landleaf.homeauto.common.constance.ErrorCodeEnumConst;
import com.landleaf.homeauto.common.exception.BusinessException;
import com.landleaf.homeauto.common.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 邮箱信息相关redis存储
 */
@Service
public class EmailMsgRedisService {

    @Autowired
    private RedisUtil redisUtil;

    @Value("${homeauto.email.code.redis-key-prefix}")
    private String redisEmailCodeKeyPrefix;


    /**
     * 存入相关信息
     *
     * @param emailMsgType
     * @param email
     * @param msg
     * @param ttl
     * @return
     */
    public boolean hsetCodeByEmail(Integer emailMsgType, String email, String msg, Long ttl) {
        return redisUtil.hsetEx(redisEmailCodeKeyPrefix + emailMsgType, email, msg, ttl);
    }


    /**
     * 获取邮箱验证码
     *
     * @param emailMsgType
     * @param email
     * @return
     */
    public String hgetCodeByEmail(Integer emailMsgType, String email) {
        Object emailCode = redisUtil.hgetEx(redisEmailCodeKeyPrefix + emailMsgType, email);
        if (emailCode == null) {
            throw new BusinessException(ErrorCodeEnumConst.ERROR_CODE_MC_EMAIL_CODE_EXPIRE);
        }
        return String.valueOf(emailCode);
    }
}
