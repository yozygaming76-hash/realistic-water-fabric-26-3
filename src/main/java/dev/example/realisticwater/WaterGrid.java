
package dev.example.realisticwater;

import java.util.Arrays;

/**
 * A small water simulation grid.
 *
 * Each cell stores a water amount between 0 and 1.
 * Terrain checks can prevent water entering blocked cells.
 *
 * This class does not change Minecraft blocks or render water.
 */
public final class WaterGrid {

    public static final int WIDTH = 16;
    public static final int DEPTH = 16;
    public static final int HEIGHT = 8;

    private static final float MAX_DEPTH = 1.0F;
    private static final float GRAVITY = 0.12F;
    private static final float HORIZONTAL_FLOW = 0.20F;

    private final float[][][] water =
        new float[WIDTH][HEIGHT][DEPTH];

    /**
     * Implement this interface to tell the simulation
     * which cells contain solid terrain.
     */
    @FunctionalInterface
    public interface Terrain {
        boolean isBlocked(int x, int y, int z);
    }

    public WaterGrid() {
    }

    public synchronized void addWater(
        int x,
        int y,
        int z,
        float amount
    ) {
        if (!inside(x, y, z) || amount <= 0.0F) {
            return;
        }

        water[x][y][z] = Math.min(
            MAX_DEPTH,
            water[x][y][z] + amount
        );
    }

    public synchronized float getWater(
        int x,
        int y,
        int z
    ) {
        if (!inside(x, y, z)) {
            return 0.0F;
        }

        return water[x][y][z];
    }

    /**
     * Compatibility method for existing code.
     * This version has no terrain information.
     */
    public synchronized void tick() {
        tick((x, y, z) -> false);
    }

    /**
     * Advances the simulation while checking terrain.
     *
     * The terrain callback uses coordinates local to this grid.
     * Blocked cells cannot receive water from other cells.
     */
    public synchronized void tick(Terrain terrain) {
        if (terrain == null) {
            throw new IllegalArgumentException(
                "Terrain checker cannot be null"
            );
        }

        float[][][] next =
            new float[WIDTH][HEIGHT][DEPTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    next[x][y][z] = water[x][y][z];
                }
            }
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {

                    float amount = water[x][y][z];

                    if (amount <= 0.001F) {
                        continue;
                    }

                    // Do not move water out of a blocked cell.
                    if (terrain.isBlocked(x, y, z)) {
                        continue;
                    }

                    float remaining = next[x][y][z];

                    // Gravity: try the cell below first.
                    if (y > 0
                        && !terrain.isBlocked(x, y - 1, z)) {

                        float capacity =
                            MAX_DEPTH - next[x][y - 1][z];

                        if (capacity > 0.0F) {
                            float transfer = Math.min(
                                remaining,
                                Math.min(capacity, GRAVITY)
                            );

                            next[x][y][z] -= transfer;
                            next[x][y - 1][z] += transfer;
                            remaining -= transfer;
                        }
                    }

                    // Spread horizontally into less-full cells.
                    int[][] directions = {
                        {1, 0},
                        {-1, 0},
                        {0, 1},
                        {0, -1}
                    };

                    for (int[] direction : directions) {
                        if (remaining <= 0.001F) {
                            break;
                        }

                        int nx = x + direction[0];
                        int nz = z + direction[1];

                        if (!inside(nx, y, nz)) {
                            continue;
                        }

                        if (terrain.isBlocked(nx, y, nz)) {
                            continue;
                        }

                        float neighbour = next[nx][y][nz];
                        float difference = amount - neighbour;

                        if (difference <= 0.05F) {
                            continue;
                        }

                        float capacity = MAX_DEPTH - neighbour;

                        if (capacity <= 0.0F) {
                            continue;
                        }

                        float transfer = Math.min(
                            remaining,
                            Math.min(
                                capacity,
                                Math.min(
                                    difference * HORIZONTAL_FLOW,
                                    HORIZONTAL_FLOW
                                )
                            )
                        );

                        if (transfer <= 0.0F) {
                            continue;
                        }

                        next[x][y][z] -= transfer;
                        next[nx][y][nz] += transfer;
                        remaining -= transfer;
                    }
                }
            }
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    water[x][y][z] = Math.max(
                        0.0F,
                        Math.min(MAX_DEPTH, next[x][y][z])
                    );
                }
            }
        }
    }

    private boolean inside(int x, int y, int z) {
        return x >= 0 && x < WIDTH
            && y >= 0 && y < HEIGHT
            && z >= 0 && z < DEPTH;
    }

    public synchronized void clear() {
        for (float[][] layer : water) {
            for (float[] row : layer) {
                Arrays.fill(row, 0.0F);
            }
        }
    }
}
