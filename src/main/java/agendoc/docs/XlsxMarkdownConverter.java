package agendoc.docs;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Converts XLSX workbooks to and from Markdown.
 */
final class XlsxMarkdownConverter
{

    private XlsxMarkdownConverter()
    {
    }

    /**
     * Reads an XLSX workbook and returns its Markdown representation.
     *
     * @param documentPath path to the XLSX file
     * @return Markdown representation
     * @throws IOException if the file cannot be read
     */
    static String toMarkdown(Path documentPath) throws IOException
    {
        try (InputStream in = Files.newInputStream(documentPath);
             XSSFWorkbook workbook = new XSSFWorkbook(in))
        {
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            StringBuilder markdown = new StringBuilder();
            for (int s = 0; s < workbook.getNumberOfSheets(); s++)
            {
                appendSheet(markdown, workbook.getSheetAt(s), formatter, evaluator);
            }
            return markdown.toString();
        }
    }

    /**
     * Writes a Markdown representation to a new XLSX workbook.
     *
     * @param targetPath path to the XLSX file to create
     * @param markdown the Markdown representation
     * @throws IOException if the file cannot be written
     */
    static void write(Path targetPath, String markdown) throws IOException
    {
        String[] lines = markdown.split("\\r?\\n", -1);
        try (XSSFWorkbook workbook = new XSSFWorkbook())
        {
            Sheet currentSheet = null;
            for (String line : lines)
            {
                if (line.startsWith("#"))
                {
                    String sheetName = line.substring(1).trim();
                    if (sheetName.isEmpty())
                    {
                        sheetName = "Sheet" + (workbook.getNumberOfSheets() + 1);
                    }
                    currentSheet = workbook.createSheet(safeSheetName(workbook, sheetName));
                    continue;
                }

                if (MarkdownText.isTableRow(line))
                {
                    if (currentSheet == null)
                    {
                        currentSheet = workbook.createSheet("Sheet1");
                    }
                    List<String> cells = MarkdownText.splitTableRow(line);
                    if (!MarkdownText.isSeparatorRow(cells))
                    {
                        appendRow(currentSheet, cells);
                    }
                }
            }

            if (workbook.getNumberOfSheets() == 0)
            {
                workbook.createSheet("Sheet1");
            }

            try (OutputStream out = Files.newOutputStream(targetPath))
            {
                workbook.write(out);
            }
        }
    }

    private static void appendSheet(StringBuilder markdown, Sheet sheet,
                                    DataFormatter formatter, FormulaEvaluator evaluator)
    {
        markdown.append("# ").append(sheet.getSheetName()).append('\n').append('\n');

        int firstRow = sheet.getFirstRowNum();
        int lastRow = sheet.getLastRowNum();
        if (firstRow == -1 || lastRow < firstRow)
        {
            return;
        }

        int firstColumn = Integer.MAX_VALUE;
        int lastColumn = -1;
        for (int r = firstRow; r <= lastRow; r++)
        {
            Row row = sheet.getRow(r);
            if (row == null)
            {
                continue;
            }
            firstColumn = Math.min(firstColumn, row.getFirstCellNum());
            lastColumn = Math.max(lastColumn, row.getLastCellNum() - 1);
        }

        if (firstColumn == Integer.MAX_VALUE || lastColumn < firstColumn)
        {
            return;
        }

        for (int r = firstRow; r <= lastRow; r++)
        {
            markdown.append('|');
            Row row = sheet.getRow(r);
            for (int c = firstColumn; c <= lastColumn; c++)
            {
                String value = "";
                if (row != null)
                {
                    Cell cell = row.getCell(c);
                    if (cell != null)
                    {
                        value = formatter.formatCellValue(cell, evaluator);
                    }
                }
                markdown.append(' ').append(MarkdownText.escapeCell(value)).append(" |");
            }
            markdown.append('\n');
            if (r == firstRow)
            {
                markdown.append('|');
                for (int c = firstColumn; c <= lastColumn; c++)
                {
                    markdown.append(" --- |");
                }
                markdown.append('\n');
            }
        }
        markdown.append('\n');
    }

    private static void appendRow(Sheet sheet, List<String> cells)
    {
        int rowIndex = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < cells.size(); c++)
        {
            String raw = MarkdownText.unescapeCell(cells.get(c).trim());
            setCellValue(row.createCell(c), raw);
        }
    }

    private static void setCellValue(Cell cell, String raw)
    {
        if (raw.isEmpty())
        {
            return;
        }

        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("true"))
        {
            cell.setCellValue(true);
            return;
        }
        if (trimmed.equalsIgnoreCase("false"))
        {
            cell.setCellValue(false);
            return;
        }
        if (trimmed.length() > 1 && trimmed.charAt(0) == '0'
            && trimmed.chars().allMatch(Character::isDigit))
        {
            cell.setCellValue(raw);
            return;
        }

        try
        {
            long longValue = Long.parseLong(trimmed);
            cell.setCellValue((double) longValue);
            return;
        }
        catch (NumberFormatException ignored)
        {
            // not an integer; try a floating point number below
        }

        try
        {
            double doubleValue = Double.parseDouble(trimmed);
            if (Double.isFinite(doubleValue))
            {
                cell.setCellValue(doubleValue);
                return;
            }
        }
        catch (NumberFormatException ignored)
        {
            // not numeric; store as text below
        }

        cell.setCellValue(raw);
    }

    private static String safeSheetName(XSSFWorkbook workbook, String requestedName)
    {
        String cleaned = requestedName.replaceAll("[\\\\/?*\\[\\]:]", " ").trim();
        if (cleaned.isEmpty())
        {
            cleaned = "Sheet";
        }
        if (cleaned.length() > 31)
        {
            cleaned = cleaned.substring(0, 31);
        }

        String result = cleaned;
        int suffix = 2;
        while (workbook.getSheet(result) != null)
        {
            String suffixText = " (" + suffix + ")";
            int baseLength = Math.max(1, cleaned.length() - suffixText.length());
            result = cleaned.substring(0, Math.min(baseLength, cleaned.length())) + suffixText;
            if (result.length() > 31)
            {
                result = result.substring(0, 31);
            }
            suffix++;
        }
        return result;
    }
}
