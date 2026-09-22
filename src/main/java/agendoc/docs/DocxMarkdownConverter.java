package agendoc.docs;

import org.apache.poi.xwpf.usermodel.BodyElementType;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Converts DOCX documents to and from Markdown.
 */
final class DocxMarkdownConverter
{

    private DocxMarkdownConverter()
    {
    }

    /**
     * Reads a DOCX document and returns its Markdown representation.
     *
     * @param documentPath path to the DOCX file
     * @return Markdown representation
     * @throws IOException if the file cannot be read
     */
    static String toMarkdown(Path documentPath) throws IOException
    {
        try (InputStream in = Files.newInputStream(documentPath);
             XWPFDocument document = new XWPFDocument(in))
        {
            StringBuilder markdown = new StringBuilder();
            List<IBodyElement> elements = document.getBodyElements();
            for (IBodyElement element : elements)
            {
                if (element.getElementType() == BodyElementType.PARAGRAPH)
                {
                    appendParagraph(markdown, (XWPFParagraph) element);
                }
                else if (element.getElementType() == BodyElementType.TABLE)
                {
                    appendTable(markdown, (XWPFTable) element);
                }
            }
            return markdown.toString();
        }
    }

    /**
     * Writes a Markdown representation to a new DOCX document.
     *
     * @param targetPath path to the DOCX file to create
     * @param markdown the Markdown representation
     * @throws IOException if the file cannot be written
     */
    static void write(Path targetPath, String markdown) throws IOException
    {
        String[] lines = markdown.split("\\r?\\n", -1);
        try (XWPFDocument document = new XWPFDocument())
        {
            int index = 0;
            while (index < lines.length)
            {
                String line = lines[index];
                if (MarkdownText.isTableRow(line))
                {
                    int tableEnd = index;
                    while (tableEnd < lines.length && MarkdownText.isTableRow(lines[tableEnd]))
                    {
                        tableEnd++;
                    }
                    writeTable(document, lines, index, tableEnd);
                    index = tableEnd;
                    continue;
                }
                if (line.startsWith("#"))
                {
                    writeHeading(document, line);
                    index++;
                    continue;
                }
                if (line.isBlank())
                {
                    index++;
                    continue;
                }
                writeParagraph(document, line);
                index++;
            }

            try (OutputStream out = Files.newOutputStream(targetPath))
            {
                document.write(out);
            }
        }
    }

    private static void appendParagraph(StringBuilder markdown, XWPFParagraph paragraph)
    {
        String text = buildParagraphText(paragraph);
        if (text.isEmpty())
        {
            markdown.append('\n');
            return;
        }

        int headingLevel = headingLevel(paragraph);
        if (headingLevel > 0)
        {
            markdown.append("#".repeat(headingLevel)).append(' ').append(text).append('\n');
        }
        else
        {
            markdown.append(text).append('\n');
        }
        markdown.append('\n');
    }

    private static String buildParagraphText(XWPFParagraph paragraph)
    {
        StringBuilder builder = new StringBuilder();
        for (XWPFRun run : paragraph.getRuns())
        {
            String text = run.text();
            if (text == null || text.isEmpty())
            {
                continue;
            }
            boolean bold = run.isBold();
            boolean italic = run.isItalic();
            if (bold && italic)
            {
                builder.append("***").append(text).append("***");
            }
            else if (bold)
            {
                builder.append("**").append(text).append("**");
            }
            else if (italic)
            {
                builder.append('*').append(text).append('*');
            }
            else
            {
                builder.append(text);
            }
        }
        return builder.toString().trim();
    }

