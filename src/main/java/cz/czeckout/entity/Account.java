package cz.czeckout.entity;

import lombok.Data;


@Data
public class Account {

    private String id;

    private String accountNumber;

    private String bankCode;

    private String bankName;

    private String iban;
}
