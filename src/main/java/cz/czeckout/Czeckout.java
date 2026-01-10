package cz.czeckout;



import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.configuration.DefaultConfigurationBuilder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.checkerframework.checker.nullness.qual.NonNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.Invoices;
import cz.czeckout.entity.PdfData;
import cz.czeckout.jackson.BigDecimalModule;
import cz.czeckout.parser.InvoiceWorkbookParser;


public class Czeckout {

    private final XmlMapper xmlMapper;
    private final JsonMapper jsonMapper;
    private final YAMLMapper yamlMapper;

    public Czeckout() {
        this.xmlMapper = configure(XmlMapper.builder())
            .defaultUseWrapper(false)
            .build();

        this.jsonMapper = configure(JsonMapper.builder())
            .build();

        this.yamlMapper = configure(YAMLMapper.builder())
            .build();
    }

    private static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B configure(@NonNull final B builder) {
        return builder
            .addModule(new BigDecimalModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL));
    }

    public static void main(String[] args) throws Exception {
        final var czechout = new Czeckout();
        czechout.render();
    }

    private void render() throws Exception {
        Path path = Paths.get(Czeckout.class.getClassLoader().getResource("Invoices.xlsx").toURI());

        final var invoiceWorkbookParser = new InvoiceWorkbookParser();
        PdfData pdfData = invoiceWorkbookParser.parse(path);
        List<Invoice> invoicesList = pdfData.getInvoices();
        final var invoices = new Invoices(invoicesList) ;

        // Your assertions here
        System.out.println("Parsed invoices: " + invoices.getInvoices().size());

        SimpleModule bigDecimalAsString = new SimpleModule();
        bigDecimalAsString.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        //JsonNode tree = jsonMapper.valueToTree(invoices);
        //String json = tree.toPrettyString();
        ////System.out.println(json);
        //Files.writeString(Path.of("invoices.json"), json);

        jsonMapper.writeValue(Path.of("invoices.json").toFile(), invoices);
        xmlMapper.writeValue(Path.of("invoices.xml").toFile(), invoices);
        //yamlMapper.writeValue(Path.of("invoices.yaml").toFile(), invoices);

        generateIndividualPdfs(pdfData);

        PDDocument document = PDDocument.load(new File("TEAMX-FINSHAPE-05-2025.pdf"));
        PDFTextStripper stripper = new PDFTextStripper();

        String text = stripper.getText(document);
        document.close();
    }

    void generateMergedPdf(PdfData pdfData) throws ConfigurationException, IOException, FOPException, TransformerException {
        final var invoices = new Invoices(pdfData.getInvoices());
        URI baseUri = new File("src/main/resources/").toURI();

        // 2. Load the config file from resources
        File configFile = new File("src/main/resources/fop.xconf");

        // 3. Build the FopFactory with the correct Base URI
        FopFactoryBuilder builder = new FopFactoryBuilder(baseUri)
            .setConfiguration(new DefaultConfigurationBuilder().buildFromFile(configFile));

        FopFactory fopFactory = builder.build();
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();

        try (
            OutputStream out = Files.newOutputStream(Path.of("invoices.pdf"));
            InputStream xsl = getClass().getClassLoader().getResourceAsStream("invoices-batch-layout.xsl")
        ) {
            if (xsl == null) throw new IllegalStateException("XSL file is missing");


            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

            // Force Saxon HE for XSLT 2.0 support
            TransformerFactory tf = new net.sf.saxon.TransformerFactoryImpl();

            // This tells Saxon: "When you see an href, look in the resources folder" so the imports in XSLT can be relative from their location
            tf.setURIResolver((href, base) -> {
                InputStream is = getClass().getClassLoader().getResourceAsStream(href);
                if (is != null) {
                    return new StreamSource(is);
                }
                return null; // Fallback to default behavior
            });

            Transformer transformer = tf.newTransformer(new StreamSource(xsl));

            // Define the base path for resolving URIs
            String systemId = new File(".").toURI().toString();

            //Source src = new StreamSource(new File("target/invoices.xml"));
            byte[] xmlBytes = xmlMapper.writeValueAsBytes(invoices);
            Source src = new StreamSource(new ByteArrayInputStream(xmlBytes));
            //src.setSystemId(new File("target/").toURI().toString());

            src.setSystemId(systemId);

            Result res = new SAXResult(fop.getDefaultHandler());
            res.setSystemId(systemId);

            pdfData.getMetadata().getVariables().forEach(transformer::setParameter);
            transformer.transform(src, res);
        }
    }

    void generateIndividualPdfs(PdfData pdfData) throws Exception {
        final var invoices = new Invoices(pdfData.getInvoices());
        URI baseUri = new File("src/main/resources/").toURI();
        File configFile = new File("src/main/resources/fop.xconf");

        FopFactoryBuilder builder = new FopFactoryBuilder(baseUri)
            .setConfiguration(new DefaultConfigurationBuilder().buildFromFile(configFile));
        FopFactory fopFactory = builder.build();

        TransformerFactory tf = new net.sf.saxon.TransformerFactoryImpl();
        tf.setURIResolver((href, base) -> {
            InputStream is = getClass().getClassLoader().getResourceAsStream(href);
            return (is != null) ? new StreamSource(is) : null;
        });

        try (InputStream xslStream = getClass().getClassLoader().getResourceAsStream("invoice-single-layout.xsl")) {
            if (xslStream == null) throw new IllegalStateException("XSL invoice-single-layout.xsl missing");

            Transformer transformer = tf.newTransformer(new StreamSource(xslStream));

            for (Invoice inv : invoices.getInvoices()) {
                // Clean filename
                String safeName = inv.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
                Path outputPath = Path.of(safeName + ".pdf");

                try (OutputStream out = Files.newOutputStream(outputPath)) {
                    Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);

                    // IMPORTANT: Explicitly set the root name to "invoice" to match the XSLT
                    byte[] xmlBytes = xmlMapper.writer()
                        .withRootName("invoice")
                        .writeValueAsBytes(inv);

                    Source src = new StreamSource(new ByteArrayInputStream(xmlBytes));
                    Result res = new SAXResult(fop.getDefaultHandler());

                    pdfData.getMetadata().getVariables().forEach(transformer::setParameter);
                    transformer.transform(src, res);
                }
            }
        }
    }
}
