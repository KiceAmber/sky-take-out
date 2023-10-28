package com.sky.service;

import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 统计指定区间的营业额
     * @param start
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end);

    /**
     * 统计指定区间的用户数据
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}
