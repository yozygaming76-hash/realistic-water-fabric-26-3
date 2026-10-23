
package dev.example.realisticwater;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class WaterSimulation {

    private static final Map<String, WaterGrid> GRIDS =
        new HashMap<>();

    private static int tickCounter = 0;

    private WaterSimulation() {
    }

    public static synchronized WaterGrid getGrid(String key) {
        return GRIDS.computeIfAbsent(
            key,
            ignored -> new WaterGrid()
        );
    }

    public static synchronized void removeGrid(String key) {
        GRIDS.remove(key);
    }

    /**
     * Returns the grid entries for later rendering integration.
     */
    public static synchronized Map<String, WaterGrid> getGridsSnapshot() {
        return new HashMap<>(GRIDS);
    }

    /**
     * Calculates the world-space origin of a grid from its key.
     */
    public static BlockPos getGridOrigin(String key) {
        int last = key.lastIndexOf(':');
        int second = last < 0 ? -1 : key.lastIndexOf(':', last - 1);
        int third = second < 0 ? -1 : key.lastIndexOf(':', second - 1);

        if (third < 0 || second < 0 || last < 0) {
            return BlockPos.ZERO;
        }

        try {
            int regionX = Integer.parseInt(
                key.substring(third + 1, second)
            );
            int regionY = Integer.parseInt(
                key.substring(second + 1, last)
            );
            int regionZ = Integer.parseInt(
                key.substring(last + 1)
            );

            return new BlockPos(
                regionX * WaterGrid.WIDTH,
                regionY * WaterGrid.HEIGHT,
                regionZ * WaterGrid.DEPTH
            );
        } catch (NumberFormatException exception) {
            return BlockPos.ZERO;
        }
    }

    /**
     * Advances grids belonging to this dimension.
     */
    public static synchronized void tickWorld(ServerLevel world) {
        tickCounter++;

        if (tickCounter % 3 != 0) {
            return;
        }

        String dimensionPrefix = world.dimension().toString() + ":";

        for (Map.Entry<String, WaterGrid> entry : GRIDS.entrySet()) {
            String key = entry.getKey();

            if (!key.startsWith(dimensionPrefix)) {
                continue;
            }

            BlockPos origin = getGridOrigin(key);
            WaterGrid grid = entry.getValue();

            grid.tick((x, y, z) -> {
                BlockPos pos = origin.offset(x, y, z);

                if (!world.hasChunkAt(pos)) {
                    return true;
                }

                return !world.getBlockState(pos)
                    .getCollisionShape(world, pos)
                    .isEmpty();
            });
        }
    }

    public static synchronized void clearAll() {
        for (WaterGrid grid : GRIDS.values()) {
            grid.clear();
        }

        GRIDS.clear();
        tickCounter = 0;
    }
}
