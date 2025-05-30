package net.shoreline.client.util.changelogs;

/**
 * h_ypi
 * @since 1.0
 */
public class ChangeLogEntry {
    public ChangeType type;
    public String description;

    public ChangeLogEntry(ChangeType type, String description) {
        this.type = type;
        this.description = description;
    }
}