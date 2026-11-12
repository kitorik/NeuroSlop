package dev.kitorik.jai.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.kitorik.jai.config.JaiConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Запрос к OpenRouter (chat/completions). */
public class OpenRouterClient {
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    /** Отправляет запрос и возвращает текст ответа модели. */
    public static String complete(JaiConfig cfg, String userRequest) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.model);

        JsonArray messages = new JsonArray();

        JsonObject sys = new JsonObject();
        sys.addProperty("role", "system");
        sys.addProperty("content", cfg.systemPromptExtra);
        messages.add(sys);

        JsonObject usr = new JsonObject();
        usr.addProperty("role", "user");
        usr.addProperty("content", userRequest);
        messages.add(usr);

        body.add("messages", messages);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(cfg.openRouterUrl))
                .timeout(Duration.ofMinutes(3))
                .header("Authorization", "Bearer " + cfg.openRouterApiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "https://github.com/kitorik")
                .header("X-Title", "JAI Mod")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("OpenRouter HTTP " + resp.statusCode() + ": "
                    + resp.body().substring(0, Math.min(500, resp.body().length())));
        }

        JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
        return json.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
    }
}
