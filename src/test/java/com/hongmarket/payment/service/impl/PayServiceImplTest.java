package com.hongmarket.payment.service.impl;

import com.hongmarket.payment.PaymentApplicationTests;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import org.junit.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class PayServiceImplTest extends PaymentApplicationTests {

    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void create() {
        payServiceImpl.create("jeffery26", BigDecimal.valueOf(0.01), BestPayTypeEnum.WXPAY_NATIVE);
    }

    @Test
    public void sendMQMsg(){
        amqpTemplate.convertAndSend("payNotify", "hello");
    }
}