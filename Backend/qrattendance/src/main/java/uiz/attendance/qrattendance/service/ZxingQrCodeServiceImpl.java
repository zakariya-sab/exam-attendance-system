package uiz.attendance.qrattendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Service
public class ZxingQrCodeServiceImpl implements QrCodeService {

    private static final int DEFAULT_SIZE_PX = 300;
    private static final String IMAGE_FORMAT = "PNG";

    @Override
    public byte[] generateQrCodeImage(String value) {
        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, DEFAULT_SIZE_PX,
                    DEFAULT_SIZE_PX);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
            return outputStream.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("Unable to generate QR code for value: " + value, e);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write QR code image", e);
        }
    }
}
