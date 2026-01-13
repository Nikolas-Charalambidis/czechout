package cz.czeckout.service;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import cz.czeckout.entity.Account;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.QR;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class QRCodeService {

    private final boolean generatePictureFiles;

    @SneakyThrows
    public QR generateQRCode(Invoice invoice, Account account, BigDecimal totalAmount, String message, LocalDate dueDate, String vs, String ss, String ks) {
        final var qrString = buildQRString(account, totalAmount, message, dueDate, vs, ss, ks);
        final var svgContent = generateSvgFromText(qrString);
        
        if (generatePictureFiles) {
            saveQRCodeToFile(invoice.getName(), qrString);
        }
        
        return new QR(qrString, svgContent);
    }

    private String buildQRString(Account account, BigDecimal totalAmount, String message, LocalDate dueDate, String vs, String ss, String ks) {
        final var sb = new StringBuilder("SPD*1.0*");
        
        if (StringUtils.isNotBlank(account.getIban())) {
            sb.append("ACC:").append(account.getIban()).append("*");
        }
        if (totalAmount != null) {
            sb.append("AM:").append(totalAmount).append("*");
            sb.append("CC:").append("CZK").append("*");
        }
        if (StringUtils.isNotBlank(message)) {
            sb.append("MSG:").append(message).append("*");
        }
        if (dueDate != null) {
            sb.append("DT:").append(dueDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append("*");
        }
        if (StringUtils.isNotBlank(vs)) {
            sb.append("X-VS:").append(vs).append("*");
        }
        if (StringUtils.isNotBlank(ss)) {
            sb.append("X-SS:").append(ss).append("*");
        }
        if (StringUtils.isNotBlank(ks)) {
            sb.append("X-KS:").append(ks).append("*");
        }
        
        return sb.toString();
    }

    @SneakyThrows
    private void saveQRCodeToFile(String invoiceName, String qrString) {
        // Setup ZXing hints
        final var hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);

        // Create the Matrix
        final var matrix = new MultiFormatWriter().encode(qrString, BarcodeFormat.QR_CODE, 0, 0, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        // Initialize SVG Document
        final var document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final var svgGenerator = new SVGGraphics2D(document);

        // Set Canvas Size
        svgGenerator.setSVGCanvasSize(new Dimension(width, height));

        // Draw White Background
        svgGenerator.setColor(java.awt.Color.WHITE);
        svgGenerator.fillRect(0, 0, width, height);

        // Draw QR Modules (Black)
        svgGenerator.setColor(java.awt.Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    svgGenerator.fillRect(x, y, 1, 1);
                }
            }
        }

        // Prepare root
        final var root = svgGenerator.getRoot();
        root.setAttributeNS(null, "viewBox", String.format("0 0 %d %d", width, height));

        // Save to file
        try (var out = new OutputStreamWriter(new FileOutputStream("%s.qr.svg".formatted(invoiceName)), StandardCharsets.UTF_8)) {
            svgGenerator.stream(root, out, true, false);
        }
    }

    public String generateSvgFromText(String qrString) throws WriterException {
        // Setup ZXing hints
        final var hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);

        // Create the Matrix
        final var matrix = new MultiFormatWriter().encode(qrString, BarcodeFormat.QR_CODE, 0, 0, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        final var svg = new StringBuilder();
        svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">", width, height));
        svg.append("<rect width=\"100%\" height=\"100%\" fill=\"white\"/>"); // white background

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                if (matrix.get(x, y)) {
                    svg.append(String.format("<rect x=\"%d\" y=\"%d\" width=\"1\" height=\"1\" fill=\"black\"/>", x, y));
                }
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }
}
