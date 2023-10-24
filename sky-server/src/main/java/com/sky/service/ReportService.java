package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;

public interface ReportService {

    /**
     * 统计指定区间的营业额
     * @param start
     * @param end
     * @return
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate start, LocalDate end);
}
