package uiz.attendance.qrattendance.service;

import uiz.attendance.qrattendance.model.ExamBlock;

public record AttendanceExportResult(ExamBlock examBlock, byte[] csvContent) {
}
