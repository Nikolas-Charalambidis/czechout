package cz.czeckout.parser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.N;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExcelCellReader {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM-dd-yy"),
        DateTimeFormatter.ofPattern("M/d/yyyy")
    };

    private final FormulaEvaluator evaluator;

    @NonNull
    public Map<String, Integer> createHeaderMap(@NonNull final Row headerRow) {
        final var map = new HashMap<String, Integer>();
        for (Cell cell : headerRow) {
            String value = getCellStringValue(cell);
            if (value != null && !value.isBlank()) {
                map.put(value.trim().toUpperCase(), cell.getColumnIndex());
            }
        }
        return map;
    }

    @Nullable
    public Row findNextNonEmptyRow(@NonNull final Iterator<Row> iterator) {
        while (iterator.hasNext()) {
            final var row = iterator.next();
            if (!isRowEmpty(row)) {
                return row;
            }
        }
        return null;
    }

    public boolean isRowEmpty(@Nullable final Row row) {
        if (row == null) {
            return true;
        }
        for (var columnIndex = row.getFirstCellNum(); columnIndex < row.getLastCellNum(); columnIndex++) {
            final var cell = row.getCell(columnIndex);
            if (cell != null) {
                final var value = FORMATTER.formatCellValue(cell);
                if (!value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    public Cell getFirstNonEmptyCell(Row row) {
        for (var cell : row) {
            final var value = getCellStringValue(cell);
            if (value != null && !value.isBlank()) {
                return cell;
            }
        }
        return null;
    }

    @Nullable
    protected String getCellStringValue(@NonNull final Row row, @Nullable final Integer columnIndex) {
        return columnIndex == null ? null : getCellStringValue(row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

    @Nullable
    public String getCellStringValue(@Nullable final Cell cell) {
        if (cell == null) {
            return null;
        }

        var cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = evaluator.evaluateFormulaCell(cell);
        }

        return switch (cellType) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    final var bigDecimal = BigDecimal.valueOf(cell.getNumericCellValue());
                    if (bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0) {
                        yield bigDecimal.toBigInteger().toString();
                    } else {
                        yield bigDecimal.toPlainString();
                    }
                }
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case _NONE, FORMULA, ERROR, BLANK -> "";
        };
    }

    @Nullable
    protected LocalDate parseDate(@Nullable final String dateText) {
        if (dateText == null || dateText.isBlank()) {
            return null;
        }
        for (var formatter : DATE_FORMATS) {
            try {
                return LocalDate.parse(dateText, formatter);
            } catch (Exception ignored) {
                // Try next format
            }
        }
        return null;
    }

    @NonNull
    protected BigDecimal parseBigDecimal(@NonNull final Cell cell) {
        var value = getCellStringValue(cell);
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        value = value.replace(",", ".").replaceAll("[^0-9.\\-]", "");
        return new BigDecimal(value);
    }

    @NonNull
    protected BigDecimal parseMonetaryValue(@NonNull final Cell cell) {
        var value = getCellStringValue(cell);
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        value = value.replace("CZK", "").replace(",", "").replaceAll("\\s+", "");
        return new BigDecimal(value);
    }
}
