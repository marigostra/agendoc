package agendoc;

import java.util.List;
import java.util.function.Consumer;

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

    private final Consumer<String> trace;

    /**
     * Creates the tool set backed by the given documents.
     *
     * @param documents the live list of documents available to the agent
     * @param trace a callback invoked with a trace line describing each tool
     *        invocation, or {@code null} to disable tracing
     */
    AgentTools(List<DocumentRef> documents, Consumer<String> trace)
    {
        this.documents = documents;
        this.trace = trace;
    }

    /**
     * Provides the list of known documents. Each document is listed as its file name.
     *
     * @return the file names of all known documents
     */
    @Tool("Provides the list of known document. Each document is listed as its file name.")
    public List<String> listDocuments()
    {
        trace("Tool listDocuments: listing documents");
        final List<String> result = snapshot().stream()
            .map(DocumentRef::toString)
            .toList();
        trace("Tool listDocuments: returned " + result.size() + " document(s)");
        return result;
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
        trace("Tool readDocument: reading document " + documentName);
        final var ref = findDocument(documentName);
        if (ref == null)
        {
            trace("Tool readDocument: unknown document " + documentName);
            return "Unknown document: " + documentName;
        }
        try {
            final String markdown = OfficeMarkdown.toMarkdown(ref.getAbsolutePath());
            trace("Tool readDocument: read " + documentName + " successfully");
            return markdown;
        }
        catch (Exception e)
        {
            trace("Tool readDocument: failed to read " + documentName + ": " + e.getMessage());
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
        trace("Tool saveDocument: saving document " + documentName);
        final var ref = findDocument(documentName);
        if (ref == null)
        {
            trace("Tool saveDocument: unknown document " + documentName);
            return false;
        }
        try
        {
            OfficeMarkdown.write(ref.getAbsolutePath(), content);
            trace("Tool saveDocument: saved " + documentName + " successfully");
            return true;
        }
        catch (Exception e)
        {
            trace("Tool saveDocument: failed to save " + documentName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Emits a trace line via the configured callback, if any.
     *
     * @param line the trace line to emit
     */
    private void trace(String line)
    {
        if (trace != null)
            trace.accept(line);
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