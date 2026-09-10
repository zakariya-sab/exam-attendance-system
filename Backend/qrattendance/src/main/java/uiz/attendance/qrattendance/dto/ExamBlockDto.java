package uiz.attendance.qrattendance.dto;

import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.model.SessionType;

import java.time.LocalDateTime;

public record ExamBlockDto(
        Long id,
        Long moduleId,
        String moduleCode,
        String moduleName,
        SessionType sessionType,
        LocalDateTime scheduledTime,
        Integer durationMinutes,
        Integer year) {

    public static ExamBlockDto from(ExamBlock examBlock) {
        return new ExamBlockDto(
                examBlock.getId(),
                examBlock.getModule().getId(),
                examBlock.getModule().getCode(),
                examBlock.getModule().getName(),
                examBlock.getSessionType(),
                examBlock.getScheduledTime(),
                examBlock.getDurationMinutes(),
                examBlock.getYear());
    }
}
