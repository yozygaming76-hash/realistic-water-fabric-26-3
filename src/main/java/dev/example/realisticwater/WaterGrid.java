
package dev.example.realisticwater;

import java.util.Arrays;

/**
 * A small, independent water simulation grid.
 *
 * Each cell stores a water depth from 0.0 to 1.0.
 * Gravity moves water downward; pressure redistributes
 * water between neighbouring cells.
 *
 * This class does not place or change Minecraft blocks.
 */
public final class WaterGrid {

    public static final int WIDTH = 16;
    public static final int DEPTH = 16;
    public static final int HEIGHT = 8;

    private static final float MAX_DEPTH = 1.0F;
    private static final float GRAVITY = 0.12F;
    private static final float HORIZONTAL_FLOW = 0.20F;
    private static final float DAMPING = 0.985F;

    private final float[][][] water =
        new float[WIDTH][HEIGHT][DEPTH];

    private final float[][][] velocity =
        new float[WIDTH][HEIGHT][DEPTH];

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
     * Advances the simulation by one step.
     * Call on the server at a controlled tick interval.
     */
    public synchronized void tick() {
        float[][][] next =
            new float[WIDTH][HEIGHT][DEPTH];

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    next[x][y][z] = water[x][y][z];
                }
            }
        }

        // Process each cell once. Transfers are limited so
        // the simulation cannot create water from nothing.
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {

                    float amount = water[x][y][z];

                    if (amount <= 0.001F) {
                        continue;
                    }

                    float remaining = amount;

                    // Gravity: move water into the cell below.
                    if (y > 0) {
                        float below = water[x][y - 1][z];
                        float capacity = MAX_DEPTH - below;

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

                    // Pressure: spread toward lower neighbouring
                    // cells, only moving a fraction per step.
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

                        float neighbour = water[nx][y][nz];
                        float difference = amount - neighbour;

                        if (difference <= 0.05F) {
                            continue;
                        }

                        float transfer = Math.min(
                            remaining,
                            Math.min(
                                difference * HORIZONTAL_FLOW,
                                HORIZONTAL_FLOW
                            )
                        );

                        transfer = Math.min(
                            transfer,
                            MAX_DEPTH - next[nx][y][nz]
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

                    velocity[x][y][z] =
                        velocity[x][y][z] * DAMPING
                        + (water[x][y][z] > 0 ? GRAVITY : 0);
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

        for (float[][] layer : velocity) {
            for (float[] row : layer) {
                Arrays.fill(row, 0.0F);
            }
        }
    }
}
