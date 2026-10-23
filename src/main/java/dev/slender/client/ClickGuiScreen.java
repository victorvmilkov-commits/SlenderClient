
package dev.slender.client;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;
import java.util.Set;

public final class ClickGuiScreen extends Screen {
    private static final int BG = 0xF20A0D16;
    private static final int PANEL = 0xFF111827;
    private static final int ALT = 0xFF182235;
    private static final int BORDER = 0xFF2C3B53;
    private static final int ACCENT = 0xFF9B6DFF;
    private static final int TEXT = 0xFFF4F3FF;
    private static final int MUTED = 0xFF9AA9C0;

    private static final Set<String> STAFF_WATCHLIST =
            Set.of("replace_with_staff_name");

    private int left, top;
    private int widthPanel = 610, heightPanel = 380;
    private String selected = "Coordinates HUD";
    private boolean waitingForKey;

    public ClickGuiScreen() {
        super(Text.literal("Slender Client"));
    }

    @Override
    protected void init() {
        left = (width - widthPanel) / 2;
        top = (height - heightPanel) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        ctx.fill(0, 0, width, height, 0xA8050710);
        ctx.fill(left + 4, top + 5, left + widthPanel + 4,
                top + heightPanel + 5, 0x50000000);
        ctx.fill(left, top, left + widthPanel, top + heightPanel, BG);
        outline(ctx, left, top, widthPanel, heightPanel, BORDER);
        ctx.fill(left, top, left + widthPanel, top + 3, ACCENT);

        ctx.fill(left + 1, top + 4, left + widthPanel - 1,
                top + 61, PANEL);
        ctx.fill(left + 18, top + 17, left + 52, top + 51, ACCENT);

        ctx.drawText(textRenderer, "S", left + 29, top + 25, TEXT, true);
        ctx.drawText(textRenderer, "SLENDER CLIENT",
                left + 64, top + 18, TEXT, true);
        ctx.drawText(textRenderer, "FABRIC 1.21.11  •  MODULAR CLIENT",
                left + 64, top + 35, MUTED, false);
        ctx.drawText(textRenderer, "RIGHT SHIFT",
                left + widthPanel - 91, top + 23, MUTED, false);

        int listX = left + 18;
        int listY = top + 78;
        int listW = 214;

        ctx.drawText(textRenderer, "MODULES", listX, listY, TEXT, true);
        ctx.fill(listX, listY + 11, listX + listW, listY + 12, BORDER);

        int i = 0;
        for (Module module : SlenderClient.MODULES.values()) {
            int y = listY + 18 + i * 35;

            if (y + 30 > top + heightPanel - 43) {
                i++;
                continue;
            }

            boolean active = module.name().equals(selected);

            ctx.fill(listX, y, listX + listW, y + 29,
                    active ? 0xFF292541 : ALT);

            if (active) {
                ctx.fill(listX, y, listX + 3, y + 29, ACCENT);
            }

            ctx.drawText(textRenderer, module.name(),
                    listX + 10, y + 10,
                    module.enabled() ? TEXT : MUTED, false);

            int sx = listX + listW - 36;

            ctx.fill(sx, y + 7, sx + 27, y + 22,
                    module.enabled() ? 0xFF287A65 : 0xFF354158);

            ctx.fill(
                    module.enabled() ? sx + 14 : sx + 2,
                    y + 9,
                    module.enabled() ? sx + 25 : sx + 13,
                    y + 20,
                    TEXT
            );

            i++;
        }

        int rx = left + 251;
        int ry = listY;
        int rw = widthPanel - 269;

        ctx.drawText(textRenderer, "SETTINGS", rx, ry, TEXT, true);
        ctx.fill(rx, ry + 11, rx + rw, ry + 12, BORDER);

        Module module = SlenderClient.getModule(selected);

        if (module != null) {
            ctx.drawText(textRenderer, module.name(),
                    rx, ry + 22, ACCENT, true);

            ctx.drawText(textRenderer, "BIND",
                    rx, ry + 44, MUTED, false);

            String bind = waitingForKey
                    ? "PRESS A KEY..."
                    : keyName(module.keyCode());

            ctx.fill(rx + 45, ry + 39, rx + rw, ry + 58, ALT);
            ctx.drawText(textRenderer, bind,
                    rx + 52, ry + 45, TEXT, false);

            ctx.drawText(textRenderer,
                    "CLICK TO REBIND  •  BACKSPACE CLEARS",
                    rx, ry + 64, MUTED, false);

            int sy = ry + 92;

            for (var setting : module.toggles().entrySet()) {
                ctx.fill(rx, sy, rx + rw, sy + 25, ALT);

                ctx.drawText(textRenderer, setting.getKey(),
                        rx + 8, sy + 8, TEXT, false);

                int tx = rx + rw - 35;
                boolean on = setting.getValue();

                ctx.fill(tx, sy + 5, tx + 27, sy + 20,
                        on ? 0xFF287A65 : 0xFF354158);

                ctx.fill(
                        on ? tx + 14 : tx + 2,
                        sy + 7,
                        on ? tx + 25 : tx + 13,
                        sy + 18,
                        TEXT
                );

                sy += 30;
            }

            for (var setting : module.sliders().entrySet()) {
                String name = setting.getKey();

                ctx.drawText(textRenderer,
                        name + "  " + String.format(
                                Locale.ROOT, "%.1f", setting.getValue()),
                        rx, sy + 2, TEXT, false);

                int barY = sy + 18;

                ctx.fill(rx, barY, rx + rw, barY + 5, 0xFF303B50);

                double min = name.toLowerCase(Locale.ROOT)
                        .contains("scale") ? 0.5 : 4.0;

                double max = name.toLowerCase(Locale.ROOT)
                        .contains("scale") ? 2.0
                        : name.toLowerCase(Locale.ROOT)
                        .contains("radius") ? 16.0 : 64.0;

                double ratio = Math.max(0, Math.min(1,
                        (setting.getValue() - min) / (max - min)));

                ctx.fill(rx, barY,
                        rx + (int) (rw * ratio), barY + 5, ACCENT);

                sy += 34;
            }

            if (sy < top + heightPanel - 42) {
                ctx.drawText(textRenderer,
                        "Module features depend on implementation",
                        rx, sy + 5, MUTED, false);
            }
        }

        int fy = top + heightPanel - 27;

        ctx.fill(left + 18, fy - 7,
                left + widthPanel - 18, fy - 6, BORDER);

        ctx.drawText(textRenderer,
                "Click module to select  •  Click settings to change",
                left + 18, fy, MUTED, false);

        ctx.drawText(textRenderer, "ESC TO CLOSE",
                left + widthPanel - 99, fy, TEXT, false);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        if (button != 0) {
            return super.mouseClicked(click, doubled);
        }

        int listX = left + 18;
        int listY = top + 78;
        int listW = 214;
        int i = 0;

        for (Module module : SlenderClient.MODULES.values()) {
            int y = listY + 18 + i * 35;

            if (mx >= listX && mx <= listX + listW
                    && my >= y && my <= y + 29) {

                if (mx >= listX + listW - 40) {
                    module.toggle();
                } else {
                    selected = module.name();
                }

                waitingForKey = false;
                return true;
            }

            i++;
        }

        Module module = SlenderClient.getModule(selected);

        if (module != null) {
            int rx = left + 251;
            int ry = listY;
            int rw = widthPanel - 269;

            if (mx >= rx + 45 && mx <= rx + rw
                    && my >= ry + 39 && my <= ry + 58) {
                waitingForKey = true;
                return true;
            }

            int sy = ry + 92;

            for (String name : module.toggles().keySet()) {
                if (mx >= rx && mx <= rx + rw
                        && my >= sy && my <= sy + 25) {
                    module.toggles().put(
                            name, !module.toggles().get(name));
                    return true;
                }

                sy += 30;
            }

            for (String name : module.sliders().keySet()) {
                int barY = sy + 18;

                if (my >= barY - 7 && my <= barY + 12
                        && mx >= rx && mx <= rx + rw) {

                    double ratio = Math.max(0, Math.min(1,
                            (mx - rx) / rw));

                    double min = name.toLowerCase(Locale.ROOT)
                            .contains("scale") ? 0.5 : 4.0;

                    double max = name.toLowerCase(Locale.ROOT)
                            .contains("scale") ? 2.0
                            : name.toLowerCase(Locale.ROOT)
                            .contains("radius") ? 16.0 : 64.0;

                    module.sliders().put(name,
                            Math.round(
                                    (min + ratio * (max - min)) * 10.0
                            ) / 10.0);

                    return true;
                }

                sy += 34;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (waitingForKey) {
            Module module = SlenderClient.getModule(selected);

            if (module != null) {
                module.setKeyCode(
                        keyCode == GLFW.GLFW_KEY_BACKSPACE
                                ? GLFW.GLFW_KEY_UNKNOWN
                                : keyCode
                );
            }

            waitingForKey = false;
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private String keyName(int key) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return "NONE";
        }

        if (key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            return "RIGHT SHIFT";
        }

        if (key == GLFW.GLFW_KEY_LEFT_SHIFT) {
            return "LEFT SHIFT";
        }

        if (key == GLFW.GLFW_KEY_SPACE) {
            return "SPACE";
        }

        String name = GLFW.glfwGetKeyName(key, 0);

        return name == null
                ? "KEY " + key
                : name.toUpperCase(Locale.ROOT);
    }

    private void outline(DrawContext ctx, int x, int y,
                         int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }
}
