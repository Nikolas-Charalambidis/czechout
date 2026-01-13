package cz.czeckout.parser;

import java.util.Map;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.iban4j.CountryCode;
import org.iban4j.Iban;

import cz.czeckout.entity.Account;
import cz.czeckout.entity.Address;
import cz.czeckout.entity.Invoice;
import cz.czeckout.entity.Item;
import cz.czeckout.entity.Metadata;
import cz.czeckout.entity.Method;
import cz.czeckout.entity.Party;

public class EntityRowMapper extends ExcelCellReader {

    public EntityRowMapper(final FormulaEvaluator evaluator) {
        super(evaluator);
    }

    @NonNull
    public Invoice mapToInvoice(@NonNull final Row row,
                               @NonNull final Map<String, Integer> headerMap,
                               @NonNull final Metadata metadata) {

        final var parties = metadata.getParties();
        final var methods = metadata.getMethods();
        final var accounts = metadata.getAccounts();

        final var invoice = new Invoice();
        invoice.setName(getCellStringValue(row, headerMap.get("INVOICE")));
        invoice.setIssuer(parties.get(getCellStringValue(row, headerMap.get("ISSUER"))));
        invoice.setRecipient(parties.get(getCellStringValue(row, headerMap.get("RECIPIENT"))));
        invoice.setIssueDate(parseDate(getCellStringValue(row, headerMap.get("ISSUE DATE"))));
        invoice.setTaxDate(parseDate(getCellStringValue(row, headerMap.get("TAX DATE"))));
        invoice.setDueDate(parseDate(getCellStringValue(row, headerMap.get("DUE DATE"))));
        invoice.setMethod(methods.get(getCellStringValue(row, headerMap.get("METHOD"))));
        invoice.setAccount(accounts.get(getCellStringValue(row, headerMap.get("ACCOUNT"))));
        invoice.setVs(getCellStringValue(row, headerMap.get("VS")));
        invoice.setKs(getCellStringValue(row, headerMap.get("KS")));
        invoice.setSs(getCellStringValue(row, headerMap.get("SS")));
        invoice.setMessage(getCellStringValue(row, headerMap.get("MESSAGE")));
        invoice.setFlag(getCellStringValue(row, headerMap.get("FLAG")));
        return invoice;
    }

    @Nullable
    public Item mapToItem(@NonNull final Row row,
                         @NonNull final Map<String, Integer> headerMap) {

        final var name = getCellStringValue(row, headerMap.get("ITEM"));
        if (name == null || name.isBlank()) {
            return null;
        }
        
        final var item = new Item();
        item.setName(name);
        item.setUnit(getCellStringValue(row, headerMap.get("UNIT")));
        item.setQuantity(parseBigDecimal(row.getCell(headerMap.get("QUANTITY"))));
        item.setUnitPrice(parseMonetaryValue(row.getCell(headerMap.get("UNIT PRICE"))));
        item.setBasePrice(parseMonetaryValue(row.getCell(headerMap.get("BASE PRICE"))));
        item.setVatRate(parseBigDecimal(row.getCell(headerMap.get("VAT RATE"))));
        item.setVatPrice(parseMonetaryValue(row.getCell(headerMap.get("VAT"))));
        item.setTotalPrice(parseMonetaryValue(row.getCell(headerMap.get("TOTAL PRICE"))));
        return item;
    }

    @NonNull
    public Party mapToParty(@NonNull final Row row,
                           @NonNull final Map<String, Integer> headerMap) {

        final var party = new Party();
        party.setId(getCellStringValue(row, headerMap.get("ID")));
        party.setName(getCellStringValue(row, headerMap.get("NAME")));
        party.setIdentifierType(getCellStringValue(row, headerMap.get("IDENTIFIER TYPE")));
        party.setIdentifier(getCellStringValue(row, headerMap.get("IDENTIFIER")));
        party.setVatPrefix(getCellStringValue(row, headerMap.get("VAT PREFIX")));
        party.setVat(getCellStringValue(row, headerMap.get("VAT")));
        party.setAddressReference(getCellStringValue(row, headerMap.get("ADDRESS")));
        return party;
    }

    @NonNull
    public Address mapToAddress(@NonNull final Row row,
                               @NonNull final Map<String, Integer> headerMap) {

        final var address = new Address();
        address.setId(getCellStringValue(row, headerMap.get("ID")));
        address.setStreet(getCellStringValue(row, headerMap.get("STREET")));
        address.setHouseNumber(getCellStringValue(row, headerMap.get("HOUSE NUMBER")));
        address.setCity(getCellStringValue(row, headerMap.get("CITY")));
        address.setDistrict(getCellStringValue(row, headerMap.get("DISTRICT")));
        address.setZipCode(getCellStringValue(row, headerMap.get("ZIP CODE")));
        address.setCountry(getCellStringValue(row, headerMap.get("COUNTRY")));
        return address;
    }

    @NonNull
    public Account mapToAccount(@NonNull final Row row,
                               @NonNull final Map<String, Integer> headerMap) {

        final var account = new Account();
        account.setId(getCellStringValue(row, headerMap.get("ID")));
        account.setAccountNumber(getCellStringValue(row, headerMap.get("ACCOUNT NUMBER")));
        account.setBankCode(getCellStringValue(row, headerMap.get("BANK CODE")));
        account.setBankName(getCellStringValue(row, headerMap.get("BANK NAME")));
        
        // Generate IBAN from account details
        account.setIban(new Iban.Builder()
            .countryCode(CountryCode.CZ)
            .bankCode(account.getBankCode())
            .accountNumber(account.getAccountNumber())
            .leftPadding(true)
            .build()
            .toFormattedString());
        return account;
    }

    @NonNull
    public Method mapToPaymentMethod(@NonNull final Row row,
                                    @NonNull final Map<String, Integer> headerMap) {
        final var method = new Method();
        method.setId(getCellStringValue(row, headerMap.get("ID")));
        method.setName(getCellStringValue(row, headerMap.get("NAME")));
        return method;
    }
}
