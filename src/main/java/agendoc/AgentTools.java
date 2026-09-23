package agendoc;

import java.util.List;

import agendoc.docs.OfficeMarkdown;

import dev.langchain4j.agent.tool.Tool;

/**
 * Tool implementations exposed to the language model for working with the
 * documents attached to the workspace.
 *
 * <p>Each tool operates on the live set of documents supplied at construction
 * time. Documents are identified by their file name (with extension).</p>
 */
public final class AgentTools
{

    private final List<DocumentRef> documents;

    /**
     * Creates the tool set backed by the given documents.
     *
     * @param documents the live list of documents available to the agent
     */
    AgentTools(List<DocumentRef> documents)
    {
        this.documents = documents;
    }

    /**
     * Provides the list of known documents. Each document is listed as its file name.
     *
     * @return the file names of all known documents
     */
    @Tool("Provides the list of known document. Each document is listed as its file name.")
    public List<String> listDocuments()
    {
        return snapshot().stream()
            .map(DocumentRef::toString)
            .toList();
    }

    /**
     * Reads the requested document and provides its content in markdown format.
     * Only known documents can be requested to be read.
     *
     * @param documentName the file name of the document to read
     * @return the document content in markdown format, or an error message if the
     *         document is unknown or cannot be read
     */
    @Tool("Reads the requested document and provides its content in markdown format. Only known documents can be requested to be read.")
    public String readDocument(String documentName)
    {
        final var ref = findDocument(documentName);
        if (ref == null)
            return "Unknown document: " + documentName;
        try {
            return OfficeMarkdown.toMarkdown(ref.getAbsolutePath());
        }
        catch (Exception e)
        {
            return "Failed to read document " + documentName + ": " + e.getMessage();
        }
    }

    /**
     * Saves the content of the document given by its name. The content must be
     * given in markdown format. The name of the document must be present in the
     * list of known documents. If an unknown document was provided, this method
     * returns false.
     *
     * @param documentName the file name of the document to save
     * @param content the markdown content to write
     * @return true if the document was saved successfully, false otherwise
     */
    @Tool("Saves the content of the document given by its name. The content must be given in markdown format. The name of the document must present in the list of known documents. If the unknown document was provided, this function returns false.")
    public boolean saveDocument(String documentName, String content)
    {
        final var ref = findDocument(documentName);
        if (ref == null)
            return false;
        try
        {
            OfficeMarkdown.write(ref.getAbsolutePath(), content);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Returns a thread-safe snapshot of the current documents.
     *
     * @return an immutable copy of the current document list
     */
    private List<DocumentRef> snapshot()
    {
        synchronized (documents)
        {
            return List.copyOf(documents);
        }
    }

    private DocumentRef findDocument(String documentName)
    {
        for (DocumentRef ref : snapshot())
        {
            if (ref.toString().equals(documentName))
                return ref;
        }
        return null;
    }
}
