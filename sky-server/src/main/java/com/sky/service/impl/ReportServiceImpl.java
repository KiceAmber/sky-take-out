package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 统计指定区间的营业额
     * @param start
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end) {

        // 将范围内的日期以 list 形式存储
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(start);
        while (!start.equals(end)) {
            start = start.plusDays(1);
            dateList.add(start);
        }

        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 查询 date 对应的营业额，查询当天所有完成的订单金额的总和
            LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // select sum(amount) from orders where order_time > and order_time < ? and status = 5
            Map map = new HashMap();
            map.put("start", startTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            // 如果当天没有营业额，默认为 0.0
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }

        // 将 list 转化为 String
        String dateString = StringUtils.join(dateList, ",");
        String turnoverString = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO
                .builder()
                .dateList(dateString)
                .turnoverList(turnoverString)
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 存放 begin 到 end 之间的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        // 统计新增用户数量
        List<Integer> totalUserList = new ArrayList<>();
        // 统计总的用户数量
        List<Integer> newUserList = new ArrayList<>();

        for (LocalDate date: dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("end", endTime);
            Integer totalUser = userMapper.countByMap(map); // 总用户数量

            map.put("begin", beginTime);
            Integer newUser = userMapper.countByMap(map); // 新增用户数量

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        // 存放 begin 到 end 之间的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>(); // 存放每天的订单数量
        List<Integer> validOrderCountList = new ArrayList<>(); // 存放每天的有效订单数量

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 查询每天的订单总数 select count(id) from orders where order_time > ? and order_time < ?;
            Integer orderCount = getOrderCount(beginTime, endTime, null);
            orderCountList.add(orderCount);

            // 查询每天的有效订单数 select count(id) from orders where order_time > ? and order_time < ? and status = 5;
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            validOrderCountList.add(validOrderCount);
        }

        // 计算区间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        // 计算区间内的有效订单总数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        // 计算订单完成率
        Double orderCompletionRate = 0.0;
        orderCompletionRate = (totalOrderCount == 0) ? 0.0 : totalValidOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 根据条件统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", status);

        return orderMapper.countByMap(map);
    }
}















