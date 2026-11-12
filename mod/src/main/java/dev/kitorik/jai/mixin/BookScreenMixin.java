package dev.kitorik.jai.mixin;

import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.text.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Доступ к внутренностям BookScreen (Yarn 1.21.11). */
@Mixin(BookScreen.class)
public interface BookScreenMixin {
    @Accessor("contents")
    BookScreen.Contents jai$getContents();

    @Accessor("pageIndex")
    int jai$getPageIndex();

    @Invoker("handleClickEvent")
    boolean jai$handleClickEvent(ClickEvent event);
}
