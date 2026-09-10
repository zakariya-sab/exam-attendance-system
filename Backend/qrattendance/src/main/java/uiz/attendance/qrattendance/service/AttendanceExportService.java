package uiz.attendance.qrattendance.service;

import org.springframework.stereotype.Service;
import uiz.attendance.qrattendance.exception.ExamBlockNotFoundException;
import uiz.attendance.qrattendance.model.AttendanceRecord;
import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.repository.AttendanceRecordRepository;
import uiz.attendance.qrattendance.repository.ExamBlockRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceExportService {

    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final String[] HEADERS = {
            "Code Apogee", "First Name", "Last Name", "Module Name", "Session Type",
            "Exam Scheduled Time", "Scan Time"
    };

    private final ExamBlockRepository examBlockRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceExportService(ExamBlockRepository examBlockRepository,
                                    AttendanceRecordRepository attendanceRecordRepository) {
        this.examBlockRepository = examBlockRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    public AttendanceExportResult exportAttendanceCsv(Long examBlockId) {
        ExamBlock examBlock = examBlockRepository.findByIdWithModule(examBlockId)
                .orElseThrow(() -> new ExamBlockNotFoundException("No exam block found"));

        List<AttendanceRecord> records = attendanceRecordRepository.findByExamBlockId(examBlockId);

        LocalDateTime start = examBlock.getScheduledTime();
        LocalDateTime end = start.plusMinutes(examBlock.getDurationMinutes());
        records = records.stream()
                .filter(record -> !record.getScanTime().isBefore(start) && record.getScanTime().isBefore(end))
                .toList();

        return new AttendanceExportResult(examBlock, buildCsv(records, examBlock));
    }

    private byte[] buildCsv(List<AttendanceRecord> records, ExamBlock examBlock) {
        StringBuilder csv = new StringBuilder();
        appendRow(csv, HEADERS);

        for (AttendanceRecord record : records) {
            appendRow(csv,
                    record.getStudent().getCodeApogee(),
                    record.getStudent().getFirstName(),
                    record.getStudent().getLastName(),
                    examBlock.getModule().getName(),
                    examBlock.getSessionType().toString(),
                    examBlock.getScheduledTime().toString(),
                    record.getScanTime().toString());
        }

        byte[] bodyBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + bodyBytes.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(bodyBytes, 0, result, UTF8_BOM.length, bodyBytes.length);
        return result;
    }

    private void appendRow(StringBuilder csv, String... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(values[i]));
        }
        csv.append("\r\n");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r");
        if (!needsQuoting) {
            return value;
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
