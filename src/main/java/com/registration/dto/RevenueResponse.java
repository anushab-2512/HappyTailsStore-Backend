package com.registration.dto;

import java.math.BigDecimal;

public class RevenueResponse {

    private BigDecimal totalRevenue;
    private Long orderCount;
    private String periodLabel;

    public RevenueResponse() {}

    public RevenueResponse(BigDecimal totalRevenue, Long orderCount, String periodLabel) {
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
        this.periodLabel = periodLabel;
    }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }

    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
}