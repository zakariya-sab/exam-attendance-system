package uiz.attendance.qrattendance.service;

import uiz.attendance.qrattendance.model.AttendanceRecord;

public record ScanResult(AttendanceRecord record, boolean alreadyPresent) {
}
