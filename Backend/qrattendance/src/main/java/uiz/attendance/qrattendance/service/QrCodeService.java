package uiz.attendance.qrattendance.service;

public interface QrCodeService {

    /**
     * Encodes the given value as a QR code and renders it as a PNG image.
     *
     * @param value the plain text to encode
     * @return PNG-encoded image bytes
     */
    byte[] generateQrCodeImage(String value);
}
