package uiz.attendance.qrattendance.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import uiz.attendance.qrattendance.model.SessionType;

import java.time.LocalDateTime;

public record ExamBlockRequest(
        @NotNull Long moduleId,
        @NotNull SessionType sessionType,
        @NotNull LocalDateTime scheduledTime,
        @NotNull
        @Min(value = 1, message = "duration must be at least 1 minute")
        @Max(value = 1440, message = "duration must be at most 1440 minutes")
        Integer durationMinutes,
        @NotNull
        @Min(value = 1400, message = "exam year must be at least 1400")
        Integer year) {
}
