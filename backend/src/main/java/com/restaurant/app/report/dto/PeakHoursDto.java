package com.restaurant.app.report.dto;

import java.util.List;

/** Peak hours analysis for staff planning. Implements SPEC-REPORT-001. */
public class PeakHoursDto {

    private List<Integer> peakOrderHours;
    private List<Integer> peakPeopleHours;
    private Integer totalOrders;
    private Integer totalPeople;
    private Double averageOrdersPerHour;
    private Double averagePeoplePerHour;

    public PeakHoursDto() {}

    public List<Integer> getPeakOrderHours() {
        return peakOrderHours;
    }

    public void setPeakOrderHours(List<Integer> peakOrderHours) {
        this.peakOrderHours = peakOrderHours;
    }

    public List<Integer> getPeakPeopleHours() {
        return peakPeopleHours;
    }

    public void setPeakPeopleHours(List<Integer> peakPeopleHours) {
        this.peakPeopleHours = peakPeopleHours;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTotalPeople() {
        return totalPeople;
    }

    public void setTotalPeople(Integer totalPeople) {
        this.totalPeople = totalPeople;
    }

    public Double getAverageOrdersPerHour() {
        return averageOrdersPerHour;
    }

    public void setAverageOrdersPerHour(Double averageOrdersPerHour) {
        this.averageOrdersPerHour = averageOrdersPerHour;
    }

    public Double getAveragePeoplePerHour() {
        return averagePeoplePerHour;
    }

    public void setAveragePeoplePerHour(Double averagePeoplePerHour) {
        this.averagePeoplePerHour = averagePeoplePerHour;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PeakHoursDto dto = new PeakHoursDto();

        public Builder peakOrderHours(List<Integer> peakOrderHours) {
            dto.peakOrderHours = peakOrderHours;
            return this;
        }

        public Builder peakPeopleHours(List<Integer> peakPeopleHours) {
            dto.peakPeopleHours = peakPeopleHours;
            return this;
        }

        public Builder totalOrders(Integer totalOrders) {
            dto.totalOrders = totalOrders;
            return this;
        }

        public Builder totalPeople(Integer totalPeople) {
            dto.totalPeople = totalPeople;
            return this;
        }

        public Builder averageOrdersPerHour(Double averageOrdersPerHour) {
            dto.averageOrdersPerHour = averageOrdersPerHour;
            return this;
        }

        public Builder averagePeoplePerHour(Double averagePeoplePerHour) {
            dto.averagePeoplePerHour = averagePeoplePerHour;
            return this;
        }

        public PeakHoursDto build() {
            return dto;
        }
    }
}
