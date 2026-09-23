package agendoc.docs;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link XlsxMarkdownConverter}.
 */
class XlsxMarkdownConverterTest
{

    @TempDir
    Path tempDir;

    @Test
    void toMarkdownShouldConvertSheetHeadingAndTable() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "simple.xlsx", workbook ->
        {
            Sheet sheet = workbook.createSheet("Stock");
            DocumentFixtures.appendTextRow(sheet, 0, "Name", "Qty");
            DocumentFixtures.appendTextRow(sheet, 1, "Bolt", "12");
        });

        String expected = "# Stock\n\n"
            + "| Name | Qty |\n"
            + "| --- | --- |\n"
            + "| Bolt | 12 |\n\n";

        assertEquals(expected, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldConvertEverySheet() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "sheets.xlsx", workbook ->
        {
            Sheet first = workbook.createSheet("First");
            DocumentFixtures.appendTextRow(first, 0, "A");
            DocumentFixtures.appendTextRow(first, 1, "1");

            Sheet second = workbook.createSheet("Second");
            DocumentFixtures.appendTextRow(second, 0, "B");
            DocumentFixtures.appendTextRow(second, 1, "2");
        });

        String expected = "# First\n\n"
            + "| A |\n"
            + "| --- |\n"
            + "| 1 |\n\n"
            + "# Second\n\n"
            + "| B |\n"
            + "| --- |\n"
            + "| 2 |\n\n";

        assertEquals(expected, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldExportFormulaResultAsValue() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "formula.xlsx", workbook ->
        {
            Sheet sheet = workbook.createSheet("Calc");
            DocumentFixtures.appendNumericRow(sheet, 0, 2.0, 3.0);
            Row row = sheet.createRow(1);
            row.createCell(0).setCellFormula("A1+B1");
            row.createCell(1).setCellValue("sum");
        });

        String markdown = XlsxMarkdownConverter.toMarkdown(file);

        assertTrue(markdown.contains("| 5 | sum |"), markdown);
    }

    @Test
    void toMarkdownShouldPreserveEmptyCellsInsideUsedRange() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "gaps.xlsx", workbook ->
        {
            Sheet sheet = workbook.createSheet("Gaps");
            DocumentFixtures.appendTextRow(sheet, 0, "A", "B", "C");
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("left");
            row.createCell(2).setCellValue("right");
        });

        String expected = "# Gaps\n\n"
            + "| A | B | C |\n"
            + "| --- | --- | --- |\n"
            + "| left |  | right |\n\n";

        assertEquals(expected, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldEscapeSpecialCharactersInCells() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "escaped.xlsx", workbook ->
        {
            Sheet sheet = workbook.createSheet("Escape");
            DocumentFixtures.appendTextRow(sheet, 0, "a|b", "c\\d", "e\nf");
        });

        String expected = "# Escape\n\n"
            + "| a\\|b | c\\\\d | e\\nf |\n"
            + "| --- | --- | --- |\n\n";

        assertEquals(expected, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldSkipLeadingAndTrailingEmptyRows() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "offset.xlsx", workbook ->
        {
            Sheet sheet = workbook.createSheet("Offset");
            DocumentFixtures.appendTextRow(sheet, 2, "H1", "H2");
            DocumentFixtures.appendTextRow(sheet, 3, "v1", "v2");
        });

        String expected = "# Offset\n\n"
            + "| H1 | H2 |\n"
            + "| --- | --- |\n"
            + "| v1 | v2 |\n\n";

        assertEquals(expected, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldWriteOnlyHeadingForEmptySheet() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "empty-sheet.xlsx", workbook ->
        {
            workbook.createSheet("Nothing");
        });

        assertEquals("# Nothing\n\n", XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldFailForMissingFile()
    {
        Path missing = tempDir.resolve("missing.xlsx");

        assertThrows(IOException.class, () -> XlsxMarkdownConverter.toMarkdown(missing));
    }

    @Test
    void writeShouldCreateSheetFromHeading() throws IOException
    {
        Path file = tempDir.resolve("sheet-out.xlsx");

        XlsxMarkdownConverter.write(file, "# Stock\n\n| Name | Qty |\n| --- | --- |\n| Bolt | 12 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(1, workbook.getNumberOfSheets());
            Sheet sheet = workbook.getSheet("Stock");
            assertEquals("Name", DocumentFixtures.formattedValue(sheet, 0, 0));
            assertEquals("Qty", DocumentFixtures.formattedValue(sheet, 0, 1));
            assertEquals("Bolt", DocumentFixtures.formattedValue(sheet, 1, 0));
            assertEquals("12", DocumentFixtures.formattedValue(sheet, 1, 1));
        }
    }

    @Test
    void writeShouldCreateMultipleSheets() throws IOException
    {
        Path file = tempDir.resolve("sheets-out.xlsx");

        String markdown = "# First\n\n| A |\n| --- |\n| 1 |\n\n"
            + "# Second\n\n| B |\n| --- |\n| 2 |\n";

        XlsxMarkdownConverter.write(file, markdown);

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(2, workbook.getNumberOfSheets());
            assertEquals("First", workbook.getSheetAt(0).getSheetName());
            assertEquals("Second", workbook.getSheetAt(1).getSheetName());
            assertEquals("1", DocumentFixtures.formattedValue(workbook.getSheetAt(0), 1, 0));
            assertEquals("2", DocumentFixtures.formattedValue(workbook.getSheetAt(1), 1, 0));
        }
    }

    @Test
    void writeShouldSkipSeparatorRow() throws IOException
    {
        Path file = tempDir.resolve("separator-out.xlsx");

        XlsxMarkdownConverter.write(file, "| A | B |\n| --- | --- |\n| 1 | 2 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(2, sheet.getLastRowNum() + 1);
            assertEquals("1", DocumentFixtures.formattedValue(sheet, 1, 0));
        }
    }

    @Test
    void writeShouldInferBooleanCells() throws IOException
    {
        Path file = tempDir.resolve("booleans.xlsx");

        XlsxMarkdownConverter.write(file, "| Flag |\n| --- |\n| true |\n| FALSE |\n| True |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(CellType.BOOLEAN, sheet.getRow(1).getCell(0).getCellType());
            assertTrue(sheet.getRow(1).getCell(0).getBooleanCellValue());
            assertEquals(CellType.BOOLEAN, sheet.getRow(2).getCell(0).getCellType());
            assertEquals(false, sheet.getRow(2).getCell(0).getBooleanCellValue());
            assertEquals(CellType.BOOLEAN, sheet.getRow(3).getCell(0).getCellType());
        }
    }

    @Test
    void writeShouldInferNumericCells() throws IOException
    {
        Path file = tempDir.resolve("numbers.xlsx");

        XlsxMarkdownConverter.write(file, "| Value |\n| --- |\n| 42 |\n| -7 |\n| 3.14 |\n| 1e3 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(CellType.NUMERIC, sheet.getRow(1).getCell(0).getCellType());
            assertEquals(42.0, sheet.getRow(1).getCell(0).getNumericCellValue(), 0.0001);
            assertEquals(-7.0, sheet.getRow(2).getCell(0).getNumericCellValue(), 0.0001);
            assertEquals(3.14, sheet.getRow(3).getCell(0).getNumericCellValue(), 0.0001);
            assertEquals(1000.0, sheet.getRow(4).getCell(0).getNumericCellValue(), 0.0001);
        }
    }

    @Test
    void writeShouldKeepLeadingZeroValuesAsText() throws IOException
    {
        Path file = tempDir.resolve("leading-zero.xlsx");

        XlsxMarkdownConverter.write(file, "| Code |\n| --- |\n| 007 |\n| 0755 |\n| 0 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(CellType.STRING, sheet.getRow(1).getCell(0).getCellType());
            assertEquals("007", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("0755", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals(CellType.NUMERIC, sheet.getRow(3).getCell(0).getCellType());
        }
    }

    @Test
    void writeShouldKeepNonNumericTextAsText() throws IOException
    {
        Path file = tempDir.resolve("text.xlsx");

        XlsxMarkdownConverter.write(file, "| Value |\n| --- |\n| abc |\n| 12abc |\n| NaN |\n| Infinity |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("abc", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("12abc", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("NaN", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals("Infinity", sheet.getRow(4).getCell(0).getStringCellValue());
        }
    }

    @Test
    void writeShouldLeaveEmptyCellsBlank() throws IOException
    {
        Path file = tempDir.resolve("blanks.xlsx");

        XlsxMarkdownConverter.write(file, "| A | B |\n| --- | --- |\n|  | x |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            Row row = sheet.getRow(1);
            Cell first = row.getCell(0);
            assertTrue(first == null || first.getCellType() == CellType.BLANK);
            assertEquals("x", row.getCell(1).getStringCellValue());
        }
    }

    @Test
    void writeShouldUnescapeCellValues() throws IOException
    {
        Path file = tempDir.resolve("unescape.xlsx");

        XlsxMarkdownConverter.write(file, "| a\\|b | c\\\\d | e\\nf |\n| --- | --- | --- |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("a|b", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("c\\d", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("e\nf", sheet.getRow(0).getCell(2).getStringCellValue());
        }
    }

    @Test
    void writeShouldCreateDefaultSheetWhenHeadingIsMissing() throws IOException
    {
        Path file = tempDir.resolve("default-sheet.xlsx");

        XlsxMarkdownConverter.write(file, "| A |\n| --- |\n| 1 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("Sheet1", workbook.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void writeShouldCreateDefaultSheetForEmptyHeading() throws IOException
    {
        Path file = tempDir.resolve("empty-heading.xlsx");

        XlsxMarkdownConverter.write(file, "#\n\n| A |\n| --- |\n| 1 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals("Sheet1", workbook.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void writeShouldCreateEmptyWorkbookForEmptyMarkdown() throws IOException
    {
        Path file = tempDir.resolve("empty-workbook.xlsx");

        XlsxMarkdownConverter.write(file, "");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(1, workbook.getNumberOfSheets());
            assertEquals("Sheet1", workbook.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void writeShouldSanitizeIllegalSheetNameCharacters() throws IOException
    {
        Path file = tempDir.resolve("illegal-name.xlsx");

        XlsxMarkdownConverter.write(file, "# Q1/2025: [Draft]*?\n\n| A |\n| --- |\n| 1 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            String name = workbook.getSheetAt(0).getSheetName();
            assertEquals("Q1 2025   Draft", name);
        }
    }

    @Test
    void writeShouldTruncateLongSheetNameToThirtyOneCharacters() throws IOException
    {
        Path file = tempDir.resolve("long-name.xlsx");

        XlsxMarkdownConverter.write(file, "# " + "A".repeat(60) + "\n\n| A |\n| --- |\n| 1 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(31, workbook.getSheetAt(0).getSheetName().length());
        }
    }

    @Test
    void writeShouldDeduplicateSheetNames() throws IOException
    {
        Path file = tempDir.resolve("duplicate-names.xlsx");

        String markdown = "# Data\n\n| A |\n| --- |\n| 1 |\n\n"
            + "# Data\n\n| B |\n| --- |\n| 2 |\n\n"
            + "# Data\n\n| C |\n| --- |\n| 3 |\n";

        XlsxMarkdownConverter.write(file, markdown);

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals(3, workbook.getNumberOfSheets());
            assertEquals("Data", workbook.getSheetAt(0).getSheetName());
            assertEquals("Data (2)", workbook.getSheetAt(1).getSheetName());
            assertEquals("Data (3)", workbook.getSheetAt(2).getSheetName());
        }
    }

    @Test
    void writeShouldHandleCarriageReturnLineEndings() throws IOException
    {
        Path file = tempDir.resolve("crlf.xlsx");

        XlsxMarkdownConverter.write(file, "# Sheet\r\n\r\n| A |\r\n| --- |\r\n| 1 |\r\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("A", DocumentFixtures.formattedValue(sheet, 0, 0));
            assertEquals("1", DocumentFixtures.formattedValue(sheet, 1, 0));
        }
    }

    @Test
    void writeShouldOverwriteExistingFile() throws IOException
    {
        Path file = tempDir.resolve("overwrite.xlsx");
        Files.writeString(file, "not an xlsx file");

        XlsxMarkdownConverter.write(file, "# Fresh\n\n| A |\n| --- |\n| 1 |\n");

        try (XSSFWorkbook workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals("Fresh", workbook.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void writeThenReadShouldRoundTripWorkbook() throws IOException
    {
        Path file = tempDir.resolve("round-trip.xlsx");

        String markdown = "# Stock\n\n"
            + "| Name | Qty |\n"
            + "| --- | --- |\n"
            + "| Bolt | 12 |\n\n";

        XlsxMarkdownConverter.write(file, markdown);

        assertEquals(markdown, XlsxMarkdownConverter.toMarkdown(file));
    }

    @Test
    void writeThenReadShouldRoundTripSpecialCharacters() throws IOException
    {
        Path file = tempDir.resolve("round-trip-escaped.xlsx");

        String markdown = "# Escape\n\n"
            + "| a\\|b | c\\\\d | e\\nf |\n"
            + "| --- | --- | --- |\n\n";

        XlsxMarkdownConverter.write(file, markdown);

        assertEquals(markdown, XlsxMarkdownConverter.toMarkdown(file));
    }
}
