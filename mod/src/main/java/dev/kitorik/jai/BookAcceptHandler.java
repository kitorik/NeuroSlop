package dev.kitorik.jai;

import dev.kitorik.jai.mixin.BookScreenMixin;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Когда открывается книга установки модуля (кнопки «ПРИНЯТЬ» / «ОТКАЗ»),
 * находит компонент «ПРИНЯТЬ» и выполняет его click-event — как будто игрок кликнул.
 */
public class BookAcceptHandler {
    private static final AtomicBoolean ARMED = new AtomicBoolean(false);

    /** Вызывается после отправки /module load-url — взводим автонажатие. */
    public static void arm() {
        ARMED.set(true);
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!ARMED.get() || client.currentScreen == null) return;
            if (!(client.currentScreen instanceof BookScreen book)) return;

            // Книга открылась — снимаем взвод и жмём с задержкой
            ARMED.set(false);
            int delay = Math.max(0, JaiMod.CONFIG.acceptDelayTicks);
            JaiMod.schedule(delay, () -> clickAccept(book));
        });
    }

    private static void clickAccept(BookScreen book) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != book) return; // экран уже сменился

        BookScreenMixin mixin = (BookScreenMixin) book;
        BookScreen.Contents contents = mixin.jai$getContents();
        if (contents == null) return;

        Text page = contents.getPage(mixin.jai$getPageIndex());
        if (page == null) return;

        if (!findAndClick(book, page)) {
            JaiMod.LOGGER.warn("[JAI] Кнопка «ПРИНЯТЬ» не найдена на странице книги");
        }
    }

    /** Рекурсивно ищет компонент с текстом «ПРИНЯТЬ» и выполняет его click-event. */
    private static boolean findAndClick(BookScreen book, Text component) {
        Style style = component.getStyle();
        if (style != null && style.getClickEvent() != null
                && component.getString().replace(" ", "").equalsIgnoreCase("ПРИНЯТЬ")) {
            return performClick(book, style);
        }
        for (Text sibling : component.getSiblings()) {
            if (findAndClick(book, sibling)) return true;
        }
        return false;
    }

    private static boolean performClick(BookScreen book, Style style) {
        ClickEvent event = style.getClickEvent();
        if (event == null) return false;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        // Самый надёжный путь — выполнить действие клика напрямую
        if (event.getAction() == ClickEvent.Action.RUN_COMMAND) {
            String cmd = event.getValue();
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            JaiMod.LOGGER.info("[JAI] Автонажатие «ПРИНЯТЬ»: выполняю /{}", cmd);
            mc.player.networkHandler.sendChatCommand(cmd);
            return true;
        }

        // Фолбэк: штатный обработчик клика BookScreen
        try {
            boolean ok = ((BookScreenMixin) (Object) book).jai$handleClickEvent(event);
            if (ok) return true;
        } catch (Throwable ignored) {
        }
        return false;
    }
}
