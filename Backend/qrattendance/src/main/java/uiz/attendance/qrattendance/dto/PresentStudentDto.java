package uiz.attendance.qrattendance.dto;

import uiz.attendance.qrattendance.model.AttendanceRecord;

import java.time.LocalDateTime;

public record PresentStudentDto(String codeApogee, String firstName, String lastName, LocalDateTime scanTime) {

    public static PresentStudentDto from(AttendanceRecord attendanceRecord) {
        return new PresentStudentDto(
                attendanceRecord.getStudent().getCodeApogee(),
                attendanceRecord.getStudent().getFirstName(),
                attendanceRecord.getStudent().getLastName(),
                attendanceRecord.getScanTime());
    }
}
