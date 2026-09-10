package uiz.attendance.qrattendance.service;

import java.time.Instant;

public record IssuedToken(String token, Instant expiresAt) {
}