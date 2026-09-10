package uiz.attendance.qrattendance.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScanRequest(
        @NotBlank String codeApogee,
        @NotNull Long examBlockId) {
}
