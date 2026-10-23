
package dev.example.realisticwater;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;

public final class RealisticWaterClient implements ClientModInitializer {

    private WaypointRenderState waypointState;

    @Override
    public void onInitializeClient() {
        LevelExtractionEvents.END_EXTRACTION.register(
            this::extractWaypoint
        );

        System.out.println(
            "[Realistic Water] Client loaded successfully!"
        );
    }

    private void extractWaypoint(LevelExtractionContext context) {
        waypointState = new WaypointRenderState(
            0, 100, 0,
            0.0f, 1.0f, 0.0f, 0.5f
        );
    }

    public static void close() {
        // No GPU resources allocated yet.
    }

    private record WaypointRenderState(
        int x,
        int y,
        int z,
        float r,
        float g,
        float b,
        float a
    ) {}
}
