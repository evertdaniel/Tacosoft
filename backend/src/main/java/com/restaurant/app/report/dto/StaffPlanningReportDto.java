package com.restaurant.app.report.dto;

import java.time.LocalDate;
import java.util.List;

/** Staff planning report with workload analysis. Implements SPEC-REPORT-001. */
public class StaffPlanningReportDto {

    private LocalDate date;
    private List<HourlyWorkloadDto> hourlyWorkload;
    private StaffRecommendationDto staffRecommendation;

    public StaffPlanningReportDto() {}

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<HourlyWorkloadDto> getHourlyWorkload() {
        return hourlyWorkload;
    }

    public void setHourlyWorkload(List<HourlyWorkloadDto> hourlyWorkload) {
        this.hourlyWorkload = hourlyWorkload;
    }

    public StaffRecommendationDto getStaffRecommendation() {
        return staffRecommendation;
    }

    public void setStaffRecommendation(StaffRecommendationDto staffRecommendation) {
        this.staffRecommendation = staffRecommendation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StaffPlanningReportDto dto = new StaffPlanningReportDto();

        public Builder date(LocalDate date) {
            dto.date = date;
            return this;
        }

        public Builder hourlyWorkload(List<HourlyWorkloadDto> hourlyWorkload) {
            dto.hourlyWorkload = hourlyWorkload;
            return this;
        }

        public Builder staffRecommendation(StaffRecommendationDto staffRecommendation) {
            dto.staffRecommendation = staffRecommendation;
            return this;
        }

        public StaffPlanningReportDto build() {
            return dto;
        }
    }
}
