
package agendoc;

import java.util.*;
import java.io.*;
import java.nio.file.*;

import static java.util.Objects.*;
import static java.nio.file.Files.*;

final class ConsoleChat
{
    void run()
    {
	final var tools = new AgentTools(Collections.emptyList(), s -> {});
	final var agent = new Agent(new SettingsStorage(), tools);
        try (Scanner scanner = new Scanner(System.in)) {
	    String userInput;
	    while (true)
	    {
		System.out.print("SG>");
		userInput = scanner.nextLine(); // Чтение строки, введенной пользователем
		if (userInput.equalsIgnoreCase("exit")) 
		    break;
		String response = agent.agent().chat(userInput);
		System.out.println(response);
	    }
	    	}
    }
}
