package agendoc;

import java.nio.file.Path;

/**
 * Represents a reference to a document file by its absolute path.
 * The display name is the file name with extension.
 */
public class DocumentRef
{

    private final Path absolutePath;

    /**
     * Creates a document reference from the given absolute path.
     *
     * @param absolutePath absolute path to the document file
     */
    public DocumentRef(Path absolutePath)
    {
        this.absolutePath = absolutePath;
    }

    /**
     * Returns the absolute path to the document file.
     *
     * @return absolute path
     */
    public Path getAbsolutePath()
    {
        return absolutePath;
    }

    /**
     * Returns the file name with extension for display purposes.
     *
     * @return file name
     */
    @Override
    public String toString()
    {
        return absolutePath.getFileName().toString();
    }
}