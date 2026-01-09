package cz.czeckout.entity;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class Item {

    private String name;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal basePrice;

    private BigDecimal vatRate;

    private BigDecimal vatPrice;

    private BigDecimal totalPrice;

    private String flag;
}
