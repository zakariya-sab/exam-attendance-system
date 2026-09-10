package uiz.attendance.qrattendance.dto;

import uiz.attendance.qrattendance.model.Module;

public record ModuleDto(Long id, String code, String name, Integer semester, String majorCode, String majorName) {

    public static ModuleDto from(Module module) {
        return new ModuleDto(
                module.getId(),
                module.getCode(),
                module.getName(),
                module.getSemester(),
                module.getMajor().getCode(),
                module.getMajor().getName());
    }
}
