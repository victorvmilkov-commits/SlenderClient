package dev.slender.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SlenderClient implements ClientModInitializer {
    public static final Map<String, Module> MODULES = new LinkedHashMap<>();
    public static boolean hudEnabled = true;
    private static KeyBinding menuKey;
    private static KeyBinding hudKey;

    @Override
    public void onInitializeClient() {
        MODULES.put("Coordinates HUD", new Module("Coordinates HUD", GLFW.GLFW_KEY_UNKNOWN, true)
                .toggleSetting("Show coordinates", true)
                .toggleSetting("Show FPS", true)
                .slider("HUD scale", 1.0));
        MODULES.put("Player Watchlist", new Module("Player Watchlist", GLFW.GLFW_KEY_UNKNOWN, true)
                .toggleSetting("Highlight watched names", true));
        // The remaining modules have configurable UI entries but their gameplay
        // logic is intentionally not implemented in this starter.
        MODULES.put("Freecam", new Module("Freecam", GLFW.GLFW_KEY_UNKNOWN, false)
                .slider("Camera speed", 1.0)
                .toggleSetting("Freeze player", true));
        MODULES.put("Storage ESP", new Module("Storage ESP", GLFW.GLFW_KEY_UNKNOWN, false)
                .toggleSetting("Chests", true)
                .toggleSetting("Barrels", true)
                .toggleSetting("Shulkers", true)
                .slider("Render distance", 32.0));
        MODULES.put("Block ESP", new Module("Block ESP", GLFW.GLFW_KEY_UNKNOWN, false)
                .toggleSetting("Ores", true)
                .toggleSetting("Containers", false)
                .slider("Render distance", 24.0));
        MODULES.put("Anti Vanish", new Module("Anti Vanish", GLFW.GLFW_KEY_UNKNOWN, false)
                .toggleSetting("Notify on tab changes", true));
        MODULES.put("Sus Chunk Finder", new Module("Sus Chunk Finder", GLFW.GLFW_KEY_UNKNOWN, false)
                .slider("Scan radius", 8.0)
                .toggleSetting("Mark unusual chunk patterns", true));

        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("slenderclient", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.slenderclient.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        hudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.slenderclient.hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) client.setScreen(new ClickGuiScreen());
            while (hudKey.wasPressed()) hudEnabled = !hudEnabled;

            if (client.currentScreen == null) {
                for (Module module : MODULES.values()) {
                    if (module.keyCode() != GLFW.GLFW_KEY_UNKNOWN
                            && InputUtil.isKeyPressed(client.getWindow(), module.keyCode())) {
                        // Keybind toggling uses edge detection below to avoid repeated toggles.
                        if (KeyEdges.justPressed(module.keyCode())) module.toggle();
                    }
                }
            }
            KeyEdges.releaseUnpressed(client);
        });
        HudOverlay.register();
    }

    public static Module getModule(String name) { return MODULES.get(name); }

    private static final class KeyEdges {
        private static final java.util.Set<Integer> down = new java.util.HashSet<>();
        static boolean justPressed(int key) { return down.add(key); }
        static void releaseUnpressed(net.minecraft.client.MinecraftClient client) {
            down.removeIf(key -> !InputUtil.isKeyPressed(client.getWindow(), key));
        }
    }
}
