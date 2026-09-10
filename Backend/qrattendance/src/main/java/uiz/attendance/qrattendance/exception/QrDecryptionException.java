package uiz.attendance.qrattendance.exception;

public class QrDecryptionException extends RuntimeException {

    public QrDecryptionException(String message) {
        super(message);
    }

    public QrDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
