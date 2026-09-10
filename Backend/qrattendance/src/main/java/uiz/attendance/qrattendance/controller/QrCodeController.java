package uiz.attendance.qrattendance.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uiz.attendance.qrattendance.service.QrCodeService;
import uiz.attendance.qrattendance.service.QrEncryptionService;

@RestController
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final QrEncryptionService qrEncryptionService;

    public QrCodeController(QrCodeService qrCodeService,
                             QrEncryptionService qrEncryptionService) {
        this.qrCodeService = qrCodeService;
        this.qrEncryptionService = qrEncryptionService;
    }

    @GetMapping(value = "/api/students/{codeApogee}/qr-code", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getEncryptedQrCode(@PathVariable String codeApogee) {
        String encrypted = qrEncryptionService.encrypt(codeApogee);
        return qrCodeService.generateQrCodeImage(encrypted);
    }
}
