package dev.kitorik.jai;

import dev.kitorik.jai.config.JaiConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;

public class JaiMod implements ClientModInitializer {
    public static final String MOD_ID = "jaimod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static JaiConfig CONFIG;

    /** Очередь отложенных задач (в тиках) — для задержки перед автонажатием. */
    private record DelayedTask(int ticksLeft, Runnable action) {}
    private static final Queue<DelayedTask> TASKS = new ArrayDeque<>();

    @Override
    public void onInitializeClient() {
        CONFIG = JaiConfig.load();
        JaiCommand.register();
        BookAcceptHandler.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TASKS.isEmpty()) return;
            int size = TASKS.size();
            for (int i = 0; i < size; i++) {
                DelayedTask t = TASKS.poll();
                if (t.ticksLeft() <= 0) {
                    try {
                        t.action().run();
                    } catch (Exception e) {
                        LOGGER.error("JAI task failed", e);
                    }
                } else {
                    TASKS.add(new DelayedTask(t.ticksLeft() - 1, t.action()));
                }
            }
        });

        LOGGER.info("JAI Mod loaded (1.21.11)");
    }

    /** Выполнить действие через N тиков. */
    public static void schedule(int ticks, Runnable action) {
        TASKS.add(new DelayedTask(ticks, action));
    }
}
