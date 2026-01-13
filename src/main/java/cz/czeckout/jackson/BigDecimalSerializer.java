package cz.czeckout.jackson;

import java.math.BigDecimal;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;


public class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

    @Override
    public void serialize(@Nullable final BigDecimal value,
                          @NonNull final JsonGenerator gen,
                          @NonNull final  SerializationContext context) {

        if (value == null) {
            gen.writeNull();
            return;
        }

        if (value.scale() <= 0 || value.stripTrailingZeros().scale() <= 0) {
            gen.writeNumber(value.toBigInteger());
        } else {
            gen.writeNumber(value.toPlainString());
        }
    }
}
