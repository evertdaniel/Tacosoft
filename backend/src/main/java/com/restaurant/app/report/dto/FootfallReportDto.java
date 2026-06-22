package com.restaurant.app.report.dto;

import java.time.LocalDate;
import java.util.List;

/** Footfall report with peak hours and traffic patterns. Implements SPEC-REPORT-001. */
public class FootfallReportDto {

    private LocalDate orderDate;
    private List<HourlyTrafficDto> hourlyTraffic;
    private PeakHoursDto peakHours;

    public FootfallReportDto() {}

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public List<HourlyTrafficDto> getHourlyTraffic() {
        return hourlyTraffic;
    }

    public void setHourlyTraffic(List<HourlyTrafficDto> hourlyTraffic) {
        this.hourlyTraffic = hourlyTraffic;
    }

    public PeakHoursDto getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(PeakHoursDto peakHours) {
        this.peakHours = peakHours;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final FootfallReportDto dto = new FootfallReportDto();

        public Builder orderDate(LocalDate orderDate) {
            dto.orderDate = orderDate;
            return this;
        }

        public Builder hourlyTraffic(List<HourlyTrafficDto> hourlyTraffic) {
            dto.hourlyTraffic = hourlyTraffic;
            return this;
        }

        public Builder peakHours(PeakHoursDto peakHours) {
            dto.peakHours = peakHours;
            return this;
        }

        public FootfallReportDto build() {
            return dto;
        }
    }
}
