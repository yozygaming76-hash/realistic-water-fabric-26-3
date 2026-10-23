
package dev.example.realisticwater;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages separate simulated water grids.
 * This is independent of Minecraft's normal water blocks.
 */
public final class WaterSimulation {

    private static final Map<String, WaterGrid> GRIDS =
        new HashMap<>();

    private WaterSimulation() {
    }

    /**
     * Gets or creates a water grid for a simulation area.
     */
    public static synchronized WaterGrid getGrid(String key) {
        return GRIDS.computeIfAbsent(
            key,
            ignored -> new WaterGrid()
        );
    }

    /**
     * Removes a simulation area.
     */
    public static synchronized void removeGrid(String key) {
        GRIDS.remove(key);
    }

    /**
     * Clears every simulated water grid.
     */
    public static synchronized void clearAll() {
        for (WaterGrid grid : GRIDS.values()) {
            grid.clear();
        }

        GRIDS.clear();
    }
}
