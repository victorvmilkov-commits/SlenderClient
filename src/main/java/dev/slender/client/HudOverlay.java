package dev.slender.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class HudOverlay {
    private HudOverlay() {}
    public static void register() {
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (!SlenderClient.hudEnabled) return;
            Module mod = SlenderClient.getModule("Coordinates HUD");
            if (mod == null || !mod.enabled()) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || client.world == null) return;

            double scale = mod.sliders().getOrDefault("HUD scale", 1.0);
            context.getMatrices().pushMatrix();
            context.getMatrices().scale((float)scale, (float)scale);
            int x = 8, y = 8;
            context.fill(x, y, x + 146, y + 46, 0xC90A0D16);
            context.fill(x, y, x + 2, y + 46, 0xFF9B6DFF);
            context.drawText(client.textRenderer, "SLENDER CLIENT", x + 9, y + 5, 0xFFBDA8FF, false);
            int line = y + 17;
            if (mod.toggles().getOrDefault("Show coordinates", true)) {
                BlockPos pos = client.player.getBlockPos();
                context.drawText(client.textRenderer, "XYZ  " + pos.getX() + "  " + pos.getY() + "  " + pos.getZ(), x + 9, line, 0xFFF4F3FF, false);
                line += 11;
            }
            if (mod.toggles().getOrDefault("Show FPS", true)) {
                context.drawText(client.textRenderer, "FPS  " + client.getCurrentFps(), x + 9, line, 0xFF9AA9C0, false);
            }
            context.getMatrices().popMatrix();
        });
    }
}
