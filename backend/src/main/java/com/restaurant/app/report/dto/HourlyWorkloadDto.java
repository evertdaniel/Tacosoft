package com.restaurant.app.report.dto;

/** Hourly workload data for staff planning. Implements SPEC-REPORT-001. */
public class HourlyWorkloadDto {

    private Integer hour;
    private Long activeOrders;
    private Integer totalPeople;
    private String workloadLevel;
    private Integer recommendedStaff;

    public HourlyWorkloadDto() {}

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Long getActiveOrders() {
        return activeOrders;
    }

    public void setActiveOrders(Long activeOrders) {
        this.activeOrders = activeOrders;
    }

    public Integer getTotalPeople() {
        return totalPeople;
    }

    public void setTotalPeople(Integer totalPeople) {
        this.totalPeople = totalPeople;
    }

    public String getWorkloadLevel() {
        return workloadLevel;
    }

    public void setWorkloadLevel(String workloadLevel) {
        this.workloadLevel = workloadLevel;
    }

    public Integer getRecommendedStaff() {
        return recommendedStaff;
    }

    public void setRecommendedStaff(Integer recommendedStaff) {
        this.recommendedStaff = recommendedStaff;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final HourlyWorkloadDto dto = new HourlyWorkloadDto();

        public Builder hour(Integer hour) {
            dto.hour = hour;
            return this;
        }

        public Builder activeOrders(Long activeOrders) {
            dto.activeOrders = activeOrders;
            return this;
        }

        public Builder totalPeople(Integer totalPeople) {
            dto.totalPeople = totalPeople;
            return this;
        }

        public Builder workloadLevel(String workloadLevel) {
            dto.workloadLevel = workloadLevel;
            return this;
        }

        public Builder recommendedStaff(Integer recommendedStaff) {
            dto.recommendedStaff = recommendedStaff;
            return this;
        }

        public HourlyWorkloadDto build() {
            return dto;
        }
    }
}
