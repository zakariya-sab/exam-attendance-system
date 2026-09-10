package uiz.attendance.qrattendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uiz.attendance.qrattendance.model.AttendanceRecord;
import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.model.Student;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    @Query("SELECT ar FROM AttendanceRecord ar "
            + "JOIN FETCH ar.student "
            + "JOIN FETCH ar.examBlock "
            + "WHERE ar.examBlock.id = :examBlockId")
    List<AttendanceRecord> findByExamBlockId(@Param("examBlockId") Long examBlockId);

    Optional<AttendanceRecord> findByStudentAndExamBlock(Student student, ExamBlock examBlock);

    long countByExamBlockId(Long examBlockId);

    @Modifying
    @Query("DELETE FROM AttendanceRecord ar WHERE ar.examBlock.id = :examBlockId")
    void deleteByExamBlockId(@Param("examBlockId") Long examBlockId);
}
