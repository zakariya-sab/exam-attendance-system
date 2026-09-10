package uiz.attendance.qrattendance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uiz.attendance.qrattendance.model.Module;

import java.util.List;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    boolean existsByCode(String code);

    @Query("SELECT m FROM Module m JOIN FETCH m.major")
    List<Module> findAllWithMajor();
}
