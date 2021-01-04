package com.hongmarket.payment.service;

import com.hongmarket.payment.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

public interface IPayService {
    /*
     *
     * initialize payment
     */
    PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);


    String asyncNotify(String notifyData);

    PayInfo queryByOrderId(String orderId);
}
