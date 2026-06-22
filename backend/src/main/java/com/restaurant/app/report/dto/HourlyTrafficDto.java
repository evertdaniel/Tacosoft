package com.restaurant.app.report.dto;

/** Hourly traffic data for footfall analysis. Implements SPEC-REPORT-001. */
public class HourlyTrafficDto {

    private Integer hour;
    private Long orderCount;
    private Integer totalPeople;
    private Double averagePeoplePerOrder;

    public HourlyTrafficDto() {}

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }

    public Integer getTotalPeople() {
        return totalPeople;
    }

    public void setTotalPeople(Integer totalPeople) {
        this.totalPeople = totalPeople;
    }

    public Double getAveragePeoplePerOrder() {
        return averagePeoplePerOrder;
    }

    public void setAveragePeoplePerOrder(Double averagePeoplePerOrder) {
        this.averagePeoplePerOrder = averagePeoplePerOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HourlyTrafficDto dto = new HourlyTrafficDto();

        public Builder hour(Integer hour) {
            dto.hour = hour;
            return this;
        }

        public Builder orderCount(Long orderCount) {
            dto.orderCount = orderCount;
            return this;
        }

        public Builder totalPeople(Integer totalPeople) {
            dto.totalPeople = totalPeople;
            return this;
        }

        public Builder averagePeoplePerOrder(Double averagePeoplePerOrder) {
            dto.averagePeoplePerOrder = averagePeoplePerOrder;
            return this;
        }

        public HourlyTrafficDto build() {
            return dto;
        }
    }
}
