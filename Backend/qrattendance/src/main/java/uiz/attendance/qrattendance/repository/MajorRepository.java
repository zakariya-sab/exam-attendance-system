package uiz.attendance.qrattendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uiz.attendance.qrattendance.model.Major;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {

    Optional<Major> findByCode(String code);

    boolean existsByCode(String code);
}
