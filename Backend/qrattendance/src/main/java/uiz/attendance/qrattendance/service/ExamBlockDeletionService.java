package uiz.attendance.qrattendance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uiz.attendance.qrattendance.exception.ExamBlockHasAttendanceRecordsException;
import uiz.attendance.qrattendance.exception.ExamBlockNotFoundException;
import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.repository.AttendanceRecordRepository;
import uiz.attendance.qrattendance.repository.ExamBlockRepository;

@Service
public class ExamBlockDeletionService {

    private final ExamBlockRepository examBlockRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public ExamBlockDeletionService(ExamBlockRepository examBlockRepository,
                                     AttendanceRecordRepository attendanceRecordRepository) {
        this.examBlockRepository = examBlockRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @Transactional
    public void deleteExamBlock(Long id, boolean force) {
        ExamBlock existing = examBlockRepository.findByIdWithModule(id)
                .orElseThrow(() -> new ExamBlockNotFoundException("No exam block found "));

        long attendanceCount = attendanceRecordRepository.countByExamBlockId(id);

        if (attendanceCount > 0) {
            if (!force) {
                throw new ExamBlockHasAttendanceRecordsException(
                        "Cannot delete the " + existing.getModule().getName() + " exam: it has "
                                + attendanceCount + " existing attendance record(s)");
            }
            attendanceRecordRepository.deleteByExamBlockId(id);
        }

        examBlockRepository.delete(existing);
    }
}
