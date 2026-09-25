package agendoc;

/**
 * Data model for OpenAI API access settings.
 */
public class Settings
{

    /**
     * Default API endpoint URL.
     */
    public static final String DEFAULT_ENDPOINT = "https://openai.bothub.chat/v1";

    /**
     * Default access token. Empty by default.
     */
    public static final String DEFAULT_TOKEN = "";

    /**
     * Default model name.
     */
    public static final String DEFAULT_MODEL = "deepsekk-v4.1-flash";

    /**
     * Default project name. Empty by default.
     */
    public static final String DEFAULT_PROJECT = "";

    /**
     * Default timeout in seconds.
     */
    public static final int DEFAULT_TIMEOUT_SEC = 300;

    /**
     * Default tool call limit.
     */
    public static final int DEFAULT_TOOL_CALL_LIMIT = 100;

    /**
     * Default temperature value.
     */
    public static final double DEFAULT_TEMPERATURE = 0.7;

    private String endpoint;
    private String token;
    private String model;
    private String project;
    private int timeoutSec;
    private int toolCallLimit;
    private double temperature;

    /**
     * Creates settings with default values.
     */
    public Settings()
    {
        this.endpoint = DEFAULT_ENDPOINT;
        this.token = DEFAULT_TOKEN;
        this.model = DEFAULT_MODEL;
        this.project = DEFAULT_PROJECT;
        this.timeoutSec = DEFAULT_TIMEOUT_SEC;
        this.toolCallLimit = DEFAULT_TOOL_CALL_LIMIT;
        this.temperature = DEFAULT_TEMPERATURE;
    }

    /**
     * Returns the API endpoint URL.
     *
     * @return endpoint URL
     */
    public String getEndpoint()
    {
        return endpoint;
    }

    /**
     * Sets the API endpoint URL.
     *
     * @param endpoint endpoint URL
     */
    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    /**
     * Returns the access token.
     *
     * @return access token
     */
    public String getToken()
    {
        return token;
    }

    /**
     * Sets the access token.
     *
     * @param token access token
     */
    public void setToken(String token)
    {
        this.token = token;
    }

    /**
     * Returns the model name.
     *
     * @return model name
     */
    public String getModel()
    {
        return model;
    }

    /**
     * Sets the model name.
     *
     * @param model model name
     */
    public void setModel(String model)
    {
        this.model = model;
    }

    /**
     * Returns the project name.
     *
     * @return project name
     */
    public String getProject()
    {
        return project;
    }

    /**
     * Sets the project name.
     *
     * @param project project name
     */
    public void setProject(String project)
    {
        this.project = project;
    }

    /**
     * Returns the timeout in seconds.
     *
     * @return timeout in seconds
     */
    public int getTimeoutSec()
    {
        return timeoutSec;
    }

    /**
     * Sets the timeout in seconds.
     *
     * @param timeout timeout in seconds
     */
    public void setTimeoutSec(int timeoutSec)
    {
        this.timeoutSec = timeoutSec;
    }

    /**
     * Returns the tool call limit.
     *
     * @return tool call limit
     */
    public int getToolCallLimit()
    {
        return toolCallLimit;
    }

    /**
     * Sets the tool call limit.
     *
     * @param toolCallLimit tool call limit
     */
    public void setToolCallLimit(int toolCallLimit)
    {
        this.toolCallLimit = toolCallLimit;
    }

    /**
     * Returns the temperature value.
     *
     * @return temperature
     */
    public double getTemperature()
    {
        return temperature;
    }

    /**
     * Sets the temperature value.
     *
     * @param temperature temperature
     */
    public void setTemperature(double temperature)
    {
        this.temperature = temperature;
    }
}
