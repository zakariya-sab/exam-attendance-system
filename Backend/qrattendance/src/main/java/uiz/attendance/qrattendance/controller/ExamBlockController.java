package uiz.attendance.qrattendance.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.dto.ExamBlockDto;
import uiz.attendance.qrattendance.exception.ExamBlockNotFoundException;
import uiz.attendance.qrattendance.exception.InvalidModuleException;
import uiz.attendance.qrattendance.model.ExamBlock;
import uiz.attendance.qrattendance.model.Module;
import uiz.attendance.qrattendance.repository.ExamBlockRepository;
import uiz.attendance.qrattendance.repository.ModuleRepository;
import uiz.attendance.qrattendance.service.AttendanceExportResult;
import uiz.attendance.qrattendance.service.AttendanceExportService;
import uiz.attendance.qrattendance.service.ExamBlockDeletionService;

import java.util.List;

@RestController
@RequestMapping("/api/exam-blocks")
public class ExamBlockController {

    private final ExamBlockRepository examBlockRepository;
    private final ModuleRepository moduleRepository;
    private final AttendanceExportService attendanceExportService;
    private final ExamBlockDeletionService examBlockDeletionService;

    public ExamBlockController(ExamBlockRepository examBlockRepository,
                                ModuleRepository moduleRepository,
                                AttendanceExportService attendanceExportService,
                                ExamBlockDeletionService examBlockDeletionService) {
        this.examBlockRepository = examBlockRepository;
        this.moduleRepository = moduleRepository;
        this.attendanceExportService = attendanceExportService;
        this.examBlockDeletionService = examBlockDeletionService;
    }

    @PostMapping
    public ResponseEntity<ExamBlockDto> createExamBlock(@Valid @RequestBody ExamBlockRequest request) {
        Module module = findModuleOrThrow(request.moduleId());

        ExamBlock examBlock = new ExamBlock();
        examBlock.setModule(module);
        examBlock.setSessionType(request.sessionType());
        examBlock.setScheduledTime(request.scheduledTime());
        examBlock.setDurationMinutes(request.durationMinutes());
        examBlock.setYear(request.year());

        ExamBlock saved = examBlockRepository.save(examBlock);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExamBlockDto.from(saved));
    }

    @GetMapping
    public List<ExamBlockDto> getExamBlocks() {
        return examBlockRepository.findAllByOrderByScheduledTimeAsc().stream()
                .map(ExamBlockDto::from)
                .toList();
    }

    @PutMapping("/{id}")
    public ExamBlockDto updateExamBlock(@PathVariable Long id, @Valid @RequestBody ExamBlockRequest request) {
        ExamBlock existing = examBlockRepository.findById(id)
                .orElseThrow(() -> new ExamBlockNotFoundException("No exam block found "));
        Module module = findModuleOrThrow(request.moduleId());

        existing.setModule(module);
        existing.setSessionType(request.sessionType());
        existing.setScheduledTime(request.scheduledTime());
        existing.setDurationMinutes(request.durationMinutes());
        existing.setYear(request.year());

        ExamBlock saved = examBlockRepository.save(existing);
        return ExamBlockDto.from(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExamBlock(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "false") boolean force) {
        examBlockDeletionService.deleteExamBlock(id, force);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/attendance/export")
    public ResponseEntity<byte[]> exportAttendance(@PathVariable Long id) {
        AttendanceExportResult result = attendanceExportService.exportAttendanceCsv(id);
        ExamBlock examBlock = result.examBlock();

        String safeModuleName = examBlock.getModule().getName().replaceAll("[^a-zA-Z0-9]+", "_");
        String filename = safeModuleName + "_" + examBlock.getSessionType() + "_" + examBlock.getYear() + "_attendance.csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(result.csvContent());
    }

    private Module findModuleOrThrow(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new InvalidModuleException("No module found with id: " + moduleId));
    }
}
