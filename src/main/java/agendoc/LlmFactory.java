package agendoc;

import java.net.http.HttpClient;
import java.time.Duration;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.http.client.jdk.JdkHttpClient;

/**
 * Factory for creating configured {@link OpenAiChatModel} instances
 * based on the application settings.
 */
public final class LlmFactory
{

    private LlmFactory()
    {
    }

    /**
     * Creates an {@link OpenAiChatModel} using the settings from the given storage.
     *
     * @param settingsStorage the settings storage to load configuration from
     * @return a configured OpenAiChatModel instance
     */
    public static OpenAiChatModel createModel(SettingsStorage settingsStorage)
    {
        final Settings settings = settingsStorage.load();
        return OpenAiChatModel.builder()
            .baseUrl(settings.getEndpoint())
            .apiKey(settings.getToken())
            .modelName(settings.getModel())
            .timeout(Duration.ofMillis(settings.getTimeout()))
            .temperature(settings.getTemperature())
            .httpClientBuilder(
                JdkHttpClient.builder()
                    .httpClientBuilder(
                        HttpClient.newBuilder()
                            .version(HttpClient.Version.HTTP_1_1)))
            .build();
    }
}