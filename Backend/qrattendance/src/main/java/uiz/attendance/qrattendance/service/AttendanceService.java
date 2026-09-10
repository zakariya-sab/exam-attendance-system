package uiz.attendance.qrattendance.service;

import org.springframework.stereotype.Service;
import uiz.attendance.qrattendance.exception.ExamBlockNotFoundException;
import uiz.attendance.qrattendance.exception.ExamBlockNotInProgressException;
import uiz.attendance.qrattendance.exception.StudentNotFoundException;
import uiz.attendance.qrattendance.model.AttendanceRecord;
import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.model.Student;
import uiz.attendance.qrattendance.repository.AttendanceRecordRepository;
import uiz.attendance.qrattendance.repository.ExamBlockRepository;
import uiz.attendance.qrattendance.repository.StudentRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AttendanceService {

    private final StudentRepository studentRepository;
    private final ExamBlockRepository examBlockRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final QrEncryptionService qrEncryptionService;

    public AttendanceService(StudentRepository studentRepository,
                              ExamBlockRepository examBlockRepository,
                              AttendanceRecordRepository attendanceRecordRepository,
                              QrEncryptionService qrEncryptionService) {
        this.studentRepository = studentRepository;
        this.examBlockRepository = examBlockRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.qrEncryptionService = qrEncryptionService;
    }

    public ScanResult recordScan(String encryptedCodeApogee, Long examBlockId) {
        String codeApogee = qrEncryptionService.decrypt(encryptedCodeApogee);
        Student student = studentRepository.findByCodeApogee(codeApogee)
                .orElseThrow(() -> new StudentNotFoundException("No student found with code apogée: " + codeApogee));
        ExamBlock examBlock = examBlockRepository.findById(examBlockId)
                .orElseThrow(() -> new ExamBlockNotFoundException("No exam block found with id: " + examBlockId));

        LocalDateTime now = LocalDateTime.now();
        if (!examBlock.isInProgress(now)) {
            if (now.isBefore(examBlock.getScheduledTime())) {
                throw new ExamBlockNotInProgressException(
                        "Exam has not started yet. It starts at " + examBlock.getScheduledTime() + ".");
            }
            throw new ExamBlockNotInProgressException(
                    "Exam has already finished. It ended at " + examBlock.getEndTime() + ".");
        }

        Optional<AttendanceRecord> existingRecord =
                attendanceRecordRepository.findByStudentAndExamBlock(student, examBlock);
        if (existingRecord.isPresent()) {
            return new ScanResult(existingRecord.get(), true);
        }

        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setStudent(student);
        attendanceRecord.setExamBlock(examBlock);
        attendanceRecord.setScanTime(LocalDateTime.now());
        attendanceRecord.setYear(examBlock.getYear());

        AttendanceRecord saved = attendanceRecordRepository.save(attendanceRecord);
        return new ScanResult(saved, false);
    }
}
