package dev.kitorik.jai.paste;

import dev.kitorik.jai.config.JaiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Заливка текста на paste.rs (открытый API: POST body -> URL).
 * paste.rs поддерживает DELETE <url> — мод удаляет пасту через N секунд.
 */
public class PasteClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** Заливает текст, возвращает прямую ссылку и планирует удаление. */
    public static String upload(JaiConfig cfg, String content) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(cfg.pasteUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "text/plain; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(content))
                .build();

        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("paste.rs HTTP " + resp.statusCode());
        }

        String url = resp.body().trim();

        long delaySec = cfg.deleteAfterSeconds;
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(delaySec * 1000L);
                HttpRequest del = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .DELETE()
                        .build();
                HTTP.send(del, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {
            }
        });

        return url;
    }
}
