package uiz.attendance.qrattendance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.dto.ModuleDto;
import uiz.attendance.qrattendance.repository.ModuleRepository;

import java.util.List;

@RestController
public class ModuleController {

    private final ModuleRepository moduleRepository;

    public ModuleController(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }

    @GetMapping("/api/modules")
    public List<ModuleDto> getAllModules() {
        return moduleRepository.findAllWithMajor().stream()
                .map(ModuleDto::from)
                .toList();
    }
}
