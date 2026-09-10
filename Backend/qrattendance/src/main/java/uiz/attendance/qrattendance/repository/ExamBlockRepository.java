package uiz.attendance.qrattendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uiz.attendance.qrattendance.model.ExamBlock;

import java.util.List;
import java.util.Optional;

public interface ExamBlockRepository extends JpaRepository<ExamBlock, Long> {

    @Query("SELECT eb FROM ExamBlock eb JOIN FETCH eb.module ORDER BY eb.scheduledTime ASC")
    List<ExamBlock> findAllByOrderByScheduledTimeAsc();

    @Query("SELECT eb FROM ExamBlock eb JOIN FETCH eb.module WHERE eb.id = :id")
    Optional<ExamBlock> findByIdWithModule(@Param("id") Long id);
}
