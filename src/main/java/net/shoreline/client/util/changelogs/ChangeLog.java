package net.shoreline.client.util.changelogs;

import java.util.ArrayList;
import java.util.List;

/**
 * h_ypi
 * @since 1.0
 */
public class ChangeLog {
    private static final List<ChangeLogEntry> changeLogs = new ArrayList<>();

    static {
        changeLogs.add(new ChangeLogEntry(ChangeType.ADD,"CrystalModelを追加"));
        changeLogs.add(new ChangeLogEntry(ChangeType.ADD,"Animationsを追加"));
        changeLogs.add(new ChangeLogEntry(ChangeType.ADD,"Godmodeを追加（ゴミ）"));
        changeLogs.add(new ChangeLogEntry(ChangeType.ADD,"Zoomを追加"));
        changeLogs.add(new ChangeLogEntry(ChangeType.IMPROVE,"その他大量の修正と改善"));
    }

    public static List<ChangeLogEntry> getChangeLogs() {
        return changeLogs;
    }
}
