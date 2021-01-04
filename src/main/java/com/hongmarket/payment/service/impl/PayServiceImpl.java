package com.hongmarket.payment.service.impl;

import com.google.gson.Gson;
import com.hongmarket.payment.dao.PayInfoMapper;
import com.hongmarket.payment.enums.PayPlatformEnum;
import com.hongmarket.payment.pojo.PayInfo;
import com.hongmarket.payment.service.IPayService;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PayServiceImpl implements IPayService {

    private final static String QUEUE_PAY_NOTIFY = "payNotify";

    @Autowired
    private BestPayService bestPayService;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
        //write into payment table
        PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
                PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
                OrderStatusEnum.NOTPAY.name(),
                amount);

        payInfoMapper.insertSelective(payInfo);

        PayRequest request = new PayRequest();
        request.setOrderName("6534967-test");
        request.setOrderId(orderId);
        request.setOrderAmount(amount.doubleValue());
        request.setPayTypeEnum(bestPayTypeEnum);

        PayResponse response = bestPayService.pay(request);
        log.info("response={}",response);
        return response;
    }

    @Override
    public String asyncNotify(String notifyData){
        // 1.validate signature
        PayResponse payResponse = bestPayService.asyncNotify(notifyData);
        log.info("payResponse={}", payResponse);

        //2.validate price(check the price from payment table)
        PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
        if(payInfo == null){
            throw new RuntimeException("Error_orderNoIsNull");
        }
        if(!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())){
            if(payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0){
                throw new RuntimeException("Error_AmountsAreNotEqual,orderNo=" + payResponse.getOrderId());
            }

            //3.update payment status
            payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
            payInfo.setPlatformNumber(payResponse.getOutTradeNo());
            payInfo.setUpdateTime(null);
            payInfoMapper.updateByPrimaryKeySelective(payInfo);
        }

        //TODO payment sends MQ message, hongmarket receives MQ message
        amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));


        if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
            //4.notify wechat that we have received the notification
            return "<xml>\n" +
                    " <return_code><![CDATA[SUCCESS]]></return_code>\n" +
                    " <return_msg><![CDATA[OK]]></return_msg>\n" +
                    "</xml>";
        }else if(payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY){
            return "success";
        }
        throw new RuntimeException("Unsupported payment method");
    }

    @Override
    public PayInfo queryByOrderId(String orderId){
        return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
    }
}
