package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ? ") // 每分钟触发一次
    public void processTimeoutOrder() {

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        // 首先查询超时订单有哪些
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if (orderList != null && orderList.size() > 0) {
            // 修改订单状态为 已取消
            for (Orders orders : orderList) {
                orders.setStatus(Orders.CANCELLED); // 设置订单状态
                orders.setCancelReason("订单超时，已自动取消"); // 取消原因
                orders.setCancelTime(LocalDateTime.now()); // 取消时间
                orderMapper.update(orders);
            }
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨 1 点触发一次
    public void processDeliveryOrder() {

        LocalDateTime time = LocalDateTime.now().plusHours(-1);
        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (orderList != null && orderList.size() > 0) {
            // 修改订单状态为 已取消
            for (Orders orders : orderList) {
                orders.setStatus(Orders.COMPLETED); // 设置订单状态
                orderMapper.update(orders);
            }
        }
    }
}
