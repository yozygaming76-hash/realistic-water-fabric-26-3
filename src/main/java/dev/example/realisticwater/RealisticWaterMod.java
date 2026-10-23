
package dev.example.realisticwater;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public final class RealisticWaterMod implements ModInitializer {

    public static final String MOD_ID = "realistic_water";

    public static final Item REALISTIC_WATER_BUCKET = registerItem(
        "realistic_water_bucket",
        new RealisticWaterBucketItem(
            new Item.Properties()
                .setId(
                    ResourceKey.create(
                        Registries.ITEM,
                        Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "realistic_water_bucket"
                        )
                    )
                )
                .stacksTo(1)
        )
    );

    private static Item registerItem(String name, Item item) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(MOD_ID, name),
            item
        );
    }

    @Override
    public void onInitialize() {
        System.out.println(
            "[Realistic Water] Simulation bucket integration loaded!"
        );

        // Advance simulated water at the end of each server world tick.
      ServerTickEvents.END_LEVEL_TICK.register(
    WaterSimulation::tickWorld
);
    }

    public static final class RealisticWaterBucketItem extends Item {

        public RealisticWaterBucketItem(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            Level level = context.getLevel();

            BlockPos target = context.getClickedPos()
                .relative(context.getClickedFace());

            Player player = context.getPlayer();

            // Only pour into a replaceable block position.
            if (!level.getBlockState(target).canBeReplaced()) {
                return InteractionResult.FAIL;
            }

            // The server performs the actual simulation update.
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            // Find the simulation region containing this position.
            int regionX = Math.floorDiv(target.getX(), WaterGrid.WIDTH);
            int regionY = Math.floorDiv(target.getY(), WaterGrid.HEIGHT);
            int regionZ = Math.floorDiv(target.getZ(), WaterGrid.DEPTH);

            String gridKey =
                level.dimension().toString()
                + ":" + regionX
                + ":" + regionY
                + ":" + regionZ;

            WaterGrid grid = WaterSimulation.getGrid(gridKey);

            // Convert world coordinates to local grid coordinates.
            int localX = Math.floorMod(target.getX(), WaterGrid.WIDTH);
            int localY = Math.floorMod(target.getY(), WaterGrid.HEIGHT);
            int localZ = Math.floorMod(target.getZ(), WaterGrid.DEPTH);

            // Do not allow pouring into an already-full cell.
            if (grid.getWater(localX, localY, localZ) >= 0.99F) {
                return InteractionResult.FAIL;
            }

            // Add simulated water without placing a vanilla water block.
            grid.addWater(localX, localY, localZ, 1.0F);

            level.gameEvent(
                player,
                GameEvent.FLUID_PLACE,
                target
            );

            level.playSound(
                null,
                target,
                SoundEvents.BUCKET_EMPTY,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
            );

            // Return a normal empty bucket in Survival.
            if (player == null || !player.getAbilities().instabuild) {
                ItemStack heldStack = context.getItemInHand();
                heldStack.shrink(1);

                if (player != null) {
                    player.getInventory().add(
                        new ItemStack(Items.BUCKET)
                    );
                }
            }

            return InteractionResult.SUCCESS;
        }
    }
}
