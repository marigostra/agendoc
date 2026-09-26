
package agendoc;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.apache.logging.log4j.*;

import static java.util.Objects.*;
import static java.nio.file.Files.*;

final class ConsoleApp
{
    static private final Logger log = LogManager.getLogger();
    
    void run()
    {
	final var tools = new AgentTools(Collections.emptyList(), s -> log.info(s));
	final var agent = new Agent(new SettingsStorage(), tools);
        try (Scanner scanner = new Scanner(System.in)) {
	    String userInput;
	    while (true)
	    {
		System.out.print("AGENDOC>");
		userInput = scanner.nextLine(); // Чтение строки, введенной пользователем
		if (userInput.equalsIgnoreCase("exit")) 
		    break;
		String response = agent.agent().chat(userInput);
		System.out.println(response);
	    }
	    	}
    }

    static public void main(String[] args) throws IOException
    {
	if (args.length == 1 && args[0].equalsIgnoreCase("--init"))
	{
	    final var st = new SettingsStorage();
	    final var sett = st.load();
	    st.save(sett);
	    System.out.println("Initial configuration saved");
	    return;
	}
	new ConsoleApp().run();
    }
}