    private static int headingLevel(XWPFParagraph paragraph)
    {
        String style = paragraph.getStyleID();
        if (style == null)
        {
            return 0;
        }
        String lower = style.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("heading"))
        {
            return 0;
        }
        for (int i = "heading".length(); i < lower.length(); i++)
        {
            char c = lower.charAt(i);
            if (c >= '1' && c <= '6')
            {
                return c - '0';
            }
        }
        return 0;
    }

    private static void appendTable(StringBuilder markdown, XWPFTable table)
    {
        int rowCount = table.getNumberOfRows();
        if (rowCount == 0)
        {
            return;
        }

        int columnCount = 0;
        for (XWPFTableRow row : table.getRows())
        {
            columnCount = Math.max(columnCount, row.getTableCells().size());
        }
        if (columnCount == 0)
        {
            return;
        }

        boolean firstRow = true;
        for (XWPFTableRow row : table.getRows())
        {
            markdown.append('|');
            List<XWPFTableCell> cells = row.getTableCells();
            for (int c = 0; c < columnCount; c++)
            {
                String cellText = c < cells.size() ? cells.get(c).getText() : "";
                markdown.append(' ').append(MarkdownText.escapeCell(cellText)).append(" |");
            }
            markdown.append('\n');
            if (firstRow)
            {
                markdown.append('|');
                for (int c = 0; c < columnCount; c++)
                {
                    markdown.append(" --- |");
                }
                markdown.append('\n');
                firstRow = false;
            }
        }
        markdown.append('\n');
    }

    private static void writeHeading(XWPFDocument document, String line)
    {
        int level = 0;
        while (level < line.length() && line.charAt(level) == '#')
        {
            level++;
        }
        level = Math.min(Math.max(level, 1), 6);

        String text = line.substring(level).trim();
        if (text.isEmpty())
        {
            return;
        }

        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setStyle("Heading" + level);
        addInlineRuns(paragraph, text);
    }

    private static void writeParagraph(XWPFDocument document, String line)
    {
        XWPFParagraph paragraph = document.createParagraph();
        addInlineRuns(paragraph, line.trim());
    }

    private static void writeTable(XWPFDocument document, String[] lines, int start, int end)
    {
        List<List<String>> rows = new ArrayList<>();
        int columnCount = 0;
        for (int i = start; i < end; i++)
        {
            List<String> cells = MarkdownText.splitTableRow(lines[i]);
            if (MarkdownText.isSeparatorRow(cells))
            {
                continue;
            }
            List<String> trimmed = new ArrayList<>(cells.size());
            for (String cell : cells)
            {
                trimmed.add(MarkdownText.unescapeCell(cell.trim()));
            }
            rows.add(trimmed);
            columnCount = Math.max(columnCount, trimmed.size());
        }

        if (rows.isEmpty() || columnCount == 0)
        {
            return;
        }

        XWPFTable table = document.createTable(rows.size(), columnCount);
        for (int r = 0; r < rows.size(); r++)
        {
            XWPFTableRow row = table.getRow(r);
            List<String> cells = rows.get(r);
            for (int c = 0; c < columnCount; c++)
            {
                XWPFTableCell cell = row.getCell(c);
                cell.setText(c < cells.size() ? cells.get(c) : "");
            }
        }
    }

    private static void addInlineRuns(XWPFParagraph paragraph, String text)
    {
        StringBuilder plain = new StringBuilder();
        int index = 0;
        while (index < text.length())
        {
            if (text.startsWith("**", index))
            {
                int end = text.indexOf("**", index + 2);
                if (end == -1)
                {
                    plain.append(text.substring(index));
                    break;
                }
                flushPlain(paragraph, plain);
                addRun(paragraph, text.substring(index + 2, end), true, false);
                index = end + 2;
            }
            else if (text.charAt(index) == '*')
            {
                int end = text.indexOf('*', index + 1);
                if (end == -1)
                {
                    plain.append(text.substring(index));
                    break;
                }
                flushPlain(paragraph, plain);
                addRun(paragraph, text.substring(index + 1, end), false, true);
                index = end + 1;
            }
            else
            {
                plain.append(text.charAt(index));
                index++;
            }
        }
        flushPlain(paragraph, plain);
    }

    private static void flushPlain(XWPFParagraph paragraph, StringBuilder plain)
    {
        if (plain.length() > 0)
        {
            addRun(paragraph, plain.toString(), false, false);
            plain.setLength(0);
        }
    }

    private static void addRun(XWPFParagraph paragraph, String text, boolean bold, boolean italic)
    {
        if (text.isEmpty())
        {
            return;
        }
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        if (bold)
        {
            run.setBold(true);
        }
        if (italic)
        {
            run.setItalic(true);
        }
    }
}
