package com.atguigu.yygh.msm.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.msm.service.MsmService;
import com.atguigu.yygh.msm.utils.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/msm")
public class MsmApiController {

    @Autowired
    private MsmService msmService;

    @Autowired
    private RedisTemplate<String,String> redisTemplate;
//     key过期时间（秒）
    private final Long EXPIRATIONTIME_SECONDS = 60L;
//     key 前缀，防止和其他地方的key可以冲突
    private final String prefix = "Demo:";

    //发送手机验证码
    @GetMapping("send/{phone}")
    public Result sendCode(@PathVariable String phone) {
        /*
        String ip = IpUtil.getIpAddr(request);
        // 前缀+ip构成key
        String key = prefix+ip;

        //检查key是否存在，返回boolean值
        boolean flag = redisTemplate.hasKey(key);
        if (flag) {
            // 调用次数+1
            redisTemplate.boundValueOps(key).increment(1);
            // 设置过期时间 设置当天过期

            Calendar curDate = Calendar.getInstance();
            Calendar tomorrowDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DATE) + 1, 0, 0, 0);
            int second = (int)(tomorrowDate.getTimeInMillis() - curDate.getTimeInMillis()) / 1000;
            redisTemplate.opsForValue().set(key, "1", second, TimeUnit.SECONDS);

            String sumVal = redisTemplate.opsForValue().get(key);
            int sum = Integer.valueOf(sumVal);
            //限制ip次数
            if (sum > 20) {
            //自定义异常，这里是验证码的要自己定义一个限制ip异常
                throw new YyghException(ResultCodeEnum.CODE_ERROR);
            }
        }else {
            // 第一次调用，所以value（调用次数）设置为1
            redisTemplate.opsForValue().set(key, "1");
        }
        String num = redisTemplate.opsForValue().get(key);
        System.out.println("第"+num+"次请求，请求成功！");
       */

        //从redis获取验证码，如果获取获取到，返回ok
        // key 手机号  value 验证码
        String code = redisTemplate.opsForValue().get(phone);
        if(!StringUtils.isEmpty(code)) {
            return Result.ok();
            /*
            * //调用service方法，通过整合短信服务进行发送
            boolean isSend = msmService.send(phone,code);
            if(isSend) {
                return Result.ok();
            } else {
                return Result.fail().message("发送短信失败");
            }
            * */
        }
        //如果从redis获取不到，
        // 生成验证码，
        code = RandomUtil.getSixBitRandom();
        //调用service方法，通过整合短信服务进行发送
        boolean isSend = msmService.send(phone,code);
        //生成验证码放到redis里面，设置有效时间
        if(isSend) {
            redisTemplate.opsForValue().set(phone,code,2, TimeUnit.MINUTES);
            return Result.ok();
        } else {
            return Result.fail().message("发送短信失败");
        }
    }
}
