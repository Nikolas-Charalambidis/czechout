package cz.czeckout.service;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.checkerframework.checker.nullness.qual.NonNull;

import cz.czeckout.entity.Item;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.databind.json.JsonMapper;
import cz.czeckout.entity.Invoices;
import cz.czeckout.entity.PdfData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvoiceProcessingService {

    private final DataParsingService dataParsingService;
    private final QRCodeService qrCodeService;
    private final PdfGenerationService pdfGenerationService;
    private final JsonMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public void processInvoices() throws Exception {
        // Parse data from Excel
        final var path = Paths.get(getClass().getClassLoader().getResource("Invoices.xlsx").toURI());
        final var sheetDataMap = dataParsingService.parseWorkbook(path);
        
        // Process each sheet separately
        for (var entry : sheetDataMap.entrySet()) {
            final var sheetName = entry.getKey();
            final var pdfData = entry.getValue();
            
            System.out.println("Processing sheet: " + sheetName);
            
            // Create output directory for this sheet
            //Path outputDir = Paths.get(sheetName);
            //Files.createDirectories(outputDir);
            
            // Process each invoice (calculate totals and generate QR codes)
            processInvoiceData(pdfData);
            
            // Export to different formats in sheet-specific folder
            exportToFormats(pdfData, sheetName);
            
            // Generate PDFs in sheet-specific folder
            pdfGenerationService.generateIndividualPdfs(pdfData, sheetName);
        }
        
        System.out.println("Invoice processing completed successfully for all sheets!");
    }

    private void processInvoiceData(@NonNull final PdfData pdfData) {
        for (var invoice : pdfData.getInvoices()) {
            final var totalAmount = invoice.getItems().stream()
                .map(Item::getTotalPrice)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

            final var qr = qrCodeService.generateQRCode(
                invoice, 
                invoice.getAccount(), 
                totalAmount, 
                invoice.getMessage(), 
                invoice.getDueDate(), 
                invoice.getVs(), 
                invoice.getSs(), 
                invoice.getKs()
            );
            
            // Set QR data on invoice
            invoice.setQr(qr.getCode());
            invoice.setQrSvg(qr.getSvg());
        }
        
        System.out.println("Processed invoices: " + pdfData.getInvoices().size());
    }

    private void exportToFormats(@NonNull final PdfData pdfData, @NonNull final String sheetName) throws Exception {
        final var invoices = new Invoices(pdfData.getInvoices());

        // Create output directory for this sheet
        final var outputDir = Paths.get("target/" + sheetName);
        Files.createDirectories(outputDir);

        final var bigDecimalAsString = new SimpleModule();
        bigDecimalAsString.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // Export to different formats in sheet-specific folder
        final var jsonPath = outputDir.resolve("invoices.json");
        final var xmlPath =  outputDir.resolve("invoices.xml");
        
        // Export to JSON in sheet-specific folder
        jsonMapper.writeValue(jsonPath.toFile(), invoices);
        
        // Export to XML in sheet-specific folder
        xmlMapper.writeValue(xmlPath.toFile(), invoices);
        
        // Export to YAML (commented out in original, keeping same behavior)
        // Path yamlPath = Paths.get(sheetName, "invoices.yaml");
        // yamlMapper.writeValue(yamlPath.toFile(), invoices);
        
        System.out.println("Exported invoices to JSON and XML formats in " + sheetName + " folder");
    }

    private void readGeneratedPdf(@NonNull final String fileName) throws Exception {
        final var document = PDDocument.load(new File(fileName));
        final var stripper = new PDFTextStripper();
        final var text = stripper.getText(document);
        document.close();
        
        System.out.println("Successfully read generated PDF: " + fileName);
    }
}
