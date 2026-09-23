package agendoc.docs;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Test fixtures that build and reopen office documents with Apache POI.
 *
 * <p>The fixtures keep the tests focused on the expected Markdown output and
 * on the expected document structure instead of on POI boilerplate.</p>
 */
final class DocumentFixtures
{

    private DocumentFixtures()
    {
    }

    /**
     * Creates a DOCX file filled by the given consumer.
     *
     * @param directory directory that receives the file
     * @param fileName name of the file to create
     * @param filler callback that populates the document
     * @return path to the created file
     * @throws IOException if the file cannot be written
     */
    static Path createDocx(Path directory, String fileName, Consumer<XWPFDocument> filler) throws IOException
    {
        Path file = directory.resolve(fileName);
        try (XWPFDocument document = new XWPFDocument())
        {
            filler.accept(document);
            try (OutputStream out = Files.newOutputStream(file))
            {
                document.write(out);
            }
        }
        return file;
    }

    /**
     * Opens an existing DOCX file for inspection.
     *
     * @param file path to the DOCX file
     * @return opened document, must be closed by the caller
     * @throws IOException if the file cannot be read
     */
    static XWPFDocument openDocx(Path file) throws IOException
    {
        return new XWPFDocument(Files.newInputStream(file));
    }

    /**
     * Creates an XLSX file filled by the given consumer.
     *
     * @param directory directory that receives the file
     * @param fileName name of the file to create
     * @param filler callback that populates the workbook
     * @return path to the created file
     * @throws IOException if the file cannot be written
     */
    static Path createXlsx(Path directory, String fileName, Consumer<XSSFWorkbook> filler) throws IOException
    {
        Path file = directory.resolve(fileName);
        try (XSSFWorkbook workbook = new XSSFWorkbook())
        {
            filler.accept(workbook);
            try (OutputStream out = Files.newOutputStream(file))
            {
                workbook.write(out);
            }
        }
        return file;
    }

    /**
     * Opens an existing XLSX file for inspection.
     *
     * @param file path to the XLSX file
     * @return opened workbook, must be closed by the caller
     * @throws IOException if the file cannot be read
     */
    static XSSFWorkbook openXlsx(Path file) throws IOException
    {
        return new XSSFWorkbook(Files.newInputStream(file));
    }

    /**
     * Appends a row of text cells to a sheet.
     *
     * @param sheet target sheet
     * @param rowIndex zero based row index
     * @param values cell values
     */
    static void appendTextRow(Sheet sheet, int rowIndex, String... values)
    {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < values.length; c++)
        {
            row.createCell(c).setCellValue(values[c]);
        }
    }

    /**
     * Appends a row of numeric cells to a sheet.
     *
     * @param sheet target sheet
     * @param rowIndex zero based row index
     * @param values cell values
     */
    static void appendNumericRow(Sheet sheet, int rowIndex, double... values)
    {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < values.length; c++)
        {
            row.createCell(c).setCellValue(values[c]);
        }
    }

    /**
     * Appends a row of boolean cells to a sheet.
     *
     * @param sheet target sheet
     * @param rowIndex zero based row index
     * @param values cell values
     */
    static void appendBooleanRow(Sheet sheet, int rowIndex, boolean... values)
    {
        Row row = sheet.createRow(rowIndex);
        for (int c = 0; c < values.length; c++)
        {
            row.createCell(c).setCellValue(values[c]);
        }
    }

    /**
     * Formats a cell value the same way the converter does.
     *
     * @param sheet sheet that owns the cell
     * @param rowIndex zero based row index
     * @param columnIndex zero based column index
     * @return formatted cell value, empty string when the cell is missing
     */
    static String formattedValue(Sheet sheet, int rowIndex, int columnIndex)
    {
        Row row = sheet.getRow(rowIndex);
        Cell cell = row == null ? null : row.getCell(columnIndex);
        return new DataFormatter(Locale.US).formatCellValue(cell);
    }
}
