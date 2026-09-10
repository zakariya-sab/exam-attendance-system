package uiz.attendance.qrattendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.dto.MajorDto;
import uiz.attendance.qrattendance.repository.MajorRepository;

import java.util.List;

@RestController
public class MajorController {

    private final MajorRepository majorRepository;

    public MajorController(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    @GetMapping("/api/majors")
    public List<MajorDto> getAllMajors() {
        return majorRepository.findAll().stream()
                .map(MajorDto::from)
                .toList();
    }
}
