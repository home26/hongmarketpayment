package com.hongmarket.payment.controller;

import com.hongmarket.payment.pojo.PayInfo;
import com.hongmarket.payment.service.impl.PayServiceImpl;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayServiceImpl payServiceImpl;

    @Autowired
    private WxPayConfig wxPayConfig;

    @GetMapping("/create")
    public ModelAndView create(@RequestParam("orderId") String orderId,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("payType") BestPayTypeEnum bestPayTypeEnum
                               ){

        PayResponse response = payServiceImpl.create(orderId, amount, bestPayTypeEnum);



        //different showing with different payment type WXPAY_NATIVE uses codeUrl, ALIPAY_PC uses body.
        Map<String, String> map = new HashMap<>();
        if(bestPayTypeEnum == BestPayTypeEnum.WXPAY_NATIVE){
            map.put("codeUrl",response.getCodeUrl());
            map.put("orderId", orderId);
            map.put("returnUrl",wxPayConfig.getReturnUrl());
            return new ModelAndView("createForWxNative",map);
        }else if(bestPayTypeEnum == BestPayTypeEnum.ALIPAY_PC){
            map.put("body", response.getBody());
            return new ModelAndView("createForAlipayPc",map);
        }

        throw new RuntimeException("This type of payment is not supported now");
    }

    @PostMapping("/notify")
    @ResponseBody
    public String asyncNotify(@RequestBody String notifyData){
        return payServiceImpl.asyncNotify(notifyData);
    }

    @GetMapping("/queryByOrderId")
    @ResponseBody
    public PayInfo queryByOrderId(@RequestParam String orderId){
        log.info("Query payment record");
        return payServiceImpl.queryByOrderId(orderId);
    }
}
