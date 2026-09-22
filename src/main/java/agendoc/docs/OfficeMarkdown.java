package agendoc.docs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Converts office documents to and from Markdown.
 *
 * <p>The document type is determined solely by the file name extension.
 * Only {@code .docx} and {@code .xlsx} files are supported.</p>
 */
public final class OfficeMarkdown
{

    private OfficeMarkdown()
    {
    }

    /**
     * Reads an office document and returns its Markdown representation.
     *
     * @param documentPath path to a {@code .docx} or {@code .xlsx} file
     * @return Markdown representation of the document
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if the file type is not supported
     */
    public static String toMarkdown(Path documentPath) throws IOException
    {
        String fileName = documentPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".docx"))
        {
            return DocxMarkdownConverter.toMarkdown(documentPath);
        }
        if (fileName.endsWith(".xlsx"))
        {
            return XlsxMarkdownConverter.toMarkdown(documentPath);
        }
        throw new IllegalArgumentException("Unsupported document type: " + documentPath.getFileName());
    }

    /**
     * Writes a Markdown representation to a new office document.
     *
     * <p>The target format is determined by the target file name extension.
     * An existing file at the target path is overwritten.</p>
     *
     * @param targetPath path to the {@code .docx} or {@code .xlsx} file to create
     * @param markdown the Markdown representation to write
     * @throws IOException if the file cannot be written
     * @throws IllegalArgumentException if the target file type is not supported
     */
    public static void write(Path targetPath, String markdown) throws IOException
    {
        String fileName = targetPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".docx"))
        {
            DocxMarkdownConverter.write(targetPath, markdown);
        }
        else if (fileName.endsWith(".xlsx"))
        {
            XlsxMarkdownConverter.write(targetPath, markdown);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported document type: " + targetPath.getFileName());
        }
    }
}
