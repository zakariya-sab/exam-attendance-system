package uiz.attendance.qrattendance.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.exception.InvalidCredentialsException;
import uiz.attendance.qrattendance.security.JwtService;
import uiz.attendance.qrattendance.service.IssuedToken;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String adminUsername;
    private final String adminPasswordHash;

    public AuthController(JwtService jwtService,
                           @Value("${admin.username}") String adminUsername,
                           @Value("${admin.password-hash}") String adminPasswordHash) {
        this.jwtService = jwtService;
        this.adminUsername = adminUsername;
        this.adminPasswordHash = adminPasswordHash;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        boolean usernameMatches = adminUsername.equals(request.username());
        boolean passwordMatches = passwordEncoder.matches(request.password(), adminPasswordHash);
        if (!usernameMatches || !passwordMatches) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        IssuedToken issuedToken = jwtService.generateToken(request.username());
        return new LoginResponse(issuedToken.token(), issuedToken.expiresAt());
    }
}
