package dev.kitorik.jai.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Простой JSON-конфиг: config/jaimod.json */
public class JaiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("jaimod.json");

    /** API-ключ OpenRouter (https://openrouter.ai/keys) */
    public String openRouterApiKey = "";
    /** Модель OpenRouter */
    public String model = "anthropic/claude-3.5-sonnet";
    /** URL OpenRouter API */
    public String openRouterUrl = "https://openrouter.ai/api/v1/chat/completions";
    /** URL pastebin-сервиса с POST + DELETE (paste.rs) */
    public String pasteUrl = "https://paste.rs";
    /** Через сколько секунд удалять pastebin (5 минут = 300) */
    public int deleteAfterSeconds = 300;
    /** Автоматически нажимать «ПРИНЯТЬ» в книге установки */
    public boolean autoAccept = true;
    /** Задержка перед автонажатием, в тиках */
    public int acceptDelayTicks = 20;
    /** Системный промпт (можно вставить выдержки из JAI.md) */
    public String systemPromptExtra = "Ты JAI — генератор JSON-модулей для Minecraft-сервера JustMC (мини-игра Creative+). "
            + "Отвечай ТОЛЬКО валидным JSON-модулем JustMC, без пояснений и без markdown-блоков. "
            + "Формат: {\"handlers\": [...], \"functions\": [...]} — обработчики событий (handler + conditional/operations) и функции с параметрами. "
            + "Действия: set_variable_value, if_variable_*, player_send_message, control_wait и т.д. "
            + "Переменные читаются только через %var/%var_local/%var_save. Лупы >10 итераций обязаны содержать control_wait.";

    public static JaiConfig load() {
        try {
            if (Files.exists(PATH)) {
                JaiConfig cfg = GSON.fromJson(Files.readString(PATH), JaiConfig.class);
                if (cfg != null) return cfg;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JaiConfig cfg = new JaiConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
