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


public class InvoiceParser extends OoXmlParser {

    public InvoiceParser(final FormulaEvaluator evaluator) {
        super(evaluator);
    }

    @NonNull
    public Invoice parseInvoice(@NonNull final Row row,
                                @NonNull final Map<String, Integer> headerMap,
                                @NonNull final Metadata metadata) {

        final var parties = metadata.getParties();
        final var methods = metadata.getMethods();
        final var accounts = metadata.getAccounts();

        final var invoice = new Invoice();
        invoice.setName(getCellString(row, headerMap.get("INVOICE")));
        invoice.setIssuer(parties.get(getCellString(row, headerMap.get("ISSUER"))));
        invoice.setRecipient(parties.get(getCellString(row, headerMap.get("RECIPIENT"))));
        invoice.setIssueDate(parseDate(getCellString(row, headerMap.get("ISSUE DATE"))));
        invoice.setTaxDate(parseDate(getCellString(row, headerMap.get("TAX DATE"))));
        invoice.setDueDate(parseDate(getCellString(row, headerMap.get("DUE DATE"))));
        invoice.setMethod(methods.get(getCellString(row, headerMap.get("METHOD"))));
        invoice.setAccount(accounts.get(getCellString(row, headerMap.get("ACCOUNT"))));
        invoice.setVs(getCellString(row, headerMap.get("VS")));
        invoice.setKs(getCellString(row, headerMap.get("KS")));
        invoice.setSs(getCellString(row, headerMap.get("SS")));
        invoice.setMessage(getCellString(row, headerMap.get("MESSAGE")));
        invoice.setFlag(getCellString(row, headerMap.get("FLAG")));
        return invoice;
    }

    @Nullable
    public Item parseItem(@NonNull final Row row,
                          @NonNull final Map<String, Integer> headerMap) {

        final var name = getCellString(row, headerMap.get("ITEM"));
        if (name == null || name.isBlank()) {
            return null;
        }
        final var item = new Item();
        item.setName(name);
        item.setUnit(getCellString(row, headerMap.get("UNIT")));
        item.setQuantity(parseBigDecimal(row.getCell(headerMap.get("QUANTITY"))));
        item.setUnitPrice(parseMoney(row.getCell(headerMap.get("UNIT PRICE"))));
        item.setBasePrice(parseMoney(row.getCell(headerMap.get("BASE PRICE"))));
        item.setVatRate(parseBigDecimal(row.getCell(headerMap.get("VAT RATE"))));
        item.setVatPrice(parseMoney(row.getCell(headerMap.get("VAT"))));
        item.setTotalPrice(parseMoney(row.getCell(headerMap.get("TOTAL PRICE"))));
        return item;
    }

    @NonNull
    public Party parseParty(@NonNull final Row row,
                            @NonNull final Map<String, Integer> headerMap) {

        final var party = new Party();
        party.setId(getCellString(row, headerMap.get("ID")));
        party.setName(getCellString(row, headerMap.get("NAME")));
        party.setIdentifierType(getCellString(row, headerMap.get("IDENTIFIER TYPE")));
        party.setIdentifier(getCellString(row, headerMap.get("IDENTIFIER")));
        party.setVatPrefix(getCellString(row, headerMap.get("VAT PREFIX")));
        party.setVat(getCellString(row, headerMap.get("VAT")));
        party.setAddressReference(getCellString(row, headerMap.get("ADDRESS")));
        return party;
    }

    @NonNull
    public Address parseAddress(@NonNull final Row row,
                                @NonNull final Map<String, Integer> headerMap) {

        final var  a = new Address();
        a.setId(getCellString(row, headerMap.get("ID")));
        a.setStreet(getCellString(row, headerMap.get("STREET")));
        a.setHouseNumber(getCellString(row, headerMap.get("HOUSE NUMBER")));
        a.setCity(getCellString(row, headerMap.get("CITY")));
        a.setDistrict(getCellString(row, headerMap.get("DISTRICT")));
        a.setZipCode(getCellString(row, headerMap.get("ZIP CODE")));
        a.setCountry(getCellString(row, headerMap.get("COUNTRY")));
        return a;
    }

    @NonNull
    public Account parseAccount(@NonNull final Row row,
                                @NonNull final Map<String, Integer> headerMap) {

        final var account = new Account();
        account.setId(getCellString(row, headerMap.get("ID")));
        account.setAccountNumber(getCellString(row, headerMap.get("ACCOUNT NUMBER")));
        account.setBankCode(getCellString(row, headerMap.get("BANK CODE")));
        account.setBankName(getCellString(row, headerMap.get("BANK NAME")));
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
    public Method parsePaymentMethod(@NonNull final Row row,
                                     @NonNull final Map<String, Integer> headerMap) {
        final var method = new Method();
        method.setId(getCellString(row, headerMap.get("ID")));
        method.setName(getCellString(row, headerMap.get("NAME")));
        return method;
    }
}
