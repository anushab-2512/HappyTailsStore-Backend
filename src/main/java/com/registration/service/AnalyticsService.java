package com.registration.service;

import com.registration.dto.BestSellingProductResponse;
import com.registration.dto.RevenueResponse;
import com.registration.entity.Order.Status;
import com.registration.exception.BadRequestException;
import com.registration.order.OrderItemRepository;
import com.registration.order.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AnalyticsService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public RevenueResponse dailyRevenue(String date) {
        LocalDate day;
        try {
            day = LocalDate.parse(date);
        } catch (Exception e) {
            throw new BadRequestException("Invalid date. Expected format: YYYY-MM-DD");
        }
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        BigDecimal revenue = orderRepository.sumRevenueBetween(Status.SUCCESS, start, end);
        Long count = orderRepository.countBetween(Status.SUCCESS, start, end);

        return new RevenueResponse(revenue, count, "Daily Revenue for " + day);
    }

    public RevenueResponse monthlyRevenue(Integer year, Integer month) {
        if (year == null || month == null) {
            throw new BadRequestException("Both year and month are required (YYYY, MM)");
        }
        YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(year, month);
        } catch (Exception e) {
            throw new BadRequestException("Invalid year/month combination");
        }
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal revenue = orderRepository.sumRevenueBetween(Status.SUCCESS, start, end);
        Long count = orderRepository.countBetween(Status.SUCCESS, start, end);

        return new RevenueResponse(revenue, count, "Monthly Revenue for " + yearMonth);
    }

    public RevenueResponse yearlyRevenue(Integer year) {
        if (year == null) {
            throw new BadRequestException("Year is required (YYYY)");
        }
        LocalDate janFirst;
        try {
            janFirst = LocalDate.of(year, 1, 1);
        } catch (Exception e) {
            throw new BadRequestException("Invalid year");
        }
        LocalDateTime start = janFirst.atStartOfDay();
        LocalDateTime end = janFirst.plusYears(1).atStartOfDay();

        BigDecimal revenue = orderRepository.sumRevenueBetween(Status.SUCCESS, start, end);
        Long count = orderRepository.countBetween(Status.SUCCESS, start, end);

        return new RevenueResponse(revenue, count, "Yearly Revenue for " + year);
    }

    public RevenueResponse overallRevenue() {
        BigDecimal revenue = orderRepository.sumRevenue(Status.SUCCESS);
        Long count = orderRepository.countByStatus(Status.SUCCESS);

        return new RevenueResponse(revenue, count, "Overall Revenue (All Time)");
    }

    public List<BestSellingProductResponse> getBestSellingProducts() {
        return orderItemRepository.findTopSellingProducts(Status.SUCCESS)
                .stream()
                .limit(5)
                .map(row -> {
                    Integer productId = (Integer) row[0];
                    String name = (String) row[1];
                    BigDecimal price = row[2] instanceof BigDecimal ? (BigDecimal) row[2] : new BigDecimal(row[2].toString());
                    Long totalQuantity = ((Number) row[3]).longValue();

                    BigDecimal totalRevenue = price.multiply(BigDecimal.valueOf(totalQuantity))
                            .setScale(2, RoundingMode.HALF_UP);

                    return new BestSellingProductResponse(productId, name, price, totalQuantity, totalRevenue);
                })
                .collect(Collectors.toList());
    }
}