
package dev.example.realisticwater;

import net.fabricmc.api.ClientModInitializer;

public final class RealisticWaterClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println(
            "[Realistic Water] Client loaded successfully!"
        );
    }
}

}
