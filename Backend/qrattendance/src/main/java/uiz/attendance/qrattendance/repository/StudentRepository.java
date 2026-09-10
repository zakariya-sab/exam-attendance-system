package uiz.attendance.qrattendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uiz.attendance.qrattendance.model.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByCodeApogee(String codeApogee);
}
