package cz.czeckout.entity;

import lombok.Data;


@Data
public class Account {

    private String id;

    private String accountNumber;

    private String bankCode;

    private String bankName;

    /**
     * Formatted IBAN.
     */
    private String iban;

    public String getRawIban() {
        return iban.replaceAll("\\s", "");
    }
}
