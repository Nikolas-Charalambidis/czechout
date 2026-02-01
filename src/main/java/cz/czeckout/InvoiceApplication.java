package cz.czeckout;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;

import cz.czeckout.jackson.BigDecimalModule;
import cz.czeckout.service.DataParsingService;
import cz.czeckout.service.InvoiceProcessingService;
import cz.czeckout.service.PdfGenerationService;
import cz.czeckout.service.QRCodeService;

public class InvoiceApplication {

    private final XmlMapper xmlMapper;
    private final JsonMapper jsonMapper;
    private final InvoiceProcessingService processingService;

    public InvoiceApplication() {
        this.xmlMapper = configure(XmlMapper.builder())
            .defaultUseWrapper(false)
            .build();
        this.jsonMapper = configure(JsonMapper.builder())
            .build();
        this.processingService = createProcessingService();
    }

    @NonNull
    private static <M extends ObjectMapper, B extends MapperBuilder<M, B>> B configure(@NonNull final B builder) {
        return builder
            .addModule(new BigDecimalModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL));
    }

    @NonNull
    private InvoiceProcessingService createProcessingService() {
        final var dataParsingService = new DataParsingService();
        final var qrCodeService = new QRCodeService(true); // Generate picture files
        final var pdfGenerationService = new PdfGenerationService(xmlMapper);

        return new InvoiceProcessingService(
            dataParsingService,
            qrCodeService,
            pdfGenerationService,
            jsonMapper,
            xmlMapper
        );
    }

    public static void main(final String[] args) throws Exception {
        final var application = new InvoiceApplication();
        application.run();
    }

    public void run() throws Exception {
        processingService.processInvoices();
    }
}
