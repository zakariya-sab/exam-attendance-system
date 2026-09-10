package uiz.attendance.qrattendance.dto;

import uiz.attendance.qrattendance.model.Major;

public record MajorDto(Long id, String code, String name) {

    public static MajorDto from(Major major) {
        return new MajorDto(major.getId(), major.getCode(), major.getName());
    }
}
