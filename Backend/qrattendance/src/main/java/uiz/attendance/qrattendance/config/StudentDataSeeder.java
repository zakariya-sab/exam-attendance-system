package uiz.attendance.qrattendance.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import uiz.attendance.qrattendance.model.Major;
import uiz.attendance.qrattendance.model.Student;
import uiz.attendance.qrattendance.repository.MajorRepository;
import uiz.attendance.qrattendance.repository.StudentRepository;

/**
 * Seeds the (mock, in-memory H2) student table with fake data on startup so
 * there's something to look up. Goes away with {@link Student} once the real
 * student data source is wired in.
 *
 * <p>Runs after {@link CatalogDataSeeder} (see {@code @Order}) so the majors
 * assigned below already exist.
 */
@Component
@Order(2)
public class StudentDataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final MajorRepository majorRepository;

    public StudentDataSeeder(StudentRepository studentRepository, MajorRepository majorRepository) {
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
    }

    @Override
    public void run(String... args) {
        studentRepository.save(newStudent("A12345", "Zakariya", "Sabri", "GI"));
        studentRepository.save(newStudent("B12345", "Asma", "fifon", "SMI"));
        studentRepository.save(newStudent("C12345", "Mohamed", "Sabri", "GI"));
        studentRepository.save(newStudent("D12345", "Imane", "Khalil", "GC"));
        studentRepository.save(newStudent("E12345", "Karim", "Sadiq", "SMI"));
        studentRepository.save(newStudent("F12345", "Zouhair", "bili", "SMI"));
        studentRepository.save(newStudent("G12345", "Hamza", "Lofi", "SMI"));
        studentRepository.save(newStudent("H12345", "Abderrahman", "Jarna", "SMI"));
        studentRepository.save(newStudent("I12345", "Mariam", "Cyber", "SMI"));
        studentRepository.save(newStudent("J12345", "Youssef", "Aouzah", "SMI"));
        studentRepository.save(newStudent("K12345", "Mohamed Amin", "EL Bacha", "SMI"));
        studentRepository.save(newStudent("L12345", "Jawad", "Kadar", "SMI"));
    }

    private Student newStudent(String codeApogee, String firstName, String lastName, String majorCode) {
        Student student = new Student();
        student.setCodeApogee(codeApogee);
        student.setFirstName(firstName);
        student.setLastName(lastName);
        Major major = majorRepository.findByCode(majorCode).orElseThrow();
        student.setMajor(major);
        return student;
    }
}
