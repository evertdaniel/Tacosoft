package com.restaurant.app.report.dto;

import java.util.List;

/** Staff recommendation based on workload analysis. Implements SPEC-REPORT-001. */
public class StaffRecommendationDto {

    private Integer minimumStaff;
    private Integer recommendedStaff;
    private Integer peakStaff;
    private List<String> peakHours;
    private String rationale;

    public StaffRecommendationDto() {}

    public Integer getMinimumStaff() {
        return minimumStaff;
    }

    public void setMinimumStaff(Integer minimumStaff) {
        this.minimumStaff = minimumStaff;
    }

    public Integer getRecommendedStaff() {
        return recommendedStaff;
    }

    public void setRecommendedStaff(Integer recommendedStaff) {
        this.recommendedStaff = recommendedStaff;
    }

    public Integer getPeakStaff() {
        return peakStaff;
    }

    public void setPeakStaff(Integer peakStaff) {
        this.peakStaff = peakStaff;
    }

    public List<String> getPeakHours() {
        return peakHours;
    }

    public void setPeakHours(List<String> peakHours) {
        this.peakHours = peakHours;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final StaffRecommendationDto dto = new StaffRecommendationDto();

        public Builder minimumStaff(Integer minimumStaff) {
            dto.minimumStaff = minimumStaff;
            return this;
        }

        public Builder recommendedStaff(Integer recommendedStaff) {
            dto.recommendedStaff = recommendedStaff;
            return this;
        }

        public Builder peakStaff(Integer peakStaff) {
            dto.peakStaff = peakStaff;
            return this;
        }

        public Builder peakHours(List<String> peakHours) {
            dto.peakHours = peakHours;
            return this;
        }

        public Builder rationale(String rationale) {
            dto.rationale = rationale;
            return this;
        }

        public StaffRecommendationDto build() {
            return dto;
        }
    }
}
