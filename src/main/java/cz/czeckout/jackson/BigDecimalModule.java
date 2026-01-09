package cz.czeckout.jackson;

import java.math.BigDecimal;

import tools.jackson.databind.module.SimpleModule;


public class BigDecimalModule extends SimpleModule {

    public BigDecimalModule() {
        // Register your custom serializer for BigDecimal
        this.addSerializer(BigDecimal.class, new BigDecimalSerializer());
    }
}
