package cz.czeckout.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRawValue;
import cz.czeckout.generator.QRGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    private String name;

    private Party issuer;

    private Party recipient;

    private LocalDate issueDate;

    private LocalDate taxDate;

    private LocalDate dueDate;

    private Method method;

    private Account account;

    private String vs;

    private String ks;

    private String ss;

    private String message;

    private String flag;

    private String qr;

    @JsonRawValue
    //@JsonIgnore
    private String qrSvg;

    private List<Item> items = new ArrayList<>();

    public void refresh() {
        final var amount = items.stream().map(Item::getTotalPrice).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        final var qrGenerator = new QRGenerator(true);
        final var qr = qrGenerator.qr(this, this.getAccount(), amount, this.getMessage(), this.getIssueDate(), this.getVs(), this.getSs(), this.getKs());
        this.qr = qr.getCode();
        this.qrSvg = qr.getSvg();
    }
}
