package cz.czeckout.generator;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import cz.czeckout.entity.Account;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.QR;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;


@RequiredArgsConstructor
public class QRGenerator {

    private final boolean generatePicture;


    @SneakyThrows
    public QR qr(Invoice invoice, Account account, BigDecimal totalAmount, String message, LocalDate dueDate, String vs, String ss, String ks) {
        // 1. Generate the QR String (Czech SPD Standard)

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
        if (StringUtils.isNotBlank(message)) {
            sb.append("X-KS:").append(ks).append("*");
        }

        String qrString = sb.toString();

        // 2. Setup ZXing hints (Using Level M for logo safety)
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);

        // 3. Create the Matrix
        BitMatrix matrix = new MultiFormatWriter().encode(qrString, BarcodeFormat.QR_CODE, 0, 0, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        // 4. Initialize SVG Document
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

        // Set Canvas Size
        svgGenerator.setSVGCanvasSize(new Dimension(width, height));

        // 5. Draw White Background
        svgGenerator.setColor(java.awt.Color.WHITE);
        svgGenerator.fillRect(0, 0, width, height);

        // 6. Draw QR Modules (Black)
        svgGenerator.setColor(java.awt.Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix.get(x, y)) {
                    svgGenerator.fillRect(x, y, 1, 1);
                }
            }
        }

        // 7. Add Vector "CZK" Logo in the center
        //drawCenterLogo(svgGenerator, width, height);

        // Step 8. Prepare root
        Element root = svgGenerator.getRoot();
        root.setAttributeNS(null, "viewBox", String.format("0 0 %d %d", width, height));

        // Step 9. Export to String instead of File
        StringWriter writer = new StringWriter();
        svgGenerator.stream(root, writer, false, false); // use false for useCSS to keep it clean

        if (generatePicture) {
            try (Writer out = new OutputStreamWriter(new FileOutputStream("%s.qr.svg".formatted(invoice.getName())), StandardCharsets.UTF_8)) {
                svgGenerator.stream(root, out, true, false);
            }
        }

        return new QR(qrString, generateSvgFromText(qrString));



        //// 8. Prepare for FOP (Set viewBox for easy resizing)
        //Element root = svgGenerator.getRoot();
        //root.setAttributeNS(null, "viewBox", String.format("0 0 %d %d", width, height));
        //// Remove hardcoded width/height so FOP handles scaling
        //root.removeAttributeNS(null, "width");
        //root.removeAttributeNS(null, "height");
        //
        //// 9. Write to file
        //

    }

    public String generateSvgFromText(String qrString) throws WriterException {
        // 2. Setup ZXing hints (Using Level M for logo safety)
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0);

        // 3. Create the Matrix
        BitMatrix matrix = new MultiFormatWriter().encode(qrString, BarcodeFormat.QR_CODE, 0, 0, hints);
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"%d\" height=\"%d\">",  width, height));
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
