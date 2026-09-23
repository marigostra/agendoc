
package agendoc;

import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;

import dev.langchain4j.agent.tool.*;
import dev.langchain4j.service.*;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.*;

final class AgentTools
{
    @Tool("Provides the list of known document. Each document is listed as it's file name.")
    public List<String> listDocuments()
    {
	return null;
    }

    @Tool("Reads the requested document and provides its content in markdown format. Only known documents can be requested to be read.")
    public String readDocument(String documentName)
    {
	return null;
    }

    @Tool("Saves the content of the document given by its name. The content must be given in markdown format. The name of the document must present in the list of known documents. If the unknown document was provided, this function returns false.")
    public boolean saveDocument(String documentName, String content)
    {
	return false;
    }
}
