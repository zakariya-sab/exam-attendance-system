package uiz.attendance.qrattendance.controller;

import java.time.Instant;

public record LoginResponse(String token, Instant expiresAt) {
}
