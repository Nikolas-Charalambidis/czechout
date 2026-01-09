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

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OoXmlParser {

    private static final DataFormatter FORMATTER = new DataFormatter();

    private static final DateTimeFormatter[] DATE_FORMATS = new DateTimeFormatter[] {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("MM-dd-yy"),
        DateTimeFormatter.ofPattern("M/d/yyyy")
    };

    private final FormulaEvaluator evaluator;

    public Map<String, Integer> toHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            String v = getCellString(cell);
            if (v != null && !v.isBlank()) {
                map.put(v.trim().toUpperCase(), cell.getColumnIndex());
            }
        }
        return map;
    }

    public Row nextNonEmptyRow(Iterator<Row> it) {
        while (it.hasNext()) {
            Row r = it.next();
            if (!isRowEmpty(r)) return r;
        }
        return null;
    }

    public boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null) {
                String value = FORMATTER.formatCellValue(cell);
                if (!value.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    public Cell getFirstNonEmptyCell(Row row) {
        for (Cell cell : row) {
            String v = getCellString(cell);
            if (v != null && !v.isBlank()) return cell;
        }
        return null;
    }

    protected String getCellString(@NonNull final Row row, @Nullable final Integer cellNum) {
        return cellNum == null ? null : getCellString(row.getCell(cellNum, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

    public String getCellString(Cell cell) {
        if (cell == null) return null;

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = evaluator.evaluateFormulaCell(cell);
        }

        switch (type) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    final var bigDecimal = BigDecimal.valueOf(cell.getNumericCellValue());
                    if (bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0) {
                        return bigDecimal.toBigInteger().toString();
                    } else {
                        return bigDecimal.toPlainString();
                    }
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case BLANK:
            default:
                return "";
        }
    }

    protected LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(text, fmt);
            } catch (Exception ignored) {}
        }
        return null;
    }

    protected BigDecimal parseBigDecimal(Cell cell) {
        String val = getCellString(cell);
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        val = val.replace(",", ".").replaceAll("[^0-9.\\-]", "");
        return new BigDecimal(val);
    }

    protected BigDecimal parseMoney(Cell cell) {
        String val = getCellString(cell);
        if (val == null || val.isBlank()) return BigDecimal.ZERO;
        val = val.replace("CZK", "").replace(",", "").replaceAll("\\s+", "");
        return new BigDecimal(val);
    }
}
