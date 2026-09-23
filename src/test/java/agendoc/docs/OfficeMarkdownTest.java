package agendoc.docs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link OfficeMarkdown}.
 */
class OfficeMarkdownTest
{

    @TempDir
    Path tempDir;

    @Test
    void toMarkdownShouldDispatchDocxByExtension() throws IOException
    {
        Path file = DocumentFixtures.createDocx(tempDir, "report.docx", document ->
        {
            document.createParagraph().createRun().setText("Docx body");
        });

        assertEquals("Docx body\n\n", OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldDispatchXlsxByExtension() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "report.xlsx", workbook ->
        {
            DocumentFixtures.appendTextRow(workbook.createSheet("Data"), 0, "A", "B");
        });

        String expected = "# Data\n\n"
            + "| A | B |\n"
            + "| --- | --- |\n\n";

        assertEquals(expected, OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldAcceptUpperCaseExtension() throws IOException
    {
        Path file = DocumentFixtures.createDocx(tempDir, "REPORT.DOCX", document ->
        {
            document.createParagraph().createRun().setText("Upper case");
        });

        assertEquals("Upper case\n\n", OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldAcceptMixedCaseExtension() throws IOException
    {
        Path file = DocumentFixtures.createXlsx(tempDir, "Report.XlSx", workbook ->
        {
            DocumentFixtures.appendTextRow(workbook.createSheet("Mixed"), 0, "A");
        });

        assertEquals("# Mixed\n\n| A |\n| --- |\n\n", OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldRejectUnsupportedExtension()
    {
        Path file = tempDir.resolve("notes.pdf");

        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class, () -> OfficeMarkdown.toMarkdown(file));

        assertTrue(failure.getMessage().contains("Unsupported document type"), failure.getMessage());
    }

    @Test
    void toMarkdownShouldRejectFileWithoutExtension()
    {
        Path file = tempDir.resolve("document");

        assertThrows(IllegalArgumentException.class, () -> OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldRejectDocExtension()
    {
        Path file = tempDir.resolve("legacy.doc");

        assertThrows(IllegalArgumentException.class, () -> OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldFailForMissingFile()
    {
        Path missing = tempDir.resolve("missing.docx");

        assertThrows(IOException.class, () -> OfficeMarkdown.toMarkdown(missing));
    }

    @Test
    void writeShouldDispatchDocxByExtension() throws IOException
    {
        Path file = tempDir.resolve("out.docx");

        OfficeMarkdown.write(file, "# Title\n\nBody\n");

        try (var document = DocumentFixtures.openDocx(file))
        {
            assertEquals("Title", document.getParagraphs().get(0).getText());
            assertEquals("Body", document.getParagraphs().get(1).getText());
        }
    }

    @Test
    void writeShouldDispatchXlsxByExtension() throws IOException
    {
        Path file = tempDir.resolve("out.xlsx");

        OfficeMarkdown.write(file, "# Data\n\n| A |\n| --- |\n| 1 |\n");

        try (var workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals("Data", workbook.getSheetAt(0).getSheetName());
            assertEquals("1", DocumentFixtures.formattedValue(workbook.getSheetAt(0), 1, 0));
        }
    }

    @Test
    void writeShouldAcceptUpperCaseExtension() throws IOException
    {
        Path file = tempDir.resolve("OUT.XLSX");

        OfficeMarkdown.write(file, "# Upper\n\n| A |\n| --- |\n| 1 |\n");

        try (var workbook = DocumentFixtures.openXlsx(file))
        {
            assertEquals("Upper", workbook.getSheetAt(0).getSheetName());
        }
    }

    @Test
    void writeShouldRejectUnsupportedExtension()
    {
        Path file = tempDir.resolve("out.pdf");

        IllegalArgumentException failure = assertThrows(
            IllegalArgumentException.class, () -> OfficeMarkdown.write(file, "text"));

        assertTrue(failure.getMessage().contains("Unsupported document type"), failure.getMessage());
    }

    @Test
    void writeShouldRejectFileWithoutExtension()
    {
        Path file = tempDir.resolve("output");

        assertThrows(IllegalArgumentException.class, () -> OfficeMarkdown.write(file, "text"));
    }

    @Test
    void writeShouldOverwriteExistingFile() throws IOException
    {
        Path file = tempDir.resolve("overwrite.docx");
        Files.writeString(file, "stale content");

        OfficeMarkdown.write(file, "fresh content\n");

        try (var document = DocumentFixtures.openDocx(file))
        {
            assertEquals("fresh content", document.getParagraphs().get(0).getText());
        }
    }

    @Test
    void writeThenReadShouldRoundTripDocx() throws IOException
    {
        Path file = tempDir.resolve("round-trip.docx");

        String markdown = "# Title\n\n"
            + "Paragraph text\n\n"
            + "| A | B |\n"
            + "| --- | --- |\n"
            + "| 1 | 2 |\n\n";

        OfficeMarkdown.write(file, markdown);

        assertEquals(markdown, OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void writeThenReadShouldRoundTripXlsx() throws IOException
    {
        Path file = tempDir.resolve("round-trip.xlsx");

        String markdown = "# Data\n\n"
            + "| A | B |\n"
            + "| --- | --- |\n"
            + "| 1 | 2 |\n\n";

        OfficeMarkdown.write(file, markdown);

        assertEquals(markdown, OfficeMarkdown.toMarkdown(file));
    }

    @Test
    void toMarkdownShouldReadFileFromNestedDirectory() throws IOException
    {
        Path nested = Files.createDirectories(tempDir.resolve("nested").resolve("deeper"));
        Path file = DocumentFixtures.createDocx(nested, "deep.docx", document ->
        {
            document.createParagraph().createRun().setText("Nested body");
        });

        assertEquals("Nested body\n\n", OfficeMarkdown.toMarkdown(file));
    }
}
