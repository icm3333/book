package rcm.book.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Builds the {@link RestClient} used to reach Open Library.
 *
 * <p>Kept separate from {@link OpenLibraryClient} so the client itself only holds request
 * logic and can be unit tested against a stubbed transport. Explicit timeouts prevent a slow
 * upstream from tying up request threads indefinitely.
 */
@Configuration
public class OpenLibraryClientConfig {

    @Bean
    public RestClient openLibraryRestClient(
            @Value("${openlibrary.base-url}") String baseUrl,
            @Value("${openlibrary.user-agent}") String userAgent,
            @Value("${openlibrary.connect-timeout}") Duration connectTimeout,
            @Value("${openlibrary.read-timeout}") Duration readTimeout){

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
                .requestFactory(requestFactory)
                .build();
    }
}
