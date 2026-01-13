package cz.czeckout.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import org.checkerframework.checker.nullness.qual.NonNull;

import tools.jackson.dataformat.xml.XmlMapper;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.Invoices;
import cz.czeckout.entity.PdfData;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PdfGenerationService {

    private final XmlMapper xmlMapper;

    public void generateMergedPdf(@NonNull final PdfData pdfData,
                                  @NonNull final String outputFileName) throws ConfigurationException, IOException, FOPException, TransformerException {

        final var invoices = new Invoices(pdfData.getInvoices());
        final var fopFactory = createFopFactory();
        final var foUserAgent = fopFactory.newFOUserAgent();

        try (
            final var out = Files.newOutputStream(Path.of(outputFileName));
            final var xsl = getClass().getClassLoader().getResourceAsStream("invoices-batch-layout.xsl")
        ) {
            if (xsl == null) {
                throw new IllegalStateException("XSL file is missing");
            }

            final var fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);
            final var tf = createTransformerFactory();
            final var transformer = tf.newTransformer(new StreamSource(xsl));
            final var systemId = new File(".").toURI().toString();
            final byte[] xmlBytes = xmlMapper.writeValueAsBytes(invoices);
            final var src = new StreamSource(new ByteArrayInputStream(xmlBytes));
            src.setSystemId(systemId);
            final var res = new SAXResult(fop.getDefaultHandler());
            res.setSystemId(systemId);
            pdfData.getMetadata().getVariables().forEach(transformer::setParameter);
            transformer.transform(src, res);
        }
    }

    public void generateIndividualPdfs(@NonNull final PdfData pdfData, @NonNull final String outputDirectory) throws Exception {
        final var invoices = new Invoices(pdfData.getInvoices());
        final var fopFactory = createFopFactory();
        final var tf = createTransformerFactory();

        try (var xslStream = getClass().getClassLoader().getResourceAsStream("invoice-single-layout.xsl")) {
            if (xslStream == null) {
                throw new IllegalStateException("XSL invoice-single-layout.xsl missing");
            }
            final var transformer = tf.newTransformer(new StreamSource(xslStream));
            for (var inv : invoices.getInvoices()) {
                generateSinglePdf(inv, pdfData, fopFactory, transformer, outputDirectory);
            }
        }
    }

    private void generateSinglePdf(@NonNull final Invoice invoice,
                                   @NonNull final PdfData pdfData,
                                   @NonNull final FopFactory fopFactory,
                                   @NonNull final Transformer transformer,
                                   @NonNull final String outputDirectory) throws Exception {
        // Clean filename
        final var safeName = invoice.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
        final var outputPath = Path.of("target/"  +outputDirectory, safeName + ".pdf");
        Files.createDirectories(outputPath.getParent());
        try (var out = Files.newOutputStream(outputPath)) {
            final var fop = fopFactory.newFop(MimeConstants.MIME_PDF, fopFactory.newFOUserAgent(), out);

            // IMPORTANT: Explicitly set the root name to "invoice" to match the XSLT
            final byte[] xmlBytes = xmlMapper.writer()
                .withRootName("invoice")
                .writeValueAsBytes(invoice);

            final var src = new StreamSource(new ByteArrayInputStream(xmlBytes));
            final var res = new SAXResult(fop.getDefaultHandler());

            pdfData.getMetadata().getVariables().forEach(transformer::setParameter);
            transformer.transform(src, res);
        }
    }

    @NonNull
    private FopFactory createFopFactory() throws ConfigurationException {
        final var baseUri = new File("src/main/resources/").toURI();
        final var configFile = new File("src/main/resources/fop.xconf");

        final var builder = new FopFactoryBuilder(baseUri)
            .setConfiguration(new DefaultConfigurationBuilder().buildFromFile(configFile));

        return builder.build();
    }

    @NonNull
    private TransformerFactory createTransformerFactory() {
        // Force Saxon HE for XSLT 2.0 support.
        final var tf = new net.sf.saxon.TransformerFactoryImpl();
        // When you see a href, look in the resources folder... so the imports in XSLT can be relative from their location.
        tf.setURIResolver((href, base) -> {
            InputStream is = getClass().getClassLoader().getResourceAsStream(href);
            return (is != null) ? new StreamSource(is) : null;
        });
        return tf;
    }
}
