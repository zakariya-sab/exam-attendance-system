package uiz.attendance.qrattendance.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.dto.PresentStudentDto;
import uiz.attendance.qrattendance.repository.AttendanceRecordRepository;
import uiz.attendance.qrattendance.service.AttendanceService;
import uiz.attendance.qrattendance.service.ScanResult;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceRecordRepository attendanceRecordRepository;

    public AttendanceController(AttendanceService attendanceService,
                                 AttendanceRecordRepository attendanceRecordRepository) {
        this.attendanceService = attendanceService;
        this.attendanceRecordRepository = attendanceRecordRepository;
    }

    @PostMapping("/scan")
    public ResponseEntity<PresentStudentDto> scan(@Valid @RequestBody ScanRequest request) {
        ScanResult scanResult = attendanceService.recordScan(request.codeApogee(), request.examBlockId());
        HttpStatus status = scanResult.alreadyPresent() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(PresentStudentDto.from(scanResult.record()));
    }

    // @GetMapping("/exam-block/{examBlockId}")
    // public List<PresentStudentDto> getByExamBlock(@PathVariable Long examBlockId) {
    //     return attendanceRecordRepository.findByExamBlockId(examBlockId).stream()
    //             .map(PresentStudentDto::from)
    //             .toList();
    // }
}
