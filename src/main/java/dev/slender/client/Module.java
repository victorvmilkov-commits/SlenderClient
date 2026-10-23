package dev.slender.client;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Module {
    private final String name;
    private boolean enabled;
    private int keyCode;
    private final Map<String, Double> sliders = new LinkedHashMap<>();
    private final Map<String, Boolean> toggles = new LinkedHashMap<>();

    public Module(String name, int keyCode, boolean enabled) {
        this.name = name;
        this.keyCode = keyCode;
        this.enabled = enabled;
    }

    public String name() { return name; }
    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void toggle() { enabled = !enabled; }
    public int keyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    public Map<String, Double> sliders() { return sliders; }
    public Map<String, Boolean> toggles() { return toggles; }

    public Module slider(String name, double value) {
        sliders.put(name, value);
        return this;
    }

    public Module toggleSetting(String name, boolean value) {
        toggles.put(name, value);
        return this;
    }
}
