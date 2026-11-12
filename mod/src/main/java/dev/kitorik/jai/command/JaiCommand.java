package dev.kitorik.jai.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import dev.kitorik.jai.BookAcceptHandler;
import dev.kitorik.jai.JaiMod;
import dev.kitorik.jai.ai.OpenRouterClient;
import dev.kitorik.jai.paste.PasteClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Клиентская команда: /jai code <запрос>
 * 1. Отправляет запрос в OpenRouter.
 * 2. Заливает ответ на paste.rs.
 * 3. Отправляет за игрока: /module load-url <ссылка>.
 * 4. Книга установки откроется — BookAcceptHandler сам нажмёт «ПРИНЯТЬ».
 */
public final class JaiCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("jai")
                        .then(literal("code")
                                .then(argument("запрос", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String request = StringArgumentType.getString(ctx, "запрос");
                                            runAsync(ctx.getSource(), request);
                                            return 1;
                                        })))
        ));
    }

    private static void runAsync(FabricClientCommandSource source, String request) {
        feedback(source, "⏳ Отправляю запрос нейросети (" + JaiMod.CONFIG.model + ")...", Formatting.GRAY);

        Thread.ofVirtual().start(() -> {
            try {
                // 1. Нейросеть
                String code = OpenRouterClient.complete(JaiMod.CONFIG, request);
                feedback(source, "✅ Ответ получен (" + code.length() + " символов). Заливаю на pastebin...", Formatting.GREEN);

                // 2. Pastebin
                String url = PasteClient.upload(JaiMod.CONFIG, code);
                feedback(source, "📤 Залито: " + url + " (удалится через "
                        + JaiMod.CONFIG.deleteAfterSeconds + " с)", Formatting.GREEN);

                // 3. /module load-url <url> — от имени игрока
                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> {
                    if (client.player == null) return;
                    feedback(source, "📦 Отправляю /module load-url ...", Formatting.YELLOW);
                    client.player.networkHandler.sendChatCommand("module load-url " + url);
                    BookAcceptHandler.arm(); // взводим автонажатие «ПРИНЯТЬ»
                });
            } catch (Exception e) {
                JaiMod.LOGGER.error("JAI error", e);
                feedback(source, "❌ Ошибка: " + e.getMessage(), Formatting.RED);
            }
        });
    }

    private static void feedback(FabricClientCommandSource source, String msg, Formatting color) {
        source.sendFeedback(Text.literal("[JAI] ").formatted(Formatting.AQUA, Formatting.BOLD)
                .append(Text.literal(msg).formatted(color)));
    }
}
