
package dev.example.realisticwater;

import java.util.Map;
import java.util.HashMap;

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
     * Advances the grids belonging to this Minecraft dimension.
     * Call from the server's world-tick event.
     */
    public static synchronized void tickWorld(ServerLevel world) {
        tickCounter++;

        // Simulate every third world tick to reduce server load.
        if (tickCounter % 3 != 0) {
            return;
        }

        String dimensionPrefix = world.dimension().toString() + ":";

        for (Map.Entry<String, WaterGrid> entry : GRIDS.entrySet()) {
            String key = entry.getKey();

            if (!key.startsWith(dimensionPrefix)) {
                continue;
            }

            int[] region = parseRegion(key);

            if (region == null) {
                continue;
            }

            int baseX = region[0] * WaterGrid.WIDTH;
            int baseY = region[1] * WaterGrid.HEIGHT;
            int baseZ = region[2] * WaterGrid.DEPTH;

            WaterGrid grid = entry.getValue();

            grid.tick((x, y, z) -> {
                BlockPos pos = new BlockPos(
                    baseX + x,
                    baseY + y,
                    baseZ + z
                );

                // Never load an unloaded chunk just for simulation.
                if (!world.hasChunkAt(pos)) {
                    return true;
                }

                // Blocks with collision shapes stop the simulated water.
                return !world.getBlockState(pos)
                    .getCollisionShape(world, pos)
                    .isEmpty();
            });
        }
    }

    /**
     * Reads the last three colon-separated values from the grid key.
     * This allows the dimension identifier to contain colons too.
     */
    private static int[] parseRegion(String key) {
        try {
            int last = key.lastIndexOf(':');
            int second = key.lastIndexOf(':', last - 1);
            int third = key.lastIndexOf(':', second - 1);

            if (third < 0 || second < 0 || last < 0) {
                return null;
            }

            return new int[] {
                Integer.parseInt(key.substring(third + 1, second)),
                Integer.parseInt(key.substring(second + 1, last)),
                Integer.parseInt(key.substring(last + 1))
            };
        } catch (RuntimeException exception) {
            return null;
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
