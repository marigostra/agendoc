package agendoc;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

/**
 * Wraps a langchain4j AI service agent configured from application settings.
 */
public final class Agent
{

    /**
     * Chat interface for the project agent.
     */
    public interface ProjectAgent
    {
        /**
         * Sends a user message to the agent and returns the response.
         *
         * @param userMessage the message from the user
         * @return the agent's response
         */
        @SystemMessage({
            "Strictly follow any instructions. When you need to work with documents (list documents, read a document, write a document) use available tools."
        })
        String chat(String userMessage);
    }

    private final ChatMemory chatMemory;
    private final ProjectAgent agent;

    /**
     * Creates an Agent configured from the given settings storage.
     *
     * @param settingsStorage The settings storage to load configuration from
     * @param tools The tools object to be provided to the newly created LLM
     */
    Agent(SettingsStorage settingsStorage, AgentTools agentTools)
    {
        chatMemory = MessageWindowChatMemory.withMaxMessages(1000);
        agent = AiServices.builder(ProjectAgent.class)
            .chatModel(LlmFactory.createModel(settingsStorage))
            .chatMemory(chatMemory)
	.tools(agentTools)
            .build();
    }

    /**
     * Returns the underlying {@link ProjectAgent} instance.
     *
     * @return the project agent
     */
    ProjectAgent agent()
    {
        return agent;
    }
}
