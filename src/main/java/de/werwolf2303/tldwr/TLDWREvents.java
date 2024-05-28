package de.werwolf2303.tldwr;

public enum TLDWREvents {
    DOWNLOAD_FINISHED("downloadfinished", "Fires when a download finishes"),
    MOD_SELECTED("modselected", "Fires when a mod gets selected"),
    MOD_UNSELECTED("modunselected", "Fires when a mod gets unselected"),
    MODLOAD_FINISHED("modloadfinished", "Fires when the mod loading has finished");

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    private final String name;
    private final String description;
    TLDWREvents(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
