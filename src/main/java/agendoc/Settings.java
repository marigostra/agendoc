package agendoc;

/**
 * Data model for OpenAI API access settings.
 */
public class Settings
{

    private String endpoint;
    private String token;
    private String model;
    private String project;
    private int timeout;
    private int toolCallLimit;
    private double temperature;

    /**
     * Creates settings with default values.
     */
    public Settings()
    {
        this.endpoint = "https://api.openai.com/v1";
        this.token = "";
        this.model = "gpt-4";
        this.project = "";
        this.timeout = 30000;
        this.toolCallLimit = 10;
        this.temperature = 0.7;
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
     * Returns the timeout in milliseconds.
     *
     * @return timeout in milliseconds
     */
    public int getTimeout()
    {
        return timeout;
    }

    /**
     * Sets the timeout in milliseconds.
     *
     * @param timeout timeout in milliseconds
     */
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
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