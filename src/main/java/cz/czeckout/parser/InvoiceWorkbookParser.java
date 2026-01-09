package cz.czeckout.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import cz.czeckout.entity.Account;
import cz.czeckout.entity.Address;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.Metadata;
import cz.czeckout.entity.Method;
import cz.czeckout.entity.Party;
import cz.czeckout.entity.PdfData;


public class InvoiceWorkbookParser {

    enum Section {
        NONE,
        PARTIES,
        ADDRESSES,
        ACCOUNTS,
        METHODS,
        VARIABLES
    }

    public PdfData parse(Path path) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(Files.newInputStream(path))) {
            final var formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            final var invoiceParser = new InvoiceParser(formulaEvaluator);

            Sheet sheet = workbook.getSheet("ROOT");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet ROOT not found");
            }

            Map<String, Party> parties = new HashMap<>();
            Map<String, Address> addresses = new HashMap<>();
            Map<String, Account> accounts = new HashMap<>();
            Map<String, Method> methods = new HashMap<>();
            Map<String, String> variables = new LinkedHashMap<>();

            Section section = Section.NONE;
            Map<String, Integer> header = new HashMap<>();

            for (Row row : sheet) {
                String marker = invoiceParser.getCellString(row.getCell(1)); // column B

                if (marker == null || marker.isBlank()) {
                    continue;
                }

                // Section detection
                section = switch (marker.trim()) {
                    case "PARTIES" -> Section.PARTIES;
                    case "ADDRESSES" -> Section.ADDRESSES;
                    case "ACCOUNTS" -> Section.ACCOUNTS;
                    case "METHODS" -> Section.METHODS;
                    case "VARIABLES" -> Section.VARIABLES;
                    default -> section;
                };

                if (marker.matches("PARTIES|ADDRESSES|ACCOUNTS|METHODS|VARIABLES")) {
                    header.clear();
                    continue;
                }

                // Header row
                if (header.isEmpty()) {
                    for (Cell c : row) {
                        if (c.getCellType() == CellType.STRING) {
                            header.put(c.getStringCellValue(), c.getColumnIndex());
                        }
                    }
                    continue;
                }

                if (section == Section.VARIABLES) {
                    String key = invoiceParser.getCellString(row.getCell(header.get("KEY")));
                    String value = invoiceParser.getCellString(row.getCell(header.get("VALUE")));
                    if (key != null) {
                        variables.put(key, value);
                    }
                    continue;
                }

                // Data rows
                final var id = invoiceParser.getCellString(row.getCell(header.get("ID")));
                switch (section) {
                    case ADDRESSES -> addresses.put(id, invoiceParser.parseAddress(row, header));
                    case ACCOUNTS -> accounts.put(id, invoiceParser.parseAccount(row, header));
                    case METHODS -> methods.put(id, invoiceParser.parsePaymentMethod(row, header));
                    case PARTIES -> parties.put(id, invoiceParser.parseParty(row, header));
                }
            }

            final var metadata = new Metadata(parties, addresses, accounts, methods, variables);
            metadata.refreshReferences();

            /// //////////////////////////////

            List<Invoice> invoices = new ArrayList<>();
            Iterator<Row> it = workbook.getSheet("2026").rowIterator();

            boolean invoicesAnchorFound = false;
            Row lookahead = null;
            Invoice currentInvoice = null;
            Map<String, Integer> invoiceHeaderMap = null;

            while (true) {
                Row row;
                if (lookahead != null) {
                    row = lookahead;
                    lookahead = null;
                } else {
                    if (!it.hasNext()) {
                        break;
                    }
                    row = it.next();
                }

                if (invoiceParser.isRowEmpty(row)) {
                    continue;
                }

                Cell firstNonEmptyCell = invoiceParser.getFirstNonEmptyCell(row);
                String firstNonEmpty = invoiceParser.getCellString(firstNonEmptyCell);

                if (!invoicesAnchorFound) {
                    if ("INVOICES".equalsIgnoreCase(firstNonEmpty)) {
                        System.out.println("Found invoices sheet at " + firstNonEmptyCell.getAddress().formatAsString());
                        invoicesAnchorFound = true;
                    }
                    continue;
                }

                if ("INVOICE".equalsIgnoreCase(firstNonEmpty)) {
                    System.out.println("Found invoice at " + firstNonEmptyCell.getAddress().formatAsString());
                    // This is invoice header row
                    invoiceHeaderMap = invoiceParser.toHeaderMap(row);
                    // Next row = first invoice data row
                    Row dataRow = invoiceParser.nextNonEmptyRow(it);
                    if (dataRow != null) {
                        currentInvoice = invoiceParser.parseInvoice(dataRow, invoiceHeaderMap, metadata);
                        invoices.add(currentInvoice);
                        // Also parse first item from same row
                        final var item = invoiceParser.parseItem(dataRow, invoiceHeaderMap);
                        if (item != null) {
                            currentInvoice.getItems().add(item);
                        }
                    }
                    continue;
                }

                if (currentInvoice != null) {
                    // Check if this row is a new invoice data row
                    String possibleInvoiceNo = invoiceParser.getCellString(row.getCell(invoiceHeaderMap.get("INVOICE")));
                    if (possibleInvoiceNo != null && !possibleInvoiceNo.isBlank()) {
                        lookahead = row; // next iteration will treat as new invoice
                        currentInvoice = null;
                        continue;
                    }

                    // Otherwise, treat as an item row
                    final var item = invoiceParser.parseItem(row, invoiceHeaderMap);
                    if (item != null) {
                        currentInvoice.getItems().add(item);
                    }
                }
            }

            invoices.forEach(Invoice::refresh);

            return new PdfData(invoices, metadata);
        }
    }
}
